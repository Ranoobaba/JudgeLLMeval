import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { FileUpload } from '../components/FileUpload';

/**
 * Home page - File upload + queue list.
 */
export function HomePage() {
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['queues'],
    queryFn: () => api.getQueues(),
  });

  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-8">AI Judge Platform</h1>

      <div className="bg-white rounded-lg shadow p-6 mb-8">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Upload Submission</h2>
        <FileUpload />
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Queues</h2>
        
        {isLoading && <LoadingSpinner />}
        {error && <ErrorMessage message={error.message} onRetry={() => refetch()} />}
        
        {data && (
          <div className="space-y-2">
            {data.queues && data.queues.length > 0 ? (
              data.queues.map((queue) => (
                <Link
                  key={queue.queueId}
                  to={`/queues/${queue.queueId}`}
                  className="block p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  <div className="font-medium text-gray-900">{queue.queueId}</div>
                </Link>
              ))
            ) : (
              <p className="text-gray-500">No queues found. Upload a submission to create a queue.</p>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

