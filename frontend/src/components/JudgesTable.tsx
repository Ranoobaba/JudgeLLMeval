import type { Judge } from '../types';
import { api } from '../api/client';
import { useMutation, useQueryClient } from '@tanstack/react-query';

interface JudgesTableProps {
  judges: Judge[];
}

/**
 * Table component for displaying and managing judges.
 */
export function JudgesTable({ judges }: JudgesTableProps) {
  const queryClient = useQueryClient();

  const toggleActiveMutation = useMutation({
    mutationFn: ({ judgeId, active }: { judgeId: string; active: boolean }) =>
      api.setJudgeActive(judgeId, active),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['judges'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (judgeId: string) => api.deleteJudge(judgeId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['judges'] });
    },
  });

  const handleToggleActive = (judgeId: string, currentActive: boolean) => {
    toggleActiveMutation.mutate({ judgeId, active: !currentActive });
  };

  const handleDelete = (judgeId: string) => {
    if (confirm('Are you sure you want to delete this judge?')) {
      deleteMutation.mutate(judgeId);
    }
  };

  if (judges.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        No judges found. Create one to get started.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Name
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Model
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Active
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Actions
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {judges.map((judge) => (
            <tr key={judge.judgeId} className="hover:bg-gray-50">
              <td className="px-6 py-4 whitespace-nowrap">
                <div className="text-sm font-medium text-gray-900">{judge.name}</div>
                <div className="text-sm text-gray-500 truncate max-w-md">
                  {judge.systemPrompt.substring(0, 100)}...
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {judge.targetModel}
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <button
                  onClick={() => handleToggleActive(judge.judgeId, judge.active)}
                  className={`px-3 py-1 rounded-full text-xs font-medium ${
                    judge.active
                      ? 'bg-green-100 text-green-800'
                      : 'bg-gray-100 text-gray-800'
                  }`}
                >
                  {judge.active ? 'Active' : 'Inactive'}
                </button>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button
                  onClick={() => handleDelete(judge.judgeId)}
                  className="text-red-600 hover:text-red-900"
                  disabled={deleteMutation.isPending}
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

