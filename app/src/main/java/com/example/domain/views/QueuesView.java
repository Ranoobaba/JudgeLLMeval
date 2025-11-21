package com.example.domain.views;

import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import com.example.domain.Submission;
import com.example.domain.entities.SubmissionsEntity;

import java.util.Collection;
import java.util.List;

/**
 * View for listing all queues.
 * Extracts unique queueIds from submissions.
 */
@Component(id = "queues-view")
public class QueuesView extends View {

  public record QueueEntry(String queueId) {}

  public record QueuesResult(Collection<QueueEntry> queues) {}

  @Consume.FromEventSourcedEntity(SubmissionsEntity.class)
  public static class QueuesUpdater extends TableUpdater<QueueEntry> {

    public Effect<QueueEntry> onEvent(SubmissionsEntity.SubmissionEvent event) {
      return switch (event) {
        case SubmissionsEntity.SubmissionEvent.SubmissionImported imported -> {
          String queueId = imported.submission().queueId();
          if (queueId != null && !queueId.isEmpty()) {
            yield effects().updateRow(new QueueEntry(queueId));
          } else {
            yield effects().ignore();
          }
        }
      };
    }
  }

  @Query("SELECT * as queues FROM queues_view")
  public QueryEffect<QueuesResult> getAllQueues() {
    return queryResult();
  }

  @Query("SELECT * as queues FROM queues_view WHERE queueId = :queueId")
  public QueryEffect<QueuesResult> getQueue(String queueId) {
    return queryResult();
  }
}

