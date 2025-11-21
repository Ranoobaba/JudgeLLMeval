/**
 * TypeScript types matching the backend domain models.
 */

export type Verdict = "PASS" | "FAIL" | "INCONCLUSIVE";

export interface Submission {
  submissionId: string;
  queueId: string;
  questions: Record<string, QuestionAnswer>;
}

export interface QuestionAnswer {
  questionTemplateId: string;
  questionText: string;
  answerChoice?: string;
  answerReasoning?: string;
  metadata?: Record<string, unknown>;
}

export interface Judge {
  judgeId: string;
  name: string;
  systemPrompt: string;
  targetModel: string;
  active: boolean;
}

export interface JudgeAssignment {
  queueId: string;
  questionTemplateId: string;
  judgeIds: string[];
}

export interface Evaluation {
  evaluationId: string;
  runId: string;
  submissionId: string;
  queueId: string;
  questionTemplateId: string;
  judgeId: string;
  verdict: Verdict;
  reasoning: string;
  evaluatedAt: string;
}

export interface Run {
  runId: string;
  queueId: string;
  status: "RUNNING" | "COMPLETED" | "FAILED";
  plannedCount: number;
  completedCount: number;
  failedCount: number;
  startedAt: string;
  completedAt?: string;
}

export interface Queue {
  queueId: string;
}

export interface Question {
  queueId: string;
  questionTemplateId: string;
  questionText: string;
}

