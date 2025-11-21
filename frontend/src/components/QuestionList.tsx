interface Question {
  queueId: string;
  questionTemplateId: string;
  questionText: string;
}

interface QuestionListProps {
  questions: Question[];
}

/**
 * Component for displaying a list of questions.
 */
export function QuestionList({ questions }: QuestionListProps) {
  if (questions.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        No questions found in this queue.
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {questions.map((question) => (
        <div
          key={question.questionTemplateId}
          className="p-4 border border-gray-200 rounded-lg hover:bg-gray-50"
        >
          <div className="font-medium text-gray-900 mb-1">
            Question: {question.questionTemplateId}
          </div>
          <div className="text-sm text-gray-600">{question.questionText}</div>
        </div>
      ))}
    </div>
  );
}

