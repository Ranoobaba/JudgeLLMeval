package com.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an AI judge definition.
 * A judge contains a rubric (system prompt) and specifies which LLM model to use.
 */
public record Judge(
    String judgeId,
    String name,
    String systemPrompt,
    String targetModel,
    boolean active
) {
  @JsonCreator
  public Judge(
      @JsonProperty("judgeId") String judgeId,
      @JsonProperty("name") String name,
      @JsonProperty("systemPrompt") String systemPrompt,
      @JsonProperty("targetModel") String targetModel,
      @JsonProperty("active") boolean active
  ) {
    this.judgeId = judgeId != null ? judgeId : java.util.UUID.randomUUID().toString();
    this.name = name;
    this.systemPrompt = systemPrompt;
    this.targetModel = targetModel != null ? targetModel : "gpt-4o-mini";
    this.active = active;
  }

  public Judge withActive(boolean active) {
    return new Judge(judgeId, name, systemPrompt, targetModel, active);
  }

  public Judge withName(String name) {
    return new Judge(judgeId, name, systemPrompt, targetModel, active);
  }

  public Judge withSystemPrompt(String systemPrompt) {
    return new Judge(judgeId, name, systemPrompt, targetModel, active);
  }

  public Judge withTargetModel(String targetModel) {
    return new Judge(judgeId, name, systemPrompt, targetModel, active);
  }
}

