package com.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Represents an individual evaluation result.
 * Stores the verdict and reasoning from a judge's evaluation of a submission's answer.
 */
public record Evaluation(
    String evaluationId,
    String runId,
    String submissionId,
    String queueId,
    String questionTemplateId,
    String judgeId,
    Verdict verdict,
    String reasoning,
    Instant evaluatedAt
) {
  /**
   * Possible verdict values from a judge evaluation.
   */
  public enum Verdict {
    PASS,
    FAIL,
    INCONCLUSIVE
  }

  @JsonCreator
  public Evaluation(
      @JsonProperty("evaluationId") String evaluationId,
      @JsonProperty("runId") String runId,
      @JsonProperty("submissionId") String submissionId,
      @JsonProperty("queueId") String queueId,
      @JsonProperty("questionTemplateId") String questionTemplateId,
      @JsonProperty("judgeId") String judgeId,
      @JsonProperty("verdict") Verdict verdict,
      @JsonProperty("reasoning") String reasoning,
      @JsonProperty("evaluatedAt") Instant evaluatedAt
  ) {
    this.evaluationId = evaluationId != null ? evaluationId : java.util.UUID.randomUUID().toString();
    this.runId = runId;
    this.submissionId = submissionId;
    this.queueId = queueId;
    this.questionTemplateId = questionTemplateId;
    this.judgeId = judgeId;
    this.verdict = verdict;
    this.reasoning = reasoning;
    this.evaluatedAt = evaluatedAt != null ? evaluatedAt : Instant.now();
  }

  public Evaluation withVerdict(Verdict verdict) {
    return new Evaluation(evaluationId, runId, submissionId, queueId, questionTemplateId, judgeId, verdict, reasoning, evaluatedAt);
  }

  public Evaluation withReasoning(String reasoning) {
    return new Evaluation(evaluationId, runId, submissionId, queueId, questionTemplateId, judgeId, verdict, reasoning, evaluatedAt);
  }
}

