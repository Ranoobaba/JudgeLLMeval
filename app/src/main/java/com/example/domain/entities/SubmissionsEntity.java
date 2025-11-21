package com.example.domain.entities;

import akka.Done;
import akka.javasdk.annotations.Component;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.example.domain.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Event Sourced Entity for storing submissions.
 * Each submission is stored with its full history of changes.
 */
@Component(id = "submissions")
public class SubmissionsEntity extends EventSourcedEntity<Submission, SubmissionsEntity.SubmissionEvent> {

  private static final Logger logger = LoggerFactory.getLogger(SubmissionsEntity.class);
  private final String entityId;

  public SubmissionsEntity(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public Submission emptyState() {
    return new Submission(entityId, "", Collections.emptyMap());
  }

  /**
   * Command: Import a submission.
   */
  public Effect<Done> importSubmission(Submission submission) {
    if (currentState() != null && currentState().submissionId() != null && 
        !currentState().submissionId().isEmpty()) {
      logger.warn("Submission {} already exists", entityId);
      return effects().error("Submission already exists");
    }

    var event = new SubmissionEvent.SubmissionImported(submission);
    return effects()
        .persist(event)
        .thenReply(newState -> Done.getInstance());
  }

  /**
   * Command: Get the submission.
   */
  public Effect<Submission> getSubmission() {
    return effects().reply(currentState());
  }

  @Override
  public Submission applyEvent(SubmissionEvent event) {
    return switch (event) {
      case SubmissionEvent.SubmissionImported evt -> evt.submission();
    };
  }

  /**
   * Domain events for SubmissionsEntity.
   */
  public sealed interface SubmissionEvent {
    @akka.javasdk.annotations.TypeName("submission-imported")
    record SubmissionImported(Submission submission) implements SubmissionEvent {}
  }
}

