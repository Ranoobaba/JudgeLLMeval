package com.example.domain.views;

import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import com.example.domain.Evaluation;
import com.example.domain.entities.EvaluationEntity;

import java.time.Instant;
import java.util.Collection;

/**
 * View for querying evaluations with filters.
 * Supports filtering by queueId, judgeId, questionTemplateId, and verdict.
 */
@Component(id = "evaluations-view")
public class EvaluationsView extends View {

  public record EvaluationEntry(
      String evaluationId,
      String runId,
      String submissionId,
      String queueId,
      String questionTemplateId,
      String judgeId,
      String verdict,
      String reasoning,
      Instant evaluatedAt
  ) {}

  public record EvaluationsResult(Collection<EvaluationEntry> evaluations) {}

  @Consume.FromEventSourcedEntity(EvaluationEntity.class)
  public static class EvaluationsUpdater extends TableUpdater<EvaluationEntry> {

    public Effect<EvaluationEntry> onEvent(EvaluationEntity.EvaluationEvent event) {
      return switch (event) {
        case EvaluationEntity.EvaluationEvent.EvaluationRecorded recorded -> {
          Evaluation eval = recorded.evaluation();
          yield effects().updateRow(new EvaluationEntry(
              eval.evaluationId(),
              eval.runId(),
              eval.submissionId(),
              eval.queueId(),
              eval.questionTemplateId(),
              eval.judgeId(),
              eval.verdict().name(),
              eval.reasoning(),
              eval.evaluatedAt()
          ));
        }
      };
    }
  }

  @Query("SELECT * as evaluations FROM evaluations_view")
  public QueryEffect<EvaluationsResult> getAllEvaluations() {
    return queryResult();
  }

  @Query("SELECT * as evaluations FROM evaluations_view WHERE queueId = :queueId")
  public QueryEffect<EvaluationsResult> getEvaluationsByQueue(String queueId) {
    return queryResult();
  }

  @Query("SELECT * as evaluations FROM evaluations_view WHERE judgeId = :judgeId")
  public QueryEffect<EvaluationsResult> getEvaluationsByJudge(String judgeId) {
    return queryResult();
  }

  @Query("SELECT * as evaluations FROM evaluations_view WHERE questionTemplateId = :questionTemplateId")
  public QueryEffect<EvaluationsResult> getEvaluationsByQuestion(String questionTemplateId) {
    return queryResult();
  }

  @Query("SELECT * as evaluations FROM evaluations_view WHERE verdict = :verdict")
  public QueryEffect<EvaluationsResult> getEvaluationsByVerdict(String verdict) {
    return queryResult();
  }

  @Query("SELECT * as evaluations FROM evaluations_view WHERE queueId = :queueId AND judgeId = :judgeId AND questionTemplateId = :questionTemplateId AND verdict = :verdict")
  public QueryEffect<EvaluationsResult> getEvaluationsWithFilters(EvaluationFilters filters) {
    return queryResult();
  }

  public record EvaluationFilters(
      String queueId,
      String judgeId,
      String questionTemplateId,
      String verdict
  ) {}
}

