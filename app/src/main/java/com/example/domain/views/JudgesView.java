package com.example.domain.views;

import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import com.example.domain.Judge;
import com.example.domain.entities.JudgeEntity;

import java.util.Collection;

/**
 * View for listing all judges.
 * Consumes judge events and maintains a list of active judges.
 */
@Component(id = "judges-view")
public class JudgesView extends View {

  public record JudgeEntry(
      String judgeId,
      String name,
      String systemPrompt,
      String targetModel,
      boolean active
  ) {}

  public record JudgesResult(Collection<JudgeEntry> judges) {}

  @Consume.FromEventSourcedEntity(JudgeEntity.class)
  public static class JudgesUpdater extends TableUpdater<JudgeEntry> {

    public Effect<JudgeEntry> onEvent(JudgeEntity.JudgeEvent event) {
      return switch (event) {
        case JudgeEntity.JudgeEvent.JudgeCreated created -> {
          Judge judge = created.judge();
          yield effects().updateRow(new JudgeEntry(
              judge.judgeId(),
              judge.name(),
              judge.systemPrompt(),
              judge.targetModel(),
              judge.active()
          ));
        }
        case JudgeEntity.JudgeEvent.JudgeUpdated updated -> {
          Judge judge = updated.judge();
          yield effects().updateRow(new JudgeEntry(
              judge.judgeId(),
              judge.name(),
              judge.systemPrompt(),
              judge.targetModel(),
              judge.active()
          ));
        }
        case JudgeEntity.JudgeEvent.JudgeDeleted deleted -> {
          yield effects().deleteRow();
        }
      };
    }
  }

  @Query("SELECT * as judges FROM judges_view")
  public QueryEffect<JudgesResult> getAllJudges() {
    return queryResult();
  }

  @Query("SELECT * as judges FROM judges_view WHERE active = true")
  public QueryEffect<JudgesResult> getActiveJudges() {
    return queryResult();
  }

  @Query("SELECT * as judges FROM judges_view WHERE judgeId = :judgeId")
  public QueryEffect<JudgesResult> getJudge(String judgeId) {
    return queryResult();
  }
}

