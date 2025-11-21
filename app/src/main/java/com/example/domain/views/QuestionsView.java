package com.example.domain.views;

import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import com.example.domain.Submission;
import com.example.domain.entities.SubmissionsEntity;

import java.util.Collection;
import java.util.Map;

/**
 * View for listing questions per queue.
 * Extracts questions from submissions grouped by queueId.
 */
@Component(id = "questions-view")
public class QuestionsView extends View {

  public record QuestionEntry(
      String queueId,
      String questionTemplateId,
      String questionText
  ) {}

  public record QuestionsResult(Collection<QuestionEntry> questions) {}

  @Consume.FromEventSourcedEntity(SubmissionsEntity.class)
  public static class QuestionsUpdater extends TableUpdater<QuestionEntry> {

    public Effect<QuestionEntry> onEvent(SubmissionsEntity.SubmissionEvent event) {
      return switch (event) {
        case SubmissionsEntity.SubmissionEvent.SubmissionImported imported -> {
          Submission submission = imported.submission();
          String queueId = submission.queueId();
          
          if (queueId == null || queueId.isEmpty() || submission.questions() == null) {
            yield effects().ignore();
          } else {
            // Update row for each question in the submission
            // Process first question and return update effect
            // Note: For multiple questions, we'd need to handle them differently
            // For now, we'll update one row per event (the view will deduplicate)
            var firstEntry = submission.questions().entrySet().iterator().next();
            Submission.QuestionAnswer qa = firstEntry.getValue();
            String questionTemplateId = qa.questionTemplateId();
            String questionText = qa.questionText();
            
            if (questionTemplateId != null && !questionTemplateId.isEmpty()) {
              yield effects().updateRow(
                  new QuestionEntry(queueId, questionTemplateId, questionText)
              );
            } else {
              yield effects().ignore();
            }
          }
        }
      };
    }
  }

  @Query("SELECT * as questions FROM questions_view WHERE queueId = :queueId")
  public QueryEffect<QuestionsResult> getQuestionsByQueue(String queueId) {
    return queryResult();
  }

  @Query("SELECT * as questions FROM questions_view WHERE queueId = :queueId AND questionTemplateId = :questionTemplateId")
  public QueryEffect<QuestionsResult> getQuestion(GetQuestionRequest request) {
    return queryResult();
  }

  public record GetQuestionRequest(String queueId, String questionTemplateId) {}
}

