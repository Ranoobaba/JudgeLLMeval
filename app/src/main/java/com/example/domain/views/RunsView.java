package com.example.domain.views;

import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import com.example.domain.Run;
import com.example.domain.entities.RunEntity;

import java.time.Instant;
import java.util.Collection;

/**
 * View for querying run status.
 * Tracks evaluation run progress.
 */
@Component(id = "runs-view")
public class RunsView extends View {

  public record RunEntry(
      String runId,
      String queueId,
      String status,
      int plannedCount,
      int completedCount,
      int failedCount,
      Instant startedAt,
      Instant completedAt
  ) {}

  public record RunsResult(Collection<RunEntry> runs) {}

  @Consume.FromEventSourcedEntity(RunEntity.class)
  public static class RunsUpdater extends TableUpdater<RunEntry> {

    public Effect<RunEntry> onEvent(RunEntity.RunEvent event) {
      return switch (event) {
        case RunEntity.RunEvent.RunStarted started -> {
          Run run = started.run();
          yield effects().updateRow(new RunEntry(
              run.runId(),
              run.queueId(),
              run.status().name(),
              run.plannedCount(),
              run.completedCount(),
              run.failedCount(),
              run.startedAt(),
              run.completedAt()
          ));
        }
        case RunEntity.RunEvent.RunProgressUpdated updated -> {
          Run run = updated.run();
          yield effects().updateRow(new RunEntry(
              run.runId(),
              run.queueId(),
              run.status().name(),
              run.plannedCount(),
              run.completedCount(),
              run.failedCount(),
              run.startedAt(),
              run.completedAt()
          ));
        }
      };
    }
  }

  @Query("SELECT * as runs FROM runs_view WHERE runId = :runId")
  public QueryEffect<RunsResult> getRun(String runId) {
    return queryResult();
  }

  @Query("SELECT * as runs FROM runs_view WHERE queueId = :queueId")
  public QueryEffect<RunsResult> getRunsByQueue(String queueId) {
    return queryResult();
  }

  @Query("SELECT * as runs FROM runs_view WHERE status = :status")
  public QueryEffect<RunsResult> getRunsByStatus(String status) {
    return queryResult();
  }
}

