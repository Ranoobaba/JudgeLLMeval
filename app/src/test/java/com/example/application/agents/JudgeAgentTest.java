package com.example.application.agents;

import com.example.domain.Evaluation;
import com.example.domain.EvaluationRequest;
import com.example.domain.EvaluationResponse;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for JudgeAgent.
 * Note: These tests require OPENAI_API_KEY to be set in the environment.
 */
public class JudgeAgentTest {

  @Test
  public void testEvaluationRequestStructure() {
    // Test that EvaluationRequest can be constructed correctly
    EvaluationRequest request = new EvaluationRequest(
        "run-123",
        "sub-456",
        "queue-1",
        "q1",
        "judge-789",
        "What is 2+2?",
        "4",
        "Two plus two equals four",
        java.util.Map.of(),
        "Test Judge",
        "You are a helpful assistant.",
        "gpt-4o-mini",
        EvaluationRequest.IncludedFields.defaults(),
        java.util.List.of()
    );

    assertEquals("run-123", request.runId());
    assertEquals("sub-456", request.submissionId());
    assertEquals("queue-1", request.queueId());
    assertEquals("q1", request.questionTemplateId());
    assertEquals("judge-789", request.judgeId());
    assertEquals("What is 2+2?", request.questionText());
    assertEquals("4", request.answerChoice());
    assertEquals("Test Judge", request.judgeName());
  }

  @Test
  public void testEvaluationResponseVerdictValidation() {
    // Test that verdict must be one of the allowed values
    EvaluationResponse response1 = new EvaluationResponse(
        Evaluation.Verdict.PASS,
        "Correct answer"
    );
    assertEquals(Evaluation.Verdict.PASS, response1.verdict());

    EvaluationResponse response2 = new EvaluationResponse(
        Evaluation.Verdict.FAIL,
        "Incorrect answer"
    );
    assertEquals(Evaluation.Verdict.FAIL, response2.verdict());

    EvaluationResponse response3 = new EvaluationResponse(
        Evaluation.Verdict.INCONCLUSIVE,
        "Cannot determine"
    );
    assertEquals(Evaluation.Verdict.INCONCLUSIVE, response3.verdict());
  }

  // Integration test - requires API key
  // Uncomment and set OPENAI_API_KEY to run
  /*
  @Test
  public void testJudgeAgentIntegration() throws Exception {
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.out.println("Skipping integration test - OPENAI_API_KEY not set");
      return;
    }

    Config config = ConfigFactory.parseString(
        "akka.javasdk.agent.openai.api-key = \"" + apiKey + "\"\n" +
        "akka.javasdk.agent.openai.model-name = \"gpt-4o-mini\""
    );

    JudgeAgent agent = new JudgeAgent(config);

    EvaluationRequest request = new EvaluationRequest(
        "run-test",
        "sub-test",
        "queue-test",
        "q-test",
        "judge-test",
        "What is 2+2?",
        "4",
        "Two plus two equals four",
        java.util.Map.of(),
        "Test Judge",
        "You are a math teacher. Evaluate if the answer is correct.",
        "gpt-4o-mini",
        EvaluationRequest.IncludedFields.defaults(),
        java.util.List.of()
    );

    EvaluationResponse response = agent.evaluate(request);

    assertNotNull(response);
    assertNotNull(response.verdict());
    assertNotNull(response.reasoning());
    assertTrue(response.reasoning().length() > 0);
  }
  */
}

