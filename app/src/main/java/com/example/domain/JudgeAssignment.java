package com.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * Represents the assignment of judges to questions within a queue.
 * This maps which judges should evaluate which questions.
 */
public record JudgeAssignment(
    String queueId,
    String questionTemplateId,
    Set<String> judgeIds
) {
  @JsonCreator
  public JudgeAssignment(
      @JsonProperty("queueId") String queueId,
      @JsonProperty("questionTemplateId") String questionTemplateId,
      @JsonProperty("judgeIds") Set<String> judgeIds
  ) {
    this.queueId = queueId;
    this.questionTemplateId = questionTemplateId;
    this.judgeIds = judgeIds != null ? judgeIds : Set.of();
  }

  public JudgeAssignment withJudgeIds(Set<String> judgeIds) {
    return new JudgeAssignment(queueId, questionTemplateId, judgeIds);
  }
}

