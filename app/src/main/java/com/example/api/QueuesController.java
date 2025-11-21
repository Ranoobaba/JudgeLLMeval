package com.example.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.AbstractHttpEndpoint;
import com.example.domain.views.QueuesView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP endpoint for queue management.
 * Lists queues and questions per queue.
 */
@HttpEndpoint("/api/queues")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class QueuesController extends AbstractHttpEndpoint {

  private static final Logger logger = LoggerFactory.getLogger(QueuesController.class);
  private final ComponentClient componentClient;

  public QueuesController(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  /**
   * GET /api/queues
   * List all queues.
   */
  @Get
  public QueuesView.QueuesResult getAllQueues() {
    try {
      return componentClient
          .forView()
          .method(QueuesView::getAllQueues)
          .invoke();
    } catch (Exception e) {
      logger.error("Failed to get queues", e);
      throw new RuntimeException("Failed to get queues: " + e.getMessage(), e);
    }
  }

  /**
   * GET /api/queues/{queueId}/questions
   * List questions for a specific queue.
   */
  @Get("/{queueId}/questions")
  public com.example.domain.views.QuestionsView.QuestionsResult getQuestions(String queueId) {
    try {
      return componentClient
          .forView()
          .method(com.example.domain.views.QuestionsView::getQuestionsByQueue)
          .invoke(queueId);
    } catch (Exception e) {
      logger.error("Failed to get questions for queue {}", queueId, e);
      throw new RuntimeException("Failed to get questions: " + e.getMessage(), e);
    }
  }
}

