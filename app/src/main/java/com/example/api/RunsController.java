package com.example.api;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.AbstractHttpEndpoint;
import akka.javasdk.http.HttpResponses;
import com.example.domain.Run;
import com.example.domain.entities.RunEntity;
import com.example.application.workflows.RunEvaluationsWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * HTTP endpoint for evaluation run management.
 * Handles starting runs and querying run status.
 */
@HttpEndpoint("/api/runs")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class RunsController extends AbstractHttpEndpoint {

  private static final Logger logger = LoggerFactory.getLogger(RunsController.class);
  private final ComponentClient componentClient;

  public RunsController(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  /**
   * POST /api/runs
   * Start a new evaluation run for a queue.
   */
  @Post
  public HttpResponse startRun(StartRunRequest request) {
    try {
      String runId = UUID.randomUUID().toString();

      // First, we need to calculate the planned count
      // This would require querying submissions and judge assignments
      // For now, we'll start the run with a placeholder count
      // The workflow will handle the actual counting

      // Create the run entity
      componentClient
          .forEventSourcedEntity(runId)
          .method(RunEntity::startRun)
          .invoke(new RunEntity.StartRunRequest(request.queueId(), 0)); // Count will be updated by workflow

      // Start the workflow
      componentClient
          .forWorkflow(runId)
          .method(RunEvaluationsWorkflow::startRun)
          .invoke(new RunEvaluationsWorkflow.StartRunRequest(request.queueId()));

      logger.info("Started evaluation run {} for queue {}", runId, request.queueId());

      return HttpResponses.created(runId);

    } catch (Exception e) {
      logger.error("Failed to start run", e);
      throw new RuntimeException("Failed to start run: " + e.getMessage(), e);
    }
  }

  /**
   * GET /api/runs/{runId}
   * Get the status of an evaluation run.
   */
  @Get("/{runId}")
  public Run getRun(String runId) {
    try {
      return componentClient
          .forEventSourcedEntity(runId)
          .method(RunEntity::getRun)
          .invoke();
    } catch (Exception e) {
      logger.error("Failed to get run {}", runId, e);
      throw new RuntimeException("Failed to get run: " + e.getMessage(), e);
    }
  }

  public record StartRunRequest(String queueId) {}
}

