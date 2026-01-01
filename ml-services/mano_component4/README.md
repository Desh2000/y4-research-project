# Mano Component 4: Community-Driven Resilience Clustering System

## Overview

This component is part of the Mano mental health support framework.
It groups similar users together and recommends helpful activities.

## Features

- **GMM Clustering**: Groups users based on their mental health profiles
- **Stress Scoring**: Calculates stress levels from multiple factors
- **Activity Recommendations**: Suggests personalized activities
- **Peer Connections**: Finds similar users for support groups

## Setup

1. Create virtual environment:
```bash
python -m venv venv
```

2. Activate virtual environment:
```bash
# Windows
venv\Scripts\activate

# Mac/Linux
source venv/bin/activate
```

3. Install dependencies:
```bash
pip install -r requirements.txt
```

4. Run the application:
```bash
uvicorn app.main:app --reload
```

5. Open browser and go to:
```
http://127.0.0.1:8000
```

## API Documentation

After running the app, visit:
```
http://127.0.0.1:8000/docs
```

## Project Structure
```
mano_component4/
├── app/                 # Main application
│   ├── api/            # API endpoints
│   ├── core/           # Business logic
│   ├── models/         # Data schemas
│   └── utils/          # Helper functions
├── ml_models/          # Saved ML models
├── data/               # Data files
├── tests/              # Test files
├── config.py           # Configuration
└── requirements.txt    # Dependencies
```

## Author

SHALINDA D.G.M (IT22317308)
```

---

## Step 7: Verify Everything Works

### Run This Test:

1. Make sure you're in the project folder
2. Make sure venv is activated
3. Run:
```
python -c "from app.main import app; print('SUCCESS: All files created correctly!')"
```

**If successful:**
```
SUCCESS: All files created correctly!
```

---

### Run the API:
```
uvicorn app.main:app --reload
```

**You should see:**
```
INFO:     Uvicorn running on http://127.0.0.1:8000 (Press CTRL+C to quit)
INFO:     Started reloader process
INFO:     Started server process
INFO:     Waiting for application startup.
INFO:     Application startup complete.