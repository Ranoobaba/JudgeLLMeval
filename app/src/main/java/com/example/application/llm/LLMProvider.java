package com.example.application.llm;

import com.example.domain.EvaluationResponse;

/**
 * Interface for LLM providers.
 * Abstracts LLM API calls for judge evaluations.
 */
public interface LLMProvider {
  
  /**
   * Evaluates a question and answer using the provided system prompt.
   * 
   * @param systemPrompt The judge's system prompt/rubric
   * @param userPrompt The question and answer to evaluate
   * @param model The model to use (e.g., "gpt-4o-mini")
   * @return EvaluationResponse with verdict and reasoning
   * @throws LLMException if the LLM call fails or returns invalid response
   */
  EvaluationResponse evaluate(String systemPrompt, String userPrompt, String model) throws LLMException;
}

