package com.example.application.agents;

import com.example.domain.EvaluationRequest;
import com.example.domain.EvaluationResponse;
import com.example.application.llm.LLMException;
import com.example.application.llm.LLMProvider;
import com.example.application.llm.OpenAIProvider;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * JudgeAgent evaluates a single (submission, question, judge) tuple.
 * 
 * Responsibilities:
 * - Build prompt from EvaluationRequest
 * - Call LLM provider with structured JSON response format
 * - Parse and validate response (verdict must be pass|fail|inconclusive)
 * - Return EvaluationResponse
 * 
 * Does NOT:
 * - Loop over multiple submissions
 * - Decide which judges to run
 * - Handle persistence
 * 
 * This is a regular service class (not an Akka component) that can be injected
 * into workflows or other components via dependency injection.
 */
public class JudgeAgent {

  private static final Logger logger = LoggerFactory.getLogger(JudgeAgent.class);
  
  private final LLMProvider llmProvider;

  public JudgeAgent(Config config) {
    // Initialize LLM provider from config
    String apiKey = config.getString("akka.javasdk.agent.openai.api-key");
    this.llmProvider = new OpenAIProvider(apiKey);
  }

  /**
   * Evaluates a submission's answer to a question using the specified judge.
   * 
   * @param request The evaluation request containing question, answer, and judge config
   * @return EvaluationResponse with verdict and reasoning
   */
  public EvaluationResponse evaluate(EvaluationRequest request) throws LLMException {
    logger.info("Evaluating submission {} question {} with judge {}", 
        request.submissionId(), request.questionTemplateId(), request.judgeId());

    // Build the user prompt based on includedFields
    String userPrompt = buildUserPrompt(request);

    // Build system prompt from judge's rubric
    String systemPrompt = buildSystemPrompt(request);

    // Call LLM provider
    try {
      EvaluationResponse response = llmProvider.evaluate(
          systemPrompt,
          userPrompt,
          request.targetModel()
      );

      logger.debug("Evaluation result: verdict={}, reasoning length={}", 
          response.verdict(), response.reasoning().length());

      return response;

    } catch (LLMException e) {
      logger.error("LLM evaluation failed for submission {} question {} judge {}", 
          request.submissionId(), request.questionTemplateId(), request.judgeId(), e);
      throw e;
    }
  }

  /**
   * Builds the system prompt from the judge's rubric.
   * Includes instructions for JSON response format.
   */
  private String buildSystemPrompt(EvaluationRequest request) {
    StringBuilder prompt = new StringBuilder();
    
    prompt.append("You are an AI judge evaluating answers to questions.\n\n");
    prompt.append("Judge Name: ").append(request.judgeName()).append("\n\n");
    prompt.append("Evaluation Rubric:\n");
    prompt.append(request.judgeSystemPrompt()).append("\n\n");
    prompt.append("Your task is to evaluate the answer and provide a verdict.\n\n");
    prompt.append("RESPONSE FORMAT:\n");
    prompt.append("You MUST respond with valid JSON in the following format:\n");
    prompt.append("{\n");
    prompt.append("  \"verdict\": \"pass\" | \"fail\" | \"inconclusive\",\n");
    prompt.append("  \"reasoning\": \"Your explanation of the verdict (2-3 sentences)\"\n");
    prompt.append("}\n\n");
    prompt.append("Verdict Guidelines:\n");
    prompt.append("- \"pass\": The answer meets all criteria in the rubric\n");
    prompt.append("- \"fail\": The answer does not meet the criteria\n");
    prompt.append("- \"inconclusive\": You cannot determine a clear verdict (e.g., ambiguous question, missing context)\n\n");
    prompt.append("Be objective, fair, and consistent with the rubric.");

    return prompt.toString();
  }

  /**
   * Builds the user prompt from the evaluation request.
   * Respects the includedFields configuration to shape what's included.
   */
  private String buildUserPrompt(EvaluationRequest request) {
    StringBuilder prompt = new StringBuilder();

    prompt.append("Evaluate the following answer:\n\n");

    if (request.includedFields().includeQuestionText()) {
      prompt.append("QUESTION:\n");
      prompt.append(request.questionText()).append("\n\n");
    }

    if (request.includedFields().includeAnswerChoice() && request.answerChoice() != null) {
      prompt.append("ANSWER CHOICE:\n");
      prompt.append(request.answerChoice()).append("\n\n");
    }

    if (request.includedFields().includeAnswerReasoning() && request.answerReasoning() != null) {
      prompt.append("ANSWER REASONING:\n");
      prompt.append(request.answerReasoning()).append("\n\n");
    }

    if (request.includedFields().includeMetadata() && request.metadata() != null && !request.metadata().isEmpty()) {
      prompt.append("METADATA:\n");
      for (Map.Entry<String, Object> entry : request.metadata().entrySet()) {
        prompt.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
      }
      prompt.append("\n");
    }

    prompt.append("Provide your evaluation as JSON with 'verdict' and 'reasoning' fields.");

    return prompt.toString();
  }
}

