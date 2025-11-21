package com.example.domain.entities;

import akka.Done;
import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.TypeName;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.example.domain.Run;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Event Sourced Entity for tracking evaluation run progress.
 * Tracks planned, completed, and failed evaluation counts.
 */
@Component(id = "runs")
public class RunEntity extends EventSourcedEntity<Run, RunEntity.RunEvent> {

  private static final Logger logger = LoggerFactory.getLogger(RunEntity.class);
  private final String entityId;

  public RunEntity(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public Run emptyState() {
    return null; // No initial state - run must be started first
  }

  /**
   * Command: Start a new evaluation run.
   */
  public Effect<Done> startRun(StartRunRequest request) {
    if (currentState() != null) {
      logger.warn("Run {} already exists", entityId);
      return effects().error("Run already exists");
    }

    var run = new Run(
        entityId,
        request.queueId(),
        Run.RunStatus.RUNNING,
        request.plannedCount(),
        0,
        0,
        Instant.now(),
        null
    );

    var event = new RunEvent.RunStarted(run);
    return effects()
        .persist(event)
        .thenReply(newState -> Done.getInstance());
  }

  public record StartRunRequest(String queueId, int plannedCount) {}

  /**
   * Command: Mark an evaluation as completed.
   */
  public Effect<Done> markCompleted() {
    if (currentState() == null) {
      return effects().error("Run not found");
    }

    int newCompletedCount = currentState().completedCount() + 1;
    Run.RunStatus newStatus = determineStatus(newCompletedCount, currentState().failedCount());
    Instant completedAt = (newStatus == Run.RunStatus.COMPLETED) ? Instant.now() : currentState().completedAt();

    var updatedRun = currentState()
        .withCompletedCount(newCompletedCount)
        .withStatus(newStatus)
        .withCompletedAt(completedAt);

    var event = new RunEvent.RunProgressUpdated(updatedRun);
    return effects()
        .persist(event)
        .thenReply(newState -> Done.getInstance());
  }

  /**
   * Command: Mark an evaluation as failed.
   */
  public Effect<Done> markFailed() {
    if (currentState() == null) {
      return effects().error("Run not found");
    }

    int newFailedCount = currentState().failedCount() + 1;
    Run.RunStatus newStatus = determineStatus(currentState().completedCount(), newFailedCount);
    Instant completedAt = (newStatus == Run.RunStatus.COMPLETED || newStatus == Run.RunStatus.FAILED) 
        ? Instant.now() 
        : currentState().completedAt();

    var updatedRun = currentState()
        .withFailedCount(newFailedCount)
        .withStatus(newStatus)
        .withCompletedAt(completedAt);

    var event = new RunEvent.RunProgressUpdated(updatedRun);
    return effects()
        .persist(event)
        .thenReply(newState -> Done.getInstance());
  }

  /**
   * Command: Get the run status.
   */
  public Effect<Run> getRun() {
    if (currentState() == null) {
      return effects().error("Run not found");
    }
    return effects().reply(currentState());
  }

  /**
   * Determine the run status based on progress.
   */
  private Run.RunStatus determineStatus(int completedCount, int failedCount) {
    int totalProcessed = completedCount + failedCount;
    if (currentState() == null) {
      return Run.RunStatus.RUNNING;
    }
    int plannedCount = currentState().plannedCount();
    
    if (totalProcessed >= plannedCount) {
      // All evaluations processed
      if (failedCount == 0) {
        return Run.RunStatus.COMPLETED;
      } else if (completedCount == 0) {
        return Run.RunStatus.FAILED;
      } else {
        return Run.RunStatus.COMPLETED; // Partial success is still completed
      }
    }
    return Run.RunStatus.RUNNING;
  }

  @Override
  public Run applyEvent(RunEvent event) {
    return switch (event) {
      case RunEvent.RunStarted evt -> evt.run();
      case RunEvent.RunProgressUpdated evt -> evt.run();
    };
  }

  /**
   * Domain events for RunEntity.
   */
  public sealed interface RunEvent {
    @TypeName("run-started")
    record RunStarted(Run run) implements RunEvent {}

    @TypeName("run-progress-updated")
    record RunProgressUpdated(Run run) implements RunEvent {}
  }
}

