package com.example.application.llm;

import com.example.domain.EvaluationResponse;
import com.example.domain.Evaluation;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * OpenAI implementation of LLMProvider.
 * Makes HTTP calls to OpenAI API and parses JSON responses.
 */
public class OpenAIProvider implements LLMProvider {

  private static final Logger logger = LoggerFactory.getLogger(OpenAIProvider.class);
  private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
  
  private final OkHttpClient httpClient;
  private final String apiKey;
  private final ObjectMapper objectMapper;

  public OpenAIProvider(String apiKey) {
    this.apiKey = apiKey;
    this.httpClient = new OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build();
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public EvaluationResponse evaluate(String systemPrompt, String userPrompt, String model) throws LLMException {
    if (apiKey == null || apiKey.isEmpty()) {
      throw new LLMException("OpenAI API key is not configured. Set OPENAI_API_KEY environment variable.");
    }

    try {
      // Build the request payload
      var requestBody = buildRequestBody(systemPrompt, userPrompt, model);
      String jsonBody = objectMapper.writeValueAsString(requestBody);

      // Create HTTP request
      Request request = new Request.Builder()
          .url(OPENAI_API_URL)
          .header("Authorization", "Bearer " + apiKey)
          .header("Content-Type", "application/json")
          .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
          .build();

      logger.debug("Calling OpenAI API with model: {}", model);

      // Execute request
      try (Response response = httpClient.newCall(request).execute()) {
        if (!response.isSuccessful()) {
          String errorBody = response.body() != null ? response.body().string() : "No error body";
          logger.error("OpenAI API error: {} - {}", response.code(), errorBody);
          throw new LLMException("OpenAI API error: " + response.code() + " - " + errorBody);
        }

        // Parse response
        String responseBody = response.body().string();
        OpenAIResponse openAIResponse = objectMapper.readValue(responseBody, OpenAIResponse.class);

        if (openAIResponse.choices == null || openAIResponse.choices.isEmpty()) {
          throw new LLMException("OpenAI API returned no choices");
        }

        String content = openAIResponse.choices.get(0).message.content;
        logger.debug("OpenAI response content: {}", content);

        // Parse JSON from content
        return parseEvaluationResponse(content);

      }
    } catch (IOException e) {
      logger.error("IO error calling OpenAI API", e);
      throw new LLMException("Failed to call OpenAI API: " + e.getMessage(), e);
    } catch (Exception e) {
      if (e instanceof LLMException) {
        throw e;
      }
      logger.error("Unexpected error calling OpenAI API", e);
      throw new LLMException("Unexpected error: " + e.getMessage(), e);
    }
  }

  /**
   * Builds the request body for OpenAI API.
   */
  private Map<String, Object> buildRequestBody(String systemPrompt, String userPrompt, String model) {
    return Map.of(
        "model", model,
        "messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userPrompt)
        ),
        "response_format", Map.of("type", "json_object"),
        "temperature", 0.0
    );
  }

  /**
   * Parses the JSON response from OpenAI into EvaluationResponse.
   * Expects JSON format: {"verdict": "pass|fail|inconclusive", "reasoning": "..."}
   */
  private EvaluationResponse parseEvaluationResponse(String jsonContent) throws LLMException {
    try {
      // Try to parse as JSON
      Map<String, Object> json = objectMapper.readValue(jsonContent, Map.class);
      
      String verdictStr = (String) json.get("verdict");
      String reasoning = (String) json.get("reasoning");

      if (verdictStr == null) {
        throw new LLMException("Missing 'verdict' field in LLM response");
      }

      if (reasoning == null) {
        reasoning = "";
      }

      // Parse and validate verdict
      Evaluation.Verdict verdict = EvaluationResponse.parseVerdict(verdictStr);

      return new EvaluationResponse(verdict, reasoning);

    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      // If JSON parsing fails, try to extract JSON from markdown code blocks
      String extractedJson = extractJsonFromMarkdown(jsonContent);
      if (extractedJson != null) {
        return parseEvaluationResponse(extractedJson);
      }
      
      logger.error("Failed to parse LLM response as JSON: {}", jsonContent);
      throw new LLMException("Invalid JSON response from LLM: " + e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      throw new LLMException("Invalid verdict in LLM response: " + e.getMessage(), e);
    }
  }

  /**
   * Extracts JSON from markdown code blocks if present.
   */
  private String extractJsonFromMarkdown(String content) {
    // Try to extract JSON from ```json ... ``` blocks
    int start = content.indexOf("```json");
    if (start == -1) {
      start = content.indexOf("```");
    }
    if (start != -1) {
      start = content.indexOf('\n', start);
      if (start != -1) {
        start++; // Skip the newline
        int end = content.indexOf("```", start);
        if (end != -1) {
          return content.substring(start, end).trim();
        }
      }
    }
    return null;
  }

  /**
   * OpenAI API response structure.
   */
  private static class OpenAIResponse {
    @JsonProperty("choices")
    List<Choice> choices;

    static class Choice {
      @JsonProperty("message")
      Message message;
    }

    static class Message {
      @JsonProperty("content")
      String content;
    }
  }
}

