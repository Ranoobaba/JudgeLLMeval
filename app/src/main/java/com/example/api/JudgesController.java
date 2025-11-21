package com.example.api;

import akka.Done;
import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.*;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.AbstractHttpEndpoint;
import akka.javasdk.http.HttpResponses;
import com.example.domain.Judge;
import com.example.domain.entities.JudgeEntity;
import com.example.domain.views.JudgesView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * HTTP endpoint for judge management.
 * Handles CRUD operations and active flag toggling.
 */
@HttpEndpoint("/api/judges")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class JudgesController extends AbstractHttpEndpoint {

  private static final Logger logger = LoggerFactory.getLogger(JudgesController.class);
  private final ComponentClient componentClient;

  public JudgesController(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  /**
   * GET /api/judges
   * List all judges.
   */
  @Get
  public JudgesView.JudgesResult getAllJudges() {
    try {
      return componentClient
          .forView()
          .method(JudgesView::getAllJudges)
          .invoke();
    } catch (Exception e) {
      logger.error("Failed to get judges", e);
      throw new RuntimeException("Failed to get judges: " + e.getMessage(), e);
    }
  }

  /**
   * GET /api/judges/{judgeId}
   * Get a specific judge.
   */
  @Get("/{judgeId}")
  public Judge getJudge(String judgeId) {
    try {
      return componentClient
          .forEventSourcedEntity(judgeId)
          .method(JudgeEntity::getJudge)
          .invoke();
    } catch (Exception e) {
      logger.error("Failed to get judge {}", judgeId, e);
      throw new RuntimeException("Failed to get judge: " + e.getMessage(), e);
    }
  }

  /**
   * POST /api/judges
   * Create a new judge.
   */
  @Post
  public HttpResponse createJudge(CreateJudgeRequest request) {
    try {
      String judgeId = request.judgeId() != null && !request.judgeId().isEmpty()
          ? request.judgeId()
          : UUID.randomUUID().toString();

      Judge judge = new Judge(
          judgeId,
          request.name(),
          request.systemPrompt(),
          request.targetModel(),
          request.active()
      );

      componentClient
          .forEventSourcedEntity(judgeId)
          .method(JudgeEntity::createJudge)
          .invoke(judge);

      logger.info("Created judge {}", judgeId);

      return HttpResponses.created(judgeId);

    } catch (Exception e) {
      logger.error("Failed to create judge", e);
      throw new RuntimeException("Failed to create judge: " + e.getMessage(), e);
    }
  }

  /**
   * PUT /api/judges/{judgeId}
   * Update a judge.
   */
  @Put("/{judgeId}")
  public HttpResponse updateJudge(String judgeId, UpdateJudgeRequest request) {
    try {
      componentClient
          .forEventSourcedEntity(judgeId)
          .method(JudgeEntity::updateJudge)
          .invoke(new JudgeEntity.UpdateJudgeRequest(
              request.name(),
              request.systemPrompt(),
              request.targetModel()
          ));

      logger.info("Updated judge {}", judgeId);

      return HttpResponses.ok();

    } catch (Exception e) {
      logger.error("Failed to update judge {}", judgeId, e);
      throw new RuntimeException("Failed to update judge: " + e.getMessage(), e);
    }
  }

  /**
   * DELETE /api/judges/{judgeId}
   * Delete a judge.
   */
  @Delete("/{judgeId}")
  public HttpResponse deleteJudge(String judgeId) {
    try {
      componentClient
          .forEventSourcedEntity(judgeId)
          .method(JudgeEntity::deleteJudge)
          .invoke();

      logger.info("Deleted judge {}", judgeId);

      return HttpResponses.ok();

    } catch (Exception e) {
      logger.error("Failed to delete judge {}", judgeId, e);
      throw new RuntimeException("Failed to delete judge: " + e.getMessage(), e);
    }
  }

  /**
   * PATCH /api/judges/{judgeId}/active
   * Toggle the active flag of a judge.
   */
  @Patch("/{judgeId}/active")
  public HttpResponse setActive(String judgeId, SetActiveRequest request) {
    try {
      componentClient
          .forEventSourcedEntity(judgeId)
          .method(JudgeEntity::setActive)
          .invoke(request.active());

      logger.info("Set judge {} active to {}", judgeId, request.active());

      return HttpResponses.ok();

    } catch (Exception e) {
      logger.error("Failed to set active flag for judge {}", judgeId, e);
      throw new RuntimeException("Failed to update active flag: " + e.getMessage(), e);
    }
  }

  public record CreateJudgeRequest(
      String judgeId,
      String name,
      String systemPrompt,
      String targetModel,
      boolean active
  ) {}

  public record UpdateJudgeRequest(
      String name,
      String systemPrompt,
      String targetModel
  ) {}

  public record SetActiveRequest(boolean active) {}
}

