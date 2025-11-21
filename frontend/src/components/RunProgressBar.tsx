import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import type { Run } from '../types';

interface RunProgressBarProps {
  runId: string;
  onComplete?: () => void;
}

/**
 * Progress bar component that polls run status and displays progress.
 */
export function RunProgressBar({ runId, onComplete }: RunProgressBarProps) {
  const { data: run, isLoading } = useQuery({
    queryKey: ['run', runId],
    queryFn: () => api.getRun(runId),
    refetchInterval: (query) => {
      const run = query.state.data as Run | undefined;
      // Poll every 2 seconds if run is still running
      return run?.status === 'RUNNING' ? 2000 : false;
    },
  });

  useEffect(() => {
    if (run?.status === 'COMPLETED' || run?.status === 'FAILED') {
      onComplete?.();
    }
  }, [run?.status, onComplete]);

  if (isLoading || !run) {
    return (
      <div className="w-full bg-gray-200 rounded-full h-4">
        <div className="bg-blue-600 h-4 rounded-full animate-pulse" style={{ width: '50%' }}></div>
      </div>
    );
  }

  const totalProcessed = run.completedCount + run.failedCount;
  const progressPercentage = run.plannedCount > 0 
    ? (totalProcessed / run.plannedCount) * 100 
    : 0;

  return (
    <div className="space-y-2">
      <div className="flex justify-between text-sm text-gray-600">
        <span>
          Status: <span className="font-medium">{run.status}</span>
        </span>
        <span>
          {totalProcessed} / {run.plannedCount} evaluations
        </span>
      </div>
      <div className="w-full bg-gray-200 rounded-full h-4">
        <div
          className={`h-4 rounded-full transition-all duration-300 ${
            run.status === 'COMPLETED'
              ? 'bg-green-600'
              : run.status === 'FAILED'
              ? 'bg-red-600'
              : 'bg-blue-600'
          }`}
          style={{ width: `${Math.min(progressPercentage, 100)}%` }}
        ></div>
      </div>
      {run.failedCount > 0 && (
        <div className="text-sm text-red-600">
          {run.failedCount} evaluation(s) failed
        </div>
      )}
    </div>
  );
}

