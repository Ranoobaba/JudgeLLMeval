package com.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Input type for JudgeAgent.
 * Contains all information needed to evaluate a single (submission, question, judge) tuple.
 */
public record EvaluationRequest(
    String runId,
    String submissionId,
    String queueId,
    String questionTemplateId,
    String judgeId,
    // What is being judged
    String questionText,
    String answerChoice,
    String answerReasoning,
    Map<String, Object> metadata,
    // Judge config
    String judgeName,
    String judgeSystemPrompt,
    String targetModel,
    // Prompt shaping options
    IncludedFields includedFields,
    // Optional: attachment URLs or IDs
    List<String> attachmentUrls
) {
  /**
   * Controls which fields are included in the prompt sent to the judge.
   */
  public record IncludedFields(
      boolean includeQuestionText,
      boolean includeAnswerChoice,
      boolean includeAnswerReasoning,
      boolean includeMetadata
  ) {
    @JsonCreator
    public IncludedFields(
        @JsonProperty("includeQuestionText") boolean includeQuestionText,
        @JsonProperty("includeAnswerChoice") boolean includeAnswerChoice,
        @JsonProperty("includeAnswerReasoning") boolean includeAnswerReasoning,
        @JsonProperty("includeMetadata") boolean includeMetadata
    ) {
      this.includeQuestionText = includeQuestionText;
      this.includeAnswerChoice = includeAnswerChoice;
      this.includeAnswerReasoning = includeAnswerReasoning;
      this.includeMetadata = includeMetadata;
    }

    public static IncludedFields all() {
      return new IncludedFields(true, true, true, true);
    }

    public static IncludedFields defaults() {
      return new IncludedFields(true, true, true, false);
    }
  }

  @JsonCreator
  public EvaluationRequest(
      @JsonProperty("runId") String runId,
      @JsonProperty("submissionId") String submissionId,
      @JsonProperty("queueId") String queueId,
      @JsonProperty("questionTemplateId") String questionTemplateId,
      @JsonProperty("judgeId") String judgeId,
      @JsonProperty("questionText") String questionText,
      @JsonProperty("answerChoice") String answerChoice,
      @JsonProperty("answerReasoning") String answerReasoning,
      @JsonProperty("metadata") Map<String, Object> metadata,
      @JsonProperty("judgeName") String judgeName,
      @JsonProperty("judgeSystemPrompt") String judgeSystemPrompt,
      @JsonProperty("targetModel") String targetModel,
      @JsonProperty("includedFields") IncludedFields includedFields,
      @JsonProperty("attachmentUrls") List<String> attachmentUrls
  ) {
    this.runId = runId;
    this.submissionId = submissionId;
    this.queueId = queueId;
    this.questionTemplateId = questionTemplateId;
    this.judgeId = judgeId;
    this.questionText = questionText;
    this.answerChoice = answerChoice;
    this.answerReasoning = answerReasoning;
    this.metadata = metadata != null ? metadata : Map.of();
    this.judgeName = judgeName;
    this.judgeSystemPrompt = judgeSystemPrompt;
    this.targetModel = targetModel;
    this.includedFields = includedFields != null ? includedFields : IncludedFields.defaults();
    this.attachmentUrls = attachmentUrls != null ? attachmentUrls : List.of();
  }
}

