/**
 * API client for communicating with the backend.
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

async function fetchAPI<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  if (!response.ok) {
    let errorMessage = `HTTP ${response.status}`;
    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorData.error || errorMessage;
    } catch {
      const errorText = await response.text();
      errorMessage = errorText || errorMessage;
    }
    throw new Error(errorMessage);
  }

  // Handle empty responses
  const text = await response.text();
  if (!text) {
    return null as T;
  }

  return JSON.parse(text) as T;
}

export const api = {
  // Submissions
  uploadSubmission: async (submission: any) => {
    return fetchAPI<string>('/api/submissions', {
      method: 'POST',
      body: JSON.stringify(submission),
    });
  },

  getSubmission: async (submissionId: string) => {
    return fetchAPI<any>(`/api/submissions/${submissionId}`);
  },

  // Queues
  getQueues: async () => {
    return fetchAPI<{ queues: Array<{ queueId: string }> }>('/api/queues');
  },

  getQuestions: async (queueId: string) => {
    return fetchAPI<{ questions: Array<{ queueId: string; questionTemplateId: string; questionText: string }> }>(
      `/api/queues/${queueId}/questions`
    );
  },

  // Judges
  getJudges: async () => {
    return fetchAPI<{ judges: Array<any> }>('/api/judges');
  },

  getJudge: async (judgeId: string) => {
    return fetchAPI<any>(`/api/judges/${judgeId}`);
  },

  createJudge: async (judge: { name: string; systemPrompt: string; targetModel: string; active: boolean }) => {
    return fetchAPI<string>('/api/judges', {
      method: 'POST',
      body: JSON.stringify(judge),
    });
  },

  updateJudge: async (judgeId: string, judge: { name: string; systemPrompt: string; targetModel: string }) => {
    return fetchAPI<void>(`/api/judges/${judgeId}`, {
      method: 'PUT',
      body: JSON.stringify(judge),
    });
  },

  deleteJudge: async (judgeId: string) => {
    return fetchAPI<void>(`/api/judges/${judgeId}`, {
      method: 'DELETE',
    });
  },

  setJudgeActive: async (judgeId: string, active: boolean) => {
    return fetchAPI<void>(`/api/judges/${judgeId}/active`, {
      method: 'PATCH',
      body: JSON.stringify({ active }),
    });
  },

  // Judge Assignments
  getJudgeAssignments: async (queueId: string, questionTemplateId: string) => {
    return fetchAPI<any>(`/api/queues/${queueId}/judge-assignments/${questionTemplateId}`);
  },

  createJudgeAssignment: async (
    queueId: string,
    questionTemplateId: string,
    judgeIds: string[]
  ) => {
    return fetchAPI<void>(`/api/queues/${queueId}/judge-assignments`, {
      method: 'POST',
      body: JSON.stringify({ questionTemplateId, judgeIds }),
    });
  },

  removeJudgeAssignment: async (queueId: string, questionTemplateId: string, judgeId: string) => {
    return fetchAPI<void>(`/api/queues/${queueId}/judge-assignments/${questionTemplateId}/${judgeId}`, {
      method: 'DELETE',
    });
  },

  // Runs
  startRun: async (queueId: string) => {
    return fetchAPI<string>('/api/runs', {
      method: 'POST',
      body: JSON.stringify({ queueId }),
    });
  },

  getRun: async (runId: string) => {
    return fetchAPI<any>(`/api/runs/${runId}`);
  },

  // Evaluations
  getEvaluations: async (filters?: {
    queueId?: string;
    judgeId?: string;
    questionTemplateId?: string;
    verdict?: string;
  }) => {
    const params = new URLSearchParams();
    if (filters?.queueId) params.append('queueId', filters.queueId);
    if (filters?.judgeId) params.append('judgeId', filters.judgeId);
    if (filters?.questionTemplateId) params.append('questionTemplateId', filters.questionTemplateId);
    if (filters?.verdict) params.append('verdict', filters.verdict);

    const queryString = params.toString();
    return fetchAPI<{ evaluations: Array<any> }>(
      `/api/evaluations${queryString ? `?${queryString}` : ''}`
    );
  },
};

