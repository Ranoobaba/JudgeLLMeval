package com.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Represents an evaluation run progress.
 * Tracks how many evaluations are planned, completed, and failed for a run.
 */
public record Run(
    String runId,
    String queueId,
    RunStatus status,
    int plannedCount,
    int completedCount,
    int failedCount,
    Instant startedAt,
    Instant completedAt
) {
  /**
   * Status of an evaluation run.
   */
  public enum RunStatus {
    RUNNING,
    COMPLETED,
    FAILED
  }

  @JsonCreator
  public Run(
      @JsonProperty("runId") String runId,
      @JsonProperty("queueId") String queueId,
      @JsonProperty("status") RunStatus status,
      @JsonProperty("plannedCount") int plannedCount,
      @JsonProperty("completedCount") int completedCount,
      @JsonProperty("failedCount") int failedCount,
      @JsonProperty("startedAt") Instant startedAt,
      @JsonProperty("completedAt") Instant completedAt
  ) {
    this.runId = runId != null ? runId : java.util.UUID.randomUUID().toString();
    this.queueId = queueId;
    this.status = status != null ? status : RunStatus.RUNNING;
    this.plannedCount = plannedCount;
    this.completedCount = completedCount;
    this.failedCount = failedCount;
    this.startedAt = startedAt != null ? startedAt : Instant.now();
    this.completedAt = completedAt;
  }

  public Run withStatus(RunStatus status) {
    return new Run(runId, queueId, status, plannedCount, completedCount, failedCount, startedAt, completedAt);
  }

  public Run withCompletedCount(int completedCount) {
    return new Run(runId, queueId, status, plannedCount, completedCount, failedCount, startedAt, completedAt);
  }

  public Run withFailedCount(int failedCount) {
    return new Run(runId, queueId, status, plannedCount, completedCount, failedCount, startedAt, completedAt);
  }

  public Run withCompletedAt(Instant completedAt) {
    return new Run(runId, queueId, status, plannedCount, completedCount, failedCount, startedAt, completedAt);
  }

  public boolean isComplete() {
    return status == RunStatus.COMPLETED || status == RunStatus.FAILED;
  }

  public int getTotalProcessed() {
    return completedCount + failedCount;
  }

  public double getProgressPercentage() {
    if (plannedCount == 0) {
      return 0.0;
    }
    return (double) getTotalProcessed() / plannedCount * 100.0;
  }
}

