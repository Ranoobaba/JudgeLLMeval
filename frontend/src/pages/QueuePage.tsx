import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import { QuestionList } from '../components/QuestionList';
import { JudgeAssignmentMatrix } from '../components/JudgeAssignmentMatrix';
import { RunProgressBar } from '../components/RunProgressBar';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';

/**
 * Queue page - Question list, judge assignment, "Run AI Judges".
 */
export function QueuePage() {
  const { queueId } = useParams<{ queueId: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [activeRunId, setActiveRunId] = useState<string | null>(null);

  const { data: questionsData, isLoading: questionsLoading, error: questionsError } = useQuery({
    queryKey: ['questions', queueId],
    queryFn: () => api.getQuestions(queueId!),
    enabled: !!queueId,
  });

  const startRunMutation = useMutation({
    mutationFn: () => api.startRun(queueId!),
    onSuccess: (runId) => {
      setActiveRunId(runId);
      queryClient.invalidateQueries({ queryKey: ['runs'] });
    },
  });

  const handleStartRun = () => {
    if (confirm('Start evaluation run for this queue? This may take a while.')) {
      startRunMutation.mutate();
    }
  };

  const handleRunComplete = () => {
    setActiveRunId(null);
    queryClient.invalidateQueries({ queryKey: ['evaluations'] });
    alert('Evaluation run completed! Check the Results page to see the results.');
  };

  if (!queueId) {
    return <div>Invalid queue ID</div>;
  }

  return (
    <div>
      <div className="mb-8">
        <button
          onClick={() => navigate('/')}
          className="text-blue-600 hover:text-blue-800 mb-4"
        >
          ← Back to Queues
        </button>
        <h1 className="text-3xl font-bold text-gray-900">Queue: {queueId}</h1>
      </div>

      {questionsLoading && <LoadingSpinner />}
      {questionsError && <ErrorMessage message={questionsError.message} />}

      {questionsData && (
        <>
          <div className="bg-white rounded-lg shadow p-6 mb-8">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Questions</h2>
            <QuestionList questions={questionsData.questions || []} />
          </div>

          <div className="bg-white rounded-lg shadow p-6 mb-8">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              Judge Assignments
            </h2>
            <p className="text-sm text-gray-600 mb-4">
              Select which judges should evaluate each question.
            </p>
            <JudgeAssignmentMatrix
              queueId={queueId}
              questions={questionsData.questions || []}
            />
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Run Evaluations</h2>
            
            {activeRunId ? (
              <div className="space-y-4">
                <p className="text-sm text-gray-600">Evaluation run in progress...</p>
                <RunProgressBar runId={activeRunId} onComplete={handleRunComplete} />
              </div>
            ) : (
              <div className="space-y-4">
                <p className="text-sm text-gray-600">
                  Start an evaluation run to have all assigned judges evaluate all questions in this queue.
                </p>
                <button
                  onClick={handleStartRun}
                  disabled={startRunMutation.isPending || (questionsData.questions?.length || 0) === 0}
                  className="px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed font-medium"
                >
                  {startRunMutation.isPending ? 'Starting...' : 'Run AI Judges'}
                </button>
                {startRunMutation.error && (
                  <div className="text-sm text-red-600">
                    {startRunMutation.error.message}
                  </div>
                )}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
