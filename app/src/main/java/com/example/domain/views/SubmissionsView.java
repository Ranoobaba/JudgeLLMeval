package com.example.domain.views;

import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import com.example.domain.Submission;
import com.example.domain.entities.SubmissionsEntity;

import java.util.Collection;

/**
 * View for querying submissions by queueId.
 * Allows finding submissions for a specific queue.
 */
@Component(id = "submissions-view")
public class SubmissionsView extends View {

  public record SubmissionEntry(
      String submissionId,
      String queueId
  ) {}

  public record SubmissionsResult(Collection<SubmissionEntry> submissions) {}

  @Consume.FromEventSourcedEntity(SubmissionsEntity.class)
  public static class SubmissionsUpdater extends TableUpdater<SubmissionEntry> {

    public Effect<SubmissionEntry> onEvent(SubmissionsEntity.SubmissionEvent event) {
      return switch (event) {
        case SubmissionsEntity.SubmissionEvent.SubmissionImported imported -> {
          Submission submission = imported.submission();
          String queueId = submission.queueId();
          if (queueId != null && !queueId.isEmpty()) {
            yield effects().updateRow(new SubmissionEntry(submission.submissionId(), queueId));
          } else {
            yield effects().ignore();
          }
        }
      };
    }
  }

  @Query("SELECT * as submissions FROM submissions_view WHERE queueId = :queueId")
  public QueryEffect<SubmissionsResult> getSubmissionsByQueue(String queueId) {
    return queryResult();
  }

  @Query("SELECT * as submissions FROM submissions_view WHERE submissionId = :submissionId")
  public QueryEffect<SubmissionsResult> getSubmission(String submissionId) {
    return queryResult();
  }
}

