package com.example.api;

import akka.Done;
import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.*;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.AbstractHttpEndpoint;
import akka.javasdk.http.HttpResponses;
import com.example.domain.Submission;
import com.example.domain.entities.SubmissionsEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * HTTP endpoint for submission management.
 * Handles uploading and retrieving submissions.
 */
@HttpEndpoint("/api/submissions")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class SubmissionsController extends AbstractHttpEndpoint {

  private static final Logger logger = LoggerFactory.getLogger(SubmissionsController.class);
  private final ComponentClient componentClient;
  private final ObjectMapper objectMapper;

  public SubmissionsController(ComponentClient componentClient) {
    this.componentClient = componentClient;
    this.objectMapper = new ObjectMapper();
  }

  /**
   * POST /api/submissions
   * Upload a new submission.
   */
  @Post
  public HttpResponse uploadSubmission(Submission submission) {
    try {
      String submissionId = submission.submissionId() != null && !submission.submissionId().isEmpty()
          ? submission.submissionId()
          : UUID.randomUUID().toString();

      // Ensure submission has the generated ID
      Submission submissionWithId = new Submission(
          submissionId,
          submission.queueId(),
          submission.questions()
      );

      componentClient
          .forEventSourcedEntity(submissionId)
          .method(SubmissionsEntity::importSubmission)
          .invoke(submissionWithId);

      logger.info("Imported submission {} for queue {}", submissionId, submission.queueId());

      return HttpResponses.created(submissionId);

    } catch (Exception e) {
      logger.error("Failed to import submission", e);
      throw new RuntimeException("Failed to import submission: " + e.getMessage(), e);
    }
  }

  /**
   * GET /api/submissions/{submissionId}
   * Get a submission by ID.
   */
  @Get("/{submissionId}")
  public Submission getSubmission(String submissionId) {
    try {
      Submission submission = componentClient
          .forEventSourcedEntity(submissionId)
          .method(SubmissionsEntity::getSubmission)
          .invoke();

      if (submission == null) {
        throw new RuntimeException("Submission not found");
      }

      return submission;

    } catch (Exception e) {
      logger.error("Failed to get submission {}", submissionId, e);
      throw new RuntimeException("Failed to get submission: " + e.getMessage(), e);
    }
  }
}

