# How to Run the AI Judge Application

## Prerequisites

1. **Java 17+** installed
2. **Node.js 18+** and npm installed
3. **OpenAI API Key** set as environment variable

## Step 1: Set Up Environment Variables

### Backend (Terminal 1)

```bash
# Set your OpenAI API key
export OPENAI_API_KEY=your-openai-api-key-here

# Navigate to backend directory
cd app
```

### Frontend (Terminal 2)

```bash
# Navigate to frontend directory
cd frontend

# Create .env file if it doesn't exist
echo "VITE_API_BASE_URL=http://localhost:8080" > .env
```

## Step 2: Start the Backend

In **Terminal 1** (backend directory):

```bash
cd app

# Compile and run the backend
mvn compile exec:java
```

**Expected output:**
- You should see logs like: "AI Judge Service starting up..."
- The service will start on `http://localhost:8080`
- You can test it by visiting: `http://localhost:8080/health`

**Note:** The backend runs in the foreground. Keep this terminal open.

## Step 3: Start the Frontend

In **Terminal 2** (frontend directory):

```bash
cd frontend

# Install dependencies (first time only)
npm install

# Start the development server
npm run dev
```

**Expected output:**
- Vite will start the dev server
- Usually runs on `http://localhost:5173`
- The browser should open automatically, or visit the URL shown in terminal

## Step 4: Use the Application

1. **Upload a Submission:**
   - Go to the home page (`/`)
   - Upload a JSON file with submission data
   - Example format:
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

2. **Create Judges:**
   - Navigate to `/judges`
   - Click "Create New Judge"
   - Fill in name, system prompt, and model (e.g., `gpt-4o-mini`)

3. **Assign Judges to Questions:**
   - Click on a queue from the home page
   - Use the judge assignment matrix to assign judges to questions

4. **Run Evaluations:**
   - On the queue page, click "Run AI Judges"
   - Watch the progress bar as evaluations complete

5. **View Results:**
   - Navigate to `/results`
   - Use filters to narrow down evaluations
   - View pass rates and charts

## Troubleshooting

### Backend won't start
- **Check API key:** Make sure `OPENAI_API_KEY` is set: `echo $OPENAI_API_KEY`
- **Check port:** Make sure port 8080 is not in use: `lsof -i :8080`
- **Check logs:** Look for error messages in the terminal

### Frontend can't connect to backend
- **Check backend is running:** Visit `http://localhost:8080/health`
- **Check .env file:** Make sure `VITE_API_BASE_URL=http://localhost:8080` is set
- **Restart frontend:** Stop and restart `npm run dev` after changing .env

### Port conflicts
- **Backend:** Change port in `app/src/main/resources/application.conf` (line 22)
- **Frontend:** Change port in `frontend/vite.config.ts` or use `npm run dev -- --port 3000`

## Stopping the Services

- **Backend:** Press `Ctrl+C` in Terminal 1
- **Frontend:** Press `Ctrl+C` in Terminal 2

## Quick Start (All Commands)

**Terminal 1 (Backend):**
```bash
cd app
export OPENAI_API_KEY=your-key-here
mvn compile exec:java
```

**Terminal 2 (Frontend):**
```bash
cd frontend
npm install  # First time only
npm run dev
```

Then open `http://localhost:5173` in your browser!

