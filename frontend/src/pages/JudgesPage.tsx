import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import { JudgesTable } from '../components/JudgesTable';
import { JudgeForm } from '../components/JudgeForm';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import type { Judge } from '../types';

/**
 * Judges page - Judge management (CRUD + active toggle).
 */
export function JudgesPage() {
  const [showForm, setShowForm] = useState(false);
  const [editingJudge, setEditingJudge] = useState<Judge | undefined>();

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['judges'],
    queryFn: () => api.getJudges(),
  });

  const handleCreate = () => {
    setEditingJudge(undefined);
    setShowForm(true);
  };


  const handleCloseForm = () => {
    setShowForm(false);
    setEditingJudge(undefined);
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Judges</h1>
        <button
          onClick={handleCreate}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          Create Judge
        </button>
      </div>

      {isLoading && <LoadingSpinner />}
      {error && <ErrorMessage message={error.message} onRetry={() => refetch()} />}

      {data && (
        <div className="bg-white rounded-lg shadow">
          <JudgesTable judges={data.judges || []} />
        </div>
      )}

      {showForm && (
        <JudgeForm judge={editingJudge} onClose={handleCloseForm} />
      )}
    </div>
  );
}
