package com.example.application.workflows;

import com.example.domain.EvaluationRequest;

import java.util.List;

/**
 * State for RunEvaluationsWorkflow.
 * Tracks the evaluation run progress and pending evaluations.
 */
public record RunEvaluationsWorkflowState(
    String runId,
    String queueId,
    List<EvaluationTask> pendingEvaluations,
    int completedCount,
    int failedCount
) {
  /**
   * Represents a single evaluation task to be processed.
   */
  public record EvaluationTask(
      String submissionId,
      String questionTemplateId,
      String judgeId,
      EvaluationRequest request
  ) {}

  public RunEvaluationsWorkflowState withCompletedCount(int completedCount) {
    return new RunEvaluationsWorkflowState(runId, queueId, pendingEvaluations, completedCount, failedCount);
  }

  public RunEvaluationsWorkflowState withFailedCount(int failedCount) {
    return new RunEvaluationsWorkflowState(runId, queueId, pendingEvaluations, completedCount, failedCount);
  }

  public RunEvaluationsWorkflowState withPendingEvaluations(List<EvaluationTask> pendingEvaluations) {
    return new RunEvaluationsWorkflowState(runId, queueId, pendingEvaluations, completedCount, failedCount);
  }

  public boolean isComplete() {
    return pendingEvaluations.isEmpty();
  }

  public int getTotalProcessed() {
    return completedCount + failedCount;
  }
}

