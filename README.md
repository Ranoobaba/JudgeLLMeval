# AI Judge Application

An AI-powered evaluation system built with Akka SDK (Java) backend and React/TypeScript frontend. This application allows you to upload submissions, configure AI judges, assign judges to questions, and run evaluations to get structured verdicts (pass/fail/inconclusive) with reasoning.

## Architecture

This application follows the architecture described in `Agent.md` and `ARCHITECTURE.md`:

- **Backend**: Akka SDK with event-sourced entities, views, agents, and workflows
- **Frontend**: Vite + React 18 + TypeScript SPA
- **Database**: PostgreSQL (or H2 for local development)
- **LLM Provider**: OpenAI (configurable)

### Core Components

- **Entities**: `SubmissionsEntity`, `JudgeEntity`, `JudgeAssignmentEntity`, `EvaluationEntity`, `RunEntity`
- **Views**: `QueuesView`, `QuestionsView`, `JudgesView`, `EvaluationsView`, `RunsView`, `SubmissionsView`
- **Agent**: `JudgeAgent` - Evaluates submissions using LLM
- **Workflow**: `RunEvaluationsWorkflow` - Orchestrates evaluation runs

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 18+ and npm
- PostgreSQL (optional, for production)
- OpenAI API key

## Setup

### Backend Setup

1. **Navigate to backend directory:**
   ```bash
   cd app
   ```

2. **Set environment variables:**
   ```bash
   export OPENAI_API_KEY=your-openai-api-key-here
   ```

   Optional (for PostgreSQL):
   ```bash
   export POSTGRES_HOST=localhost
   export POSTGRES_PORT=5432
   export POSTGRES_DB=ai_judge
   export POSTGRES_USER=postgres
   export POSTGRES_PASSWORD=postgres
   ```

3. **Build the project:**
   ```bash
   mvn clean package
   ```

4. **Run the service:**
   ```bash
   mvn exec:java -Dexec.mainClass="com.example.application.Main"
   ```

   Or use the Akka SDK's built-in runner:
   ```bash
   mvn akka:run
   ```

   The service will start on `http://localhost:8080` by default.

### Frontend Setup

1. **Navigate to frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Set environment variables:**
   Create a `.env` file:
   ```bash
   VITE_API_BASE_URL=http://localhost:8080
   ```

4. **Start the development server:**
   ```bash
   npm run dev
   ```

   The frontend will start on `http://localhost:5173` by default.

## Usage

### 1. Upload Submissions

- Navigate to the home page (`/`)
- Upload a JSON file with the following structure:
  ```json
  {
    "submissionId": "sub-123",
    "queueId": "queue-1",
    "questions": {
      "q1": {
        "questionTemplateId": "q1",
        "questionText": "What is 2+2?",
        "answerChoice": "4",
        "answerReasoning": "Two plus two equals four",
        "metadata": {}
      }
    }
  }
  ```

### 2. Create Judges

- Navigate to `/judges`
- Click "Create New Judge"
- Fill in:
  - **Name**: A descriptive name for the judge
  - **System Prompt**: The prompt that defines the judge's evaluation criteria
  - **Target Model**: The LLM model to use (e.g., `gpt-4o-mini`)
  - **Active**: Toggle to enable/disable the judge

### 3. Assign Judges to Questions

- Navigate to `/queues/:queueId` (click on a queue from the home page)
- Use the judge assignment matrix to assign judges to questions
- Check the boxes to assign judges to specific questions

### 4. Run Evaluations

- On the queue page, click "Run AI Judges"
- The system will:
  - Create evaluation tasks for each (submission × question × judge) combination
  - Process evaluations sequentially
  - Show progress with a progress bar
  - Store results in the database

### 5. View Results

- Navigate to `/results`
- Use filters to narrow down evaluations:
  - Queue
  - Judge
  - Question ID
  - Verdict (PASS/FAIL/INCONCLUSIVE)
- View pass rate statistics and charts

## API Endpoints

### Submissions
- `POST /api/submissions` - Upload a submission
- `GET /api/submissions/{id}` - Get a submission by ID

### Queues
- `GET /api/queues` - List all queues
- `GET /api/queues/{queueId}/questions` - Get questions for a queue

### Judges
- `GET /api/judges` - List all judges
- `GET /api/judges/{id}` - Get a judge by ID
- `POST /api/judges` - Create a judge
- `PUT /api/judges/{id}` - Update a judge
- `DELETE /api/judges/{id}` - Delete a judge
- `PATCH /api/judges/{id}/active` - Toggle judge active status

### Judge Assignments
- `GET /api/queues/{queueId}/judge-assignments/{questionId}` - Get assignments for a question
- `POST /api/queues/{queueId}/judge-assignments` - Set assignments for a question
- `DELETE /api/queues/{queueId}/judge-assignments/{questionId}/{judgeId}` - Remove a judge assignment

### Runs
- `POST /api/runs` - Start a new evaluation run
  ```json
  {
    "queueId": "queue-1"
  }
  ```
- `GET /api/runs/{runId}` - Get run status

### Evaluations
- `GET /api/evaluations` - List evaluations with optional filters:
  - `queueId` - Filter by queue
  - `judgeId` - Filter by judge
  - `questionTemplateId` - Filter by question
  - `verdict` - Filter by verdict (PASS/FAIL/INCONCLUSIVE)

## Development

### Backend Structure

```
app/
├── src/main/java/com/example/
│   ├── api/              # HTTP endpoints
│   ├── application/      # Main, agents, workflows
│   ├── domain/           # Domain models, entities, views
│   └── llm/              # LLM provider implementations
└── src/main/resources/
    └── application.conf  # Akka SDK configuration
```

### Frontend Structure

```
frontend/
├── src/
│   ├── api/              # API client
│   ├── components/       # React components
│   ├── pages/            # Page components
│   ├── types/            # TypeScript types
│   └── App.tsx           # Main app component
```

## Testing

### Backend Tests

Run unit tests:
```bash
cd app
mvn test
```

### Frontend Tests

Run tests (if configured):
```bash
cd frontend
npm test
```

## Troubleshooting

### Backend Issues

1. **OpenAI API Key not found:**
   - Ensure `OPENAI_API_KEY` environment variable is set
   - Check `application.conf` for configuration

2. **Database connection errors:**
   - For local development, Akka SDK uses H2 in-memory database by default
   - For PostgreSQL, ensure the database is running and credentials are correct

3. **Port conflicts:**
   - Default port is 8080, change in `application.conf` if needed

### Frontend Issues

1. **API connection errors:**
   - Ensure backend is running
   - Check `VITE_API_BASE_URL` in `.env` file
   - Verify CORS settings if running on different ports

2. **Build errors:**
   - Run `npm install` to ensure dependencies are installed
   - Check Node.js version (18+ required)

## License

This project is provided as-is for evaluation purposes.
