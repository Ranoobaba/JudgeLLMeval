package com.example.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.AbstractHttpEndpoint;
import com.example.domain.views.EvaluationsView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP endpoint for evaluation queries.
 * Supports filtering evaluations by queue, judge, question, and verdict.
 */
@HttpEndpoint("/api/evaluations")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class EvaluationsController extends AbstractHttpEndpoint {

  private static final Logger logger = LoggerFactory.getLogger(EvaluationsController.class);
  private final ComponentClient componentClient;

  public EvaluationsController(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  /**
   * GET /api/evaluations
   * List evaluations with optional filters.
   * Query parameters: queueId, judgeId, questionTemplateId, verdict
   */
  @Get
  public EvaluationsView.EvaluationsResult getEvaluations() {
    try {
      // Get query parameters from request context
      var queryParams = requestContext().queryParams();
      String queueId = queryParams.getString("queueId").orElse(null);
      String judgeId = queryParams.getString("judgeId").orElse(null);
      String questionTemplateId = queryParams.getString("questionTemplateId").orElse(null);
      String verdict = queryParams.getString("verdict").orElse(null);

      // If all filters are provided, use the combined filter query
      if (queueId != null && judgeId != null && questionTemplateId != null && verdict != null) {
        return componentClient
            .forView()
            .method(EvaluationsView::getEvaluationsWithFilters)
            .invoke(new EvaluationsView.EvaluationFilters(queueId, judgeId, questionTemplateId, verdict));
      }

      // Otherwise, use individual filter queries
      if (queueId != null) {
        return componentClient
            .forView()
            .method(EvaluationsView::getEvaluationsByQueue)
            .invoke(queueId);
      }

      if (judgeId != null) {
        return componentClient
            .forView()
            .method(EvaluationsView::getEvaluationsByJudge)
            .invoke(judgeId);
      }

      if (questionTemplateId != null) {
        return componentClient
            .forView()
            .method(EvaluationsView::getEvaluationsByQuestion)
            .invoke(questionTemplateId);
      }

      if (verdict != null) {
        return componentClient
            .forView()
            .method(EvaluationsView::getEvaluationsByVerdict)
            .invoke(verdict);
      }

      // No filters - return all evaluations
      return componentClient
          .forView()
          .method(EvaluationsView::getAllEvaluations)
          .invoke();

    } catch (Exception e) {
      logger.error("Failed to get evaluations", e);
      throw new RuntimeException("Failed to get evaluations: " + e.getMessage(), e);
    }
  }
}

