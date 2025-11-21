package com.example.domain.entities;

import akka.Done;
import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.TypeName;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.example.domain.Judge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event Sourced Entity for managing judges.
 * Supports CRUD operations with full event history.
 */
@Component(id = "judges")
public class JudgeEntity extends EventSourcedEntity<Judge, JudgeEntity.JudgeEvent> {

  private static final Logger logger = LoggerFactory.getLogger(JudgeEntity.class);
  private final String entityId;

  public JudgeEntity(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public Judge emptyState() {
    return null; // No initial state - judge must be created first
  }

  /**
   * Command: Create a new judge.
   */
  public Effect<Done> createJudge(Judge judge) {
    if (currentState() != null) {
      logger.warn("Judge {} already exists", entityId);
      return effects().error("Judge already exists");
    }

    var event = new JudgeEvent.JudgeCreated(judge);
    return effects()
        .persist(event)
        .thenReply(newState -> Done.getInstance());
  }

  /**
   * Command: Update judge name and system prompt.
   */
  public Effect<Done> updateJudge(UpdateJudgeRequest request) {
    if (currentState() == null) {
      return effects().error("Judge not found");
    }

    var updatedJudge = currentState()
        .withName(request.name())
        .withSystemPrompt(request.systemPrompt())
        .withTargetModel(request.targetModel());
    
    var event = new JudgeEvent.JudgeUpdated(updatedJudge);
    return effects()
        .persist(event)
        .thenReply(newState -> Done.getInstance());
  }

  public record UpdateJudgeRequest(String name, String systemPrompt, String targetModel) {}

  /**
   * Command: Toggle active flag.
   */
  public Effect<Done> setActive(boolean active) {
    if (currentState() == null) {
      return effects().error("Judge not found");
    }

    var updatedJudge = currentState().withActive(active);
    var event = new JudgeEvent.JudgeUpdated(updatedJudge);
    return effects()
        .persist(event)
        .thenReply(newState -> Done.getInstance());
  }

  /**
   * Command: Get the judge.
   */
  public Effect<Judge> getJudge() {
    if (currentState() == null) {
      return effects().error("Judge not found");
    }
    return effects().reply(currentState());
  }

  /**
   * Command: Delete the judge.
   */
  public Effect<Done> deleteJudge() {
    if (currentState() == null) {
      return effects().error("Judge not found");
    }

    var event = new JudgeEvent.JudgeDeleted();
    return effects()
        .persist(event)
        .deleteEntity()
        .thenReply(newState -> Done.getInstance());
  }

  @Override
  public Judge applyEvent(JudgeEvent event) {
    return switch (event) {
      case JudgeEvent.JudgeCreated evt -> evt.judge();
      case JudgeEvent.JudgeUpdated evt -> evt.judge();
      case JudgeEvent.JudgeDeleted evt -> null;
    };
  }

  /**
   * Domain events for JudgeEntity.
   */
  public sealed interface JudgeEvent {
    @TypeName("judge-created")
    record JudgeCreated(Judge judge) implements JudgeEvent {}

    @TypeName("judge-updated")
    record JudgeUpdated(Judge judge) implements JudgeEvent {}

    @TypeName("judge-deleted")
    record JudgeDeleted() implements JudgeEvent {}
  }
}

