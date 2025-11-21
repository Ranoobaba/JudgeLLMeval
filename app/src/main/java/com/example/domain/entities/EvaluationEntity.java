package com.example.domain.entities;

import akka.Done;
import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.TypeName;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.example.domain.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Event Sourced Entity for storing evaluation results.
 * Each evaluation is stored with its full history.
 */
@Component(id = "evaluations")
public class EvaluationEntity extends EventSourcedEntity<Evaluation, EvaluationEntity.EvaluationEvent> {

  private static final Logger logger = LoggerFactory.getLogger(EvaluationEntity.class);
  private final String entityId;

  public EvaluationEntity(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public Evaluation emptyState() {
    return null; // No initial state - evaluation must be created first
  }

  /**
   * Command: Record an evaluation result.
   */
  public Effect<Done> recordEvaluation(RecordEvaluationRequest request) {
    if (currentState() != null) {
      logger.warn("Evaluation {} already exists", entityId);
      return effects().error("Evaluation already exists");
    }

    var evaluation = new Evaluation(
        entityId,
        request.runId(),
        request.submissionId(),
        request.queueId(),
        request.questionTemplateId(),
        request.judgeId(),
        request.verdict(),
        request.reasoning(),
        Instant.now()
    );

    var event = new EvaluationEvent.EvaluationRecorded(evaluation);
    return effects()
        .persist(event)
        .thenReply(newState -> Done.getInstance());
  }

  public record RecordEvaluationRequest(
      String runId,
      String submissionId,
      String queueId,
      String questionTemplateId,
      String judgeId,
      Evaluation.Verdict verdict,
      String reasoning
  ) {}

  /**
   * Command: Get the evaluation.
   */
  public Effect<Evaluation> getEvaluation() {
    if (currentState() == null) {
      return effects().error("Evaluation not found");
    }
    return effects().reply(currentState());
  }

  @Override
  public Evaluation applyEvent(EvaluationEvent event) {
    return switch (event) {
      case EvaluationEvent.EvaluationRecorded evt -> evt.evaluation();
    };
  }

  /**
   * Domain events for EvaluationEntity.
   */
  public sealed interface EvaluationEvent {
    @TypeName("evaluation-recorded")
    record EvaluationRecorded(Evaluation evaluation) implements EvaluationEvent {}
  }
}

