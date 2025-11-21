# Agent.md



## Overview

This document describes the **agent and workflow architecture** used to power the AI Judge feature.

The backend is built on **Akka SDK** and uses:

- **Agents** for encapsulating LLM calls ("judge" behavior)

- **Workflows** for orchestrating evaluation runs across many submissions

- **Entities/Views** for persistence and querying

The key idea:  

> **Agents do the "thinking" (LLM calls). Workflows decide *when* and *how often* they get called.**

---

## Concepts

### Agent

An **Agent** wraps an LLM model call in a type-safe interface. It:

- Receives a typed request (question, answer, rubric, etc.)

- Calls the configured LLM provider (OpenAI, Anthropic, Gemini…)

- Normalizes the response into a **structured verdict** (`pass | fail | inconclusive`) plus reasoning

- Handles AI-specific errors (bad JSON, missing fields, etc.)

In this project, we use a single primary agent:

- `JudgeAgent` – evaluates a single `(submission, question, judge)` and returns the verdict.

### Workflow

A **Workflow** coordinates multiple steps and orchestrates many agent invocations. It:

- Iterates over submissions and questions in a queue

- Looks up which judges are assigned to each question

- Invokes `JudgeAgent` for each `(question × judge)` pair

- Persists evaluations and updates run progress (planned / completed / failed)

In this project:

- `RunEvaluationsWorkflow` performs a full evaluation pass over a queue.

### Entities & Views (storage)

Akka **entities** store long-lived state; **views** provide query access.

Relevant entities:

- `SubmissionsEntity` – stores imported submissions

- `JudgeEntity` – stores AI judge definitions (name, prompt, model, active flag)

- `JudgeAssignmentEntity` – stores mapping from questions to judges per queue

- `EvaluationEntity` – stores individual evaluation results

- `RunEntity` – stores progress for a specific evaluation run

Views expose:

- Lists of queues

- Questions per queue

- Judges

- Evaluations filtered by queue/judge/question/verdict

- Run status / counts

---

## JudgeAgent

### Responsibilities

`JudgeAgent` is responsible for turning:

> Question + Answer + Judge rubric (+ optional metadata/attachments)

into:

> `{ verdict: "pass" | "fail" | "inconclusive", reasoning: string }`

It **does not**:

- Loop over multiple submissions

- Decide which judges to run

- Handle persistence

Those responsibilities belong to the workflow and entities.

### Input / Output types

**Input: `EvaluationRequest`**

```ts

type IncludedFields = {

  includeQuestionText: boolean;

  includeAnswerChoice: boolean;

  includeAnswerReasoning: boolean;

  includeMetadata: boolean;

};

type EvaluationRequest = {

  runId: string;

  submissionId: string;

  queueId: string;

  questionTemplateId: string;

  judgeId: string;

  // What is being judged

  questionText: string;

  answerChoice?: string;

  answerReasoning?: string;

  metadata?: Record<string, unknown>;

  // Judge config

  judgeName: string;

  judgeSystemPrompt: string;

  targetModel: string;

  // Prompt shaping options (bonus feature)

  includedFields: IncludedFields;

  // Optional bonus: attachment URLs or IDs

  attachmentUrls?: string[];

};

