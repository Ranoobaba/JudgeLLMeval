import type { Evaluation } from '../types';

interface EvaluationsTableProps {
  evaluations: Evaluation[];
}

/**
 * Table component for displaying evaluation results.
 */
export function EvaluationsTable({ evaluations }: EvaluationsTableProps) {
  if (evaluations.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        No evaluations found matching the filters.
      </div>
    );
  }

  const getVerdictColor = (verdict: string) => {
    switch (verdict) {
      case 'PASS':
        return 'bg-green-100 text-green-800';
      case 'FAIL':
        return 'bg-red-100 text-red-800';
      case 'INCONCLUSIVE':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Question
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Judge
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Verdict
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Reasoning
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Evaluated At
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {evaluations.map((evaluation) => (
            <tr key={evaluation.evaluationId} className="hover:bg-gray-50">
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {evaluation.questionTemplateId}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {evaluation.judgeId}
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <span
                  className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getVerdictColor(
                    evaluation.verdict
                  )}`}
                >
                  {evaluation.verdict}
                </span>
              </td>
              <td className="px-6 py-4 text-sm text-gray-500 max-w-md">
                <div className="truncate" title={evaluation.reasoning}>
                  {evaluation.reasoning}
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {new Date(evaluation.evaluatedAt).toLocaleString()}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

