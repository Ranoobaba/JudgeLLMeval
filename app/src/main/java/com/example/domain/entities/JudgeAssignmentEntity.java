package com.example.domain.entities;

import akka.Done;
import akka.javasdk.annotations.Component;
import akka.javasdk.keyvalueentity.KeyValueEntity;
import akka.javasdk.keyvalueentity.KeyValueEntityContext;
import com.example.domain.JudgeAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Key Value Entity for managing judge assignments.
 * Maps questions to judges within a queue.
 * Uses composite key: "{queueId}|{questionTemplateId}"
 */
@Component(id = "judge-assignments")
public class JudgeAssignmentEntity extends KeyValueEntity<JudgeAssignment> {

  private static final Logger logger = LoggerFactory.getLogger(JudgeAssignmentEntity.class);
  private final String entityId;

  public JudgeAssignmentEntity(KeyValueEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public JudgeAssignment emptyState() {
    // Parse queueId and questionTemplateId from entityId (format: "queueId|questionTemplateId")
    String[] parts = entityId.split("\\|");
    if (parts.length != 2) {
      logger.error("Invalid entityId format: {}", entityId);
      return new JudgeAssignment("", "", Set.of());
    }
    return new JudgeAssignment(parts[0], parts[1], Set.of());
  }

  /**
   * Command: Set judge assignments for a question.
   */
  public Effect<JudgeAssignment> setAssignments(Set<String> judgeIds) {
    JudgeAssignment newState = currentState().withJudgeIds(judgeIds);
    return effects()
        .updateState(newState)
        .thenReply(newState);
  }

  /**
   * Command: Add a judge to the assignment.
   */
  public Effect<JudgeAssignment> addJudge(String judgeId) {
    Set<String> currentJudges = currentState().judgeIds();
    Set<String> newJudges = new java.util.HashSet<>(currentJudges);
    newJudges.add(judgeId);
    
    JudgeAssignment newState = currentState().withJudgeIds(newJudges);
    return effects()
        .updateState(newState)
        .thenReply(newState);
  }

  /**
   * Command: Remove a judge from the assignment.
   */
  public Effect<JudgeAssignment> removeJudge(String judgeId) {
    Set<String> currentJudges = currentState().judgeIds();
    Set<String> newJudges = new java.util.HashSet<>(currentJudges);
    newJudges.remove(judgeId);
    
    JudgeAssignment newState = currentState().withJudgeIds(newJudges);
    return effects()
        .updateState(newState)
        .thenReply(newState);
  }

  /**
   * Command: Get the judge assignments.
   */
  public Effect<JudgeAssignment> getAssignments() {
    return effects().reply(currentState());
  }

  /**
   * Command: Delete the assignment.
   */
  public Effect<Done> deleteAssignments() {
    return effects()
        .deleteEntity()
        .thenReply(Done.getInstance());
  }
}

