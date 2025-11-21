import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';

interface JudgeAssignmentMatrixProps {
  queueId: string;
  questions: Array<{ queueId: string; questionTemplateId: string; questionText: string }>;
}

/**
 * Matrix component for assigning judges to questions.
 * Shows a checkbox grid: questions × judges.
 */
export function JudgeAssignmentMatrix({ queueId, questions }: JudgeAssignmentMatrixProps) {
  const queryClient = useQueryClient();

  const { data: judgesData } = useQuery({
    queryKey: ['judges'],
    queryFn: () => api.getJudges(),
  });

  const { data: assignmentsData, isLoading: assignmentsLoading } = useQuery({
    queryKey: ['assignments', queueId],
    queryFn: async () => {
      const assignments: Record<string, string[]> = {};
      for (const question of questions) {
        try {
          const assignment = await api.getJudgeAssignments(queueId, question.questionTemplateId);
          assignments[question.questionTemplateId] = assignment.judgeIds || [];
        } catch (e) {
          assignments[question.questionTemplateId] = [];
        }
      }
      return assignments;
    },
    enabled: questions.length > 0,
  });

  const updateAssignmentMutation = useMutation({
    mutationFn: async ({
      questionTemplateId,
      judgeIds,
    }: {
      questionTemplateId: string;
      judgeIds: string[];
    }) => {
      return api.createJudgeAssignment(queueId, questionTemplateId, judgeIds);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['assignments', queueId] });
    },
  });

  const handleToggle = (questionTemplateId: string, judgeId: string) => {
    const currentJudgeIds = assignmentsData?.[questionTemplateId] || [];
    const newJudgeIds = currentJudgeIds.includes(judgeId)
      ? currentJudgeIds.filter((id) => id !== judgeId)
      : [...currentJudgeIds, judgeId];

    updateAssignmentMutation.mutate({ questionTemplateId, judgeIds: newJudgeIds });
  };

  if (!judgesData || !judgesData.judges) {
    return <div className="text-gray-500">Loading judges...</div>;
  }

  const activeJudges = judgesData.judges.filter((j: any) => j.active);

  if (activeJudges.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        No active judges found. Create and activate judges first.
      </div>
    );
  }

  if (assignmentsLoading) {
    return <div className="text-gray-500">Loading assignments...</div>;
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Question
            </th>
            {activeJudges.map((judge: any) => (
              <th
                key={judge.judgeId}
                className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                {judge.name}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {questions.map((question) => (
            <tr key={question.questionTemplateId} className="hover:bg-gray-50">
              <td className="px-4 py-3">
                <div className="text-sm font-medium text-gray-900">
                  {question.questionTemplateId}
                </div>
                <div className="text-xs text-gray-500 truncate max-w-xs">
                  {question.questionText}
                </div>
              </td>
              {activeJudges.map((judge: any) => {
                const isAssigned =
                  assignmentsData?.[question.questionTemplateId]?.includes(judge.judgeId) ||
                  false;
                return (
                  <td key={judge.judgeId} className="px-4 py-3 text-center">
                    <input
                      type="checkbox"
                      checked={isAssigned}
                      onChange={() => handleToggle(question.questionTemplateId, judge.judgeId)}
                      disabled={updateAssignmentMutation.isPending}
                      className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                    />
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

