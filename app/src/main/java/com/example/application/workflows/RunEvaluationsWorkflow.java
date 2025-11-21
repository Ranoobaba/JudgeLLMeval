package com.example.application.workflows;

import akka.Done;
import akka.javasdk.annotations.Component;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.workflow.Workflow;
import akka.javasdk.workflow.WorkflowContext;
import com.example.application.agents.JudgeAgent;
import com.example.application.llm.LLMException;
import com.example.domain.*;
import com.example.domain.entities.*;
import com.example.domain.views.*;
import com.example.domain.views.SubmissionsView;
import com.example.domain.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Workflow that orchestrates evaluation runs for a queue.
 * 
 * Responsibilities:
 * - Iterates over submissions and questions in a queue
 * - Looks up which judges are assigned to each question
 * - Invokes JudgeAgent for each (question × judge) pair
 * - Persists evaluations and updates run progress
 */
@Component(id = "run-evaluations-workflow")
public class RunEvaluationsWorkflow extends Workflow<RunEvaluationsWorkflowState> {

  private static final Logger logger = LoggerFactory.getLogger(RunEvaluationsWorkflow.class);
  
  private final ComponentClient componentClient;
  private final JudgeAgent judgeAgent;
  private final String workflowId;

  public RunEvaluationsWorkflow(
      WorkflowContext context,
      ComponentClient componentClient,
      JudgeAgent judgeAgent
  ) {
    this.componentClient = componentClient;
    this.judgeAgent = judgeAgent;
    this.workflowId = context.workflowId();
  }

  @Override
  public WorkflowSettings settings() {
    return WorkflowSettings.builder()
        .defaultStepTimeout(Duration.ofMinutes(30))
        .build();
  }

  @Override
  public RunEvaluationsWorkflowState emptyState() {
    return new RunEvaluationsWorkflowState("", "", List.of(), 0, 0);
  }

  /**
   * Command: Start an evaluation run for a queue.
   */
  public Effect<Done> startRun(StartRunRequest request) {
    String runId = java.util.UUID.randomUUID().toString();
    logger.info("Starting evaluation run {} for queue {}", runId, request.queueId());

    // Create initial state
    RunEvaluationsWorkflowState initialState = new RunEvaluationsWorkflowState(
        runId,
        request.queueId(),
        List.of(), // Will be populated in the first step
        0,
        0
    );

    return effects()
        .updateState(initialState)
        .transitionTo(RunEvaluationsWorkflow::prepareEvaluationsStep)
        .thenReply(Done.getInstance());
  }

  /**
   * Step: Prepare evaluation tasks by querying submissions, questions, and judge assignments.
   */
  private StepEffect prepareEvaluationsStep() {
    String queueId = currentState().queueId();
    logger.info("Preparing evaluations for queue {}", queueId);

    try {
      // Query questions for this queue
      QuestionsView.QuestionsResult questionsResult = componentClient
          .forView()
          .method(QuestionsView::getQuestionsByQueue)
          .invoke(queueId);

      if (questionsResult == null || questionsResult.questions() == null || questionsResult.questions().isEmpty()) {
        logger.warn("No questions found for queue {}", queueId);
        return stepEffects()
            .updateState(currentState().withPendingEvaluations(List.of()))
            .thenEnd();
      }

      // Query submissions for this queue
      SubmissionsView.SubmissionsResult submissionsResult = componentClient
          .forView()
          .method(SubmissionsView::getSubmissionsByQueue)
          .invoke(queueId);

      if (submissionsResult == null || submissionsResult.submissions() == null || submissionsResult.submissions().isEmpty()) {
        logger.warn("No submissions found for queue {}", queueId);
        return stepEffects()
            .updateState(currentState().withPendingEvaluations(List.of()))
            .thenEnd();
      }

      // Query judge assignments for each question
      List<RunEvaluationsWorkflowState.EvaluationTask> tasks = new ArrayList<>();

      // Query active judges once
      JudgesView.JudgesResult activeJudgesResult = componentClient
          .forView()
          .method(JudgesView::getActiveJudges)
          .invoke();

      Set<String> activeJudgeIds = activeJudgesResult != null && activeJudgesResult.judges() != null
          ? activeJudgesResult.judges().stream()
              .map(JudgesView.JudgeEntry::judgeId)
              .collect(Collectors.toSet())
          : Set.of();

      // For each question and each submission, create tasks
      for (QuestionsView.QuestionEntry question : questionsResult.questions()) {
        // Query judge assignments for this question
        String assignmentEntityId = queueId + "|" + question.questionTemplateId();
        
        try {
          JudgeAssignment assignment = componentClient
              .forKeyValueEntity(assignmentEntityId)
              .method(JudgeAssignmentEntity::getAssignments)
              .invoke();

          if (assignment != null && assignment.judgeIds() != null) {
            // For each submission and each assigned active judge, create a task
            for (SubmissionsView.SubmissionEntry submission : submissionsResult.submissions()) {
              for (String judgeId : assignment.judgeIds()) {
                if (activeJudgeIds.contains(judgeId)) {
                  tasks.add(new RunEvaluationsWorkflowState.EvaluationTask(
                      submission.submissionId(),
                      question.questionTemplateId(),
                      judgeId,
                      null // request - will be built in processEvaluationsStep
                  ));
                }
              }
            }
          }
        } catch (Exception e) {
          logger.warn("Failed to get judge assignments for question {}: {}", 
              question.questionTemplateId(), e.getMessage());
        }
      }

      logger.info("Prepared {} evaluation tasks for queue {}", tasks.size(), queueId);

      if (tasks.isEmpty()) {
        logger.warn("No evaluation tasks created for queue {}", queueId);
        return stepEffects()
            .updateState(currentState().withPendingEvaluations(List.of()))
            .thenEnd();
      }

      // Create run entity with planned count
      componentClient
          .forEventSourcedEntity(currentState().runId())
          .method(RunEntity::startRun)
          .invoke(new RunEntity.StartRunRequest(queueId, tasks.size()));

      // Update state with tasks and transition to processing step
      return stepEffects()
          .updateState(currentState().withPendingEvaluations(tasks))
          .thenTransitionTo(RunEvaluationsWorkflow::processEvaluationsStep);

    } catch (Exception e) {
      logger.error("Failed to prepare evaluations for queue {}", queueId, e);
      return stepEffects()
          .updateState(currentState())
          .thenEnd(); // End workflow on error
    }
  }

  /**
   * Step: Process evaluations one by one.
   * For each task, builds EvaluationRequest, calls JudgeAgent, and persists results.
   */
  private StepEffect processEvaluationsStep() {
    if (currentState().isComplete()) {
      logger.info("All evaluations completed for run {}", currentState().runId());
      return stepEffects().thenEnd();
    }

    // Process one evaluation at a time
    RunEvaluationsWorkflowState.EvaluationTask task = currentState().pendingEvaluations().get(0);
    List<RunEvaluationsWorkflowState.EvaluationTask> remainingTasks = 
        currentState().pendingEvaluations().subList(1, currentState().pendingEvaluations().size());

    logger.info("Processing evaluation: question={}, judge={}", 
        task.questionTemplateId(), task.judgeId());

    try {
      // Build EvaluationRequest
      // TODO: Get submission data and judge data to build the full request
      // For now, this is a simplified version - in production, we'd query:
      // - Submission data from SubmissionsEntity
      // - Judge data from JudgeEntity
      // - Question data from QuestionsView

      // This is a placeholder - the actual implementation would query entities
      EvaluationRequest request = buildEvaluationRequest(task);

      // Call JudgeAgent
      EvaluationResponse response = judgeAgent.evaluate(request);

      // Persist evaluation
      String evaluationId = java.util.UUID.randomUUID().toString();
      componentClient
          .forEventSourcedEntity(evaluationId)
          .method(EvaluationEntity::recordEvaluation)
          .invoke(new EvaluationEntity.RecordEvaluationRequest(
              currentState().runId(),
              request.submissionId(),
              request.queueId(),
              request.questionTemplateId(),
              request.judgeId(),
              response.verdict(),
              response.reasoning()
          ));

      // Update run progress
      componentClient
          .forEventSourcedEntity(currentState().runId())
          .method(RunEntity::markCompleted)
          .invoke();

      // Update workflow state
      RunEvaluationsWorkflowState newState = currentState()
          .withPendingEvaluations(remainingTasks)
          .withCompletedCount(currentState().completedCount() + 1);

      // Continue processing or end if complete
      if (newState.isComplete()) {
        logger.info("All evaluations completed for run {}", currentState().runId());
        return stepEffects()
            .updateState(newState)
            .thenEnd();
      } else {
        return stepEffects()
            .updateState(newState)
            .thenTransitionTo(RunEvaluationsWorkflow::processEvaluationsStep);
      }

    } catch (LLMException e) {
      logger.error("LLM evaluation failed for task: {}", task, e);
      
      // Mark as failed
      componentClient
          .forEventSourcedEntity(currentState().runId())
          .method(RunEntity::markFailed)
          .invoke();

      // Update workflow state
      RunEvaluationsWorkflowState newState = currentState()
          .withPendingEvaluations(remainingTasks)
          .withFailedCount(currentState().failedCount() + 1);

      // Continue processing even on failure
      if (newState.isComplete()) {
        return stepEffects()
            .updateState(newState)
            .thenEnd();
      } else {
        return stepEffects()
            .updateState(newState)
            .thenTransitionTo(RunEvaluationsWorkflow::processEvaluationsStep);
      }
    } catch (Exception e) {
      logger.error("Unexpected error processing evaluation: {}", task, e);
      
      // Mark as failed and continue
      componentClient
          .forEventSourcedEntity(currentState().runId())
          .method(RunEntity::markFailed)
          .invoke();

      RunEvaluationsWorkflowState newState = currentState()
          .withPendingEvaluations(remainingTasks)
          .withFailedCount(currentState().failedCount() + 1);

      if (newState.isComplete()) {
        return stepEffects()
            .updateState(newState)
            .thenEnd();
      } else {
        return stepEffects()
            .updateState(newState)
            .thenTransitionTo(RunEvaluationsWorkflow::processEvaluationsStep);
      }
    }
  }

  /**
   * Builds an EvaluationRequest from an EvaluationTask.
   */
  private EvaluationRequest buildEvaluationRequest(RunEvaluationsWorkflowState.EvaluationTask task) throws Exception {
    // Query judge data
    Judge judge = componentClient
        .forEventSourcedEntity(task.judgeId())
        .method(JudgeEntity::getJudge)
        .invoke();

    if (judge == null) {
      throw new Exception("Judge not found: " + task.judgeId());
    }

    // Query submission data
    Submission submission = componentClient
        .forEventSourcedEntity(task.submissionId())
        .method(SubmissionsEntity::getSubmission)
        .invoke();

    if (submission == null) {
      throw new Exception("Submission not found: " + task.submissionId());
    }

    // Find the question in the submission
    Submission.QuestionAnswer questionAnswer = null;
    for (Submission.QuestionAnswer qa : submission.questions().values()) {
      if (qa.questionTemplateId().equals(task.questionTemplateId())) {
        questionAnswer = qa;
        break;
      }
    }

    if (questionAnswer == null) {
      throw new Exception("Question " + task.questionTemplateId() + " not found in submission " + task.submissionId());
    }

    return new EvaluationRequest(
        currentState().runId(),
        task.submissionId(),
        currentState().queueId(),
        task.questionTemplateId(),
        task.judgeId(),
        questionAnswer.questionText(),
        questionAnswer.answerChoice(),
        questionAnswer.answerReasoning(),
        questionAnswer.metadata() != null ? questionAnswer.metadata() : Map.of(),
        judge.name(),
        judge.systemPrompt(),
        judge.targetModel(),
        EvaluationRequest.IncludedFields.defaults(),
        List.of() // attachmentUrls
    );
  }

  public record StartRunRequest(String queueId) {}
}

