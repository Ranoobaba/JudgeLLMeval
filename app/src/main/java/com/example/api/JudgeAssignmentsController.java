package com.example.api;

import akka.Done;
import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.*;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.AbstractHttpEndpoint;
import akka.javasdk.http.HttpResponses;
import com.example.domain.JudgeAssignment;
import com.example.domain.entities.JudgeAssignmentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * HTTP endpoint for judge assignment management.
 * Handles assigning judges to questions within queues.
 */
@HttpEndpoint("/api/queues/{queueId}/judge-assignments")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class JudgeAssignmentsController extends AbstractHttpEndpoint {

  private static final Logger logger = LoggerFactory.getLogger(JudgeAssignmentsController.class);
  private final ComponentClient componentClient;

  public JudgeAssignmentsController(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  /**
   * GET /api/queues/{queueId}/judge-assignments
   * Get all judge assignments for a queue.
   * Note: This returns assignments for all questions in the queue.
   */
  @Get
  public HttpResponse getAssignments(String queueId) {
    try {
      // Note: This is a simplified version. In production, you might want to
      // query all questions for the queue and get assignments for each.
      // For now, we'll return a message indicating this endpoint needs questionTemplateId
      return HttpResponse.create()
          .withStatus(400)
          .withEntity("Please specify questionTemplateId. Use GET /api/queues/{queueId}/judge-assignments/{questionTemplateId}");

    } catch (Exception e) {
      logger.error("Failed to get assignments for queue {}", queueId, e);
      return HttpResponse.create()
          .withStatus(500)
          .withEntity("Failed to get assignments: " + e.getMessage());
    }
  }

  /**
   * GET /api/queues/{queueId}/judge-assignments/{questionTemplateId}
   * Get judge assignments for a specific question.
   */
  @Get("/{questionTemplateId}")
  public JudgeAssignment getAssignment(String queueId, String questionTemplateId) {
    try {
      String entityId = queueId + "|" + questionTemplateId;
      return componentClient
          .forKeyValueEntity(entityId)
          .method(JudgeAssignmentEntity::getAssignments)
          .invoke();
    } catch (Exception e) {
      logger.error("Failed to get assignment for queue {} question {}", queueId, questionTemplateId, e);
      throw new RuntimeException("Failed to get assignment: " + e.getMessage(), e);
    }
  }

  /**
   * POST /api/queues/{queueId}/judge-assignments
   * Create or update judge assignments for a question.
   */
  @Post
  public HttpResponse createAssignment(String queueId, CreateAssignmentRequest request) {
    try {
      String entityId = queueId + "|" + request.questionTemplateId();

      componentClient
          .forKeyValueEntity(entityId)
          .method(JudgeAssignmentEntity::setAssignments)
          .invoke(request.judgeIds());

      logger.info("Created assignment for queue {} question {} with {} judges",
          queueId, request.questionTemplateId(), request.judgeIds().size());

      return HttpResponses.created("Assignment created");

    } catch (Exception e) {
      logger.error("Failed to create assignment", e);
      throw new RuntimeException("Failed to create assignment: " + e.getMessage(), e);
    }
  }

  /**
   * DELETE /api/queues/{queueId}/judge-assignments/{questionTemplateId}/{judgeId}
   * Remove a judge assignment.
   */
  @Delete("/{questionTemplateId}/{judgeId}")
  public HttpResponse removeAssignment(String queueId, String questionTemplateId, String judgeId) {
    try {
      String entityId = queueId + "|" + questionTemplateId;

      componentClient
          .forKeyValueEntity(entityId)
          .method(JudgeAssignmentEntity::removeJudge)
          .invoke(judgeId);

      logger.info("Removed judge {} from queue {} question {}", judgeId, queueId, questionTemplateId);

      return HttpResponses.ok();

    } catch (Exception e) {
      logger.error("Failed to remove assignment", e);
      throw new RuntimeException("Failed to remove assignment: " + e.getMessage(), e);
    }
  }

  public record CreateAssignmentRequest(
      String questionTemplateId,
      Set<String> judgeIds
  ) {}
}

