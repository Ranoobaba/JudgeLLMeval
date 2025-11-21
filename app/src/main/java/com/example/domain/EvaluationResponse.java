package com.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Output type from JudgeAgent.
 * Contains the structured verdict and reasoning from the LLM evaluation.
 */
public record EvaluationResponse(
    Evaluation.Verdict verdict,
    String reasoning
) {
  @JsonCreator
  public EvaluationResponse(
      @JsonProperty("verdict") Evaluation.Verdict verdict,
      @JsonProperty("reasoning") String reasoning
  ) {
    this.verdict = verdict;
    this.reasoning = reasoning != null ? reasoning : "";
  }

  /**
   * Parses a verdict string (case-insensitive) to Verdict enum.
   * Throws IllegalArgumentException if the string doesn't match a valid verdict.
   */
  public static Evaluation.Verdict parseVerdict(String verdictStr) {
    if (verdictStr == null) {
      throw new IllegalArgumentException("Verdict cannot be null");
    }
    String normalized = verdictStr.trim().toUpperCase();
    return switch (normalized) {
      case "PASS" -> Evaluation.Verdict.PASS;
      case "FAIL" -> Evaluation.Verdict.FAIL;
      case "INCONCLUSIVE" -> Evaluation.Verdict.INCONCLUSIVE;
      default -> throw new IllegalArgumentException(
          "Invalid verdict: " + verdictStr + ". Must be one of: pass, fail, inconclusive"
      );
    };
  }
}

