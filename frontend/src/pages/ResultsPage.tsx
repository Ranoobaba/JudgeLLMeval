import { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import { EvaluationsTable } from '../components/EvaluationsTable';
import { EvaluationFilters } from '../components/EvaluationFilters';
import { PassRateChart } from '../components/PassRateChart';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import type { Evaluation } from '../types';

/**
 * Results page - Results table + filters + pass-rate + chart.
 */
export function ResultsPage() {
  const [filters, setFilters] = useState<{
    queueId?: string;
    judgeId?: string;
    questionTemplateId?: string;
    verdict?: string;
  }>({});

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['evaluations', filters],
    queryFn: () => api.getEvaluations(filters),
  });

  const evaluations = data?.evaluations || [];

  // Calculate pass rate
  const passRate = useMemo(() => {
    if (evaluations.length === 0) return 0;
    const passCount = evaluations.filter((e: Evaluation) => e.verdict === 'PASS').length;
    return (passCount / evaluations.length) * 100;
  }, [evaluations]);

  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Evaluation Results</h1>

      <EvaluationFilters filters={filters} onFiltersChange={setFilters} />

      {isLoading && <LoadingSpinner />}
      {error && <ErrorMessage message={error.message} onRetry={() => refetch()} />}

      {data && (
        <>
          {/* Pass Rate Summary */}
          <div className="bg-white rounded-lg shadow p-6 mb-8">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Summary</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="bg-blue-50 rounded-lg p-4">
                <div className="text-sm text-blue-600 font-medium">Total Evaluations</div>
                <div className="text-2xl font-bold text-blue-900">{evaluations.length}</div>
              </div>
              <div className="bg-green-50 rounded-lg p-4">
                <div className="text-sm text-green-600 font-medium">Pass Rate</div>
                <div className="text-2xl font-bold text-green-900">
                  {passRate.toFixed(1)}%
                </div>
              </div>
              <div className="bg-gray-50 rounded-lg p-4">
                <div className="text-sm text-gray-600 font-medium">Pass Count</div>
                <div className="text-2xl font-bold text-gray-900">
                  {evaluations.filter((e: Evaluation) => e.verdict === 'PASS').length}
                </div>
              </div>
            </div>
          </div>

          {/* Pass Rate Chart */}
          {evaluations.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6 mb-8">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">
                Pass Rate by Judge
              </h2>
              <PassRateChart evaluations={evaluations} />
            </div>
          )}

          {/* Evaluations Table */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Evaluations</h2>
            <EvaluationsTable evaluations={evaluations} />
          </div>
        </>
      )}
    </div>
  );
}
