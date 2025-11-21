package com.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Represents an uploaded submission containing questions and answers.
 * Submissions are imported from JSON files uploaded by users.
 */
public record Submission(
    String submissionId,
    String queueId,
    Map<String, QuestionAnswer> questions
) {
  /**
   * Represents a question and its answer within a submission.
   */
  public record QuestionAnswer(
      String questionTemplateId,
      String questionText,
      String answerChoice,
      String answerReasoning,
      Map<String, Object> metadata
  ) {
    @JsonCreator
    public QuestionAnswer(
        @JsonProperty("questionTemplateId") String questionTemplateId,
        @JsonProperty("questionText") String questionText,
        @JsonProperty("answerChoice") String answerChoice,
        @JsonProperty("answerReasoning") String answerReasoning,
        @JsonProperty("metadata") Map<String, Object> metadata
    ) {
      this.questionTemplateId = questionTemplateId;
      this.questionText = questionText;
      this.answerChoice = answerChoice;
      this.answerReasoning = answerReasoning;
      this.metadata = metadata != null ? metadata : Map.of();
    }
  }

  @JsonCreator
  public Submission(
      @JsonProperty("submissionId") String submissionId,
      @JsonProperty("queueId") String queueId,
      @JsonProperty("questions") Map<String, QuestionAnswer> questions
  ) {
    this.submissionId = submissionId != null ? submissionId : java.util.UUID.randomUUID().toString();
    this.queueId = queueId;
    this.questions = questions != null ? questions : Map.of();
  }
}

