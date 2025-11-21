import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';

interface EvaluationFiltersProps {
  filters: {
    queueId?: string;
    judgeId?: string;
    questionTemplateId?: string;
    verdict?: string;
  };
  onFiltersChange: (filters: {
    queueId?: string;
    judgeId?: string;
    questionTemplateId?: string;
    verdict?: string;
  }) => void;
}

/**
 * Filter component for evaluations.
 */
export function EvaluationFilters({ filters, onFiltersChange }: EvaluationFiltersProps) {
  const { data: queuesData } = useQuery({
    queryKey: ['queues'],
    queryFn: () => api.getQueues(),
  });

  const { data: judgesData } = useQuery({
    queryKey: ['judges'],
    queryFn: () => api.getJudges(),
  });

  const handleFilterChange = (key: string, value: string) => {
    onFiltersChange({
      ...filters,
      [key]: value || undefined,
    });
  };

  const handleClearFilters = () => {
    onFiltersChange({});
  };

  return (
    <div className="bg-white rounded-lg shadow p-6 mb-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-900">Filters</h3>
        <button
          onClick={handleClearFilters}
          className="text-sm text-blue-600 hover:text-blue-800"
        >
          Clear All
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Queue
          </label>
          <select
            value={filters.queueId || ''}
            onChange={(e) => handleFilterChange('queueId', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="">All Queues</option>
            {queuesData?.queues?.map((queue) => (
              <option key={queue.queueId} value={queue.queueId}>
                {queue.queueId}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Judge
          </label>
          <select
            value={filters.judgeId || ''}
            onChange={(e) => handleFilterChange('judgeId', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="">All Judges</option>
            {judgesData?.judges?.map((judge: any) => (
              <option key={judge.judgeId} value={judge.judgeId}>
                {judge.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Question
          </label>
          <input
            type="text"
            value={filters.questionTemplateId || ''}
            onChange={(e) => handleFilterChange('questionTemplateId', e.target.value)}
            placeholder="Question ID"
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Verdict
          </label>
          <select
            value={filters.verdict || ''}
            onChange={(e) => handleFilterChange('verdict', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="">All Verdicts</option>
            <option value="PASS">Pass</option>
            <option value="FAIL">Fail</option>
            <option value="INCONCLUSIVE">Inconclusive</option>
          </select>
        </div>
      </div>
    </div>
  );
}

