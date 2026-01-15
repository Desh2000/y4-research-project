# Empathetic Support System - Complete Setup and Run Script
# This script will set up everything and run the project

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Empathetic Support System Setup" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Set error action preference
$ErrorActionPreference = "Continue"

# Get project root
$PROJECT_ROOT = "d:\Sliit Project\Research\CResearch\empathetic_support_system"
Set-Location $PROJECT_ROOT

Write-Host "[1/7] Checking datasets..." -ForegroundColor Yellow

# Check if datasets exist
$DATA_SOURCE = "c:\Users\dilek\Downloads\Compressed\archive (1-3)"
$DATA_DEST = "$PROJECT_ROOT\data"

# Create data directory
if (-not (Test-Path $DATA_DEST)) {
    New-Item -ItemType Directory -Path $DATA_DEST -Force | Out-Null
    Write-Host "  Created data folder" -ForegroundColor Green
}

# Copy datasets if they exist
$files_to_copy = @("Mental_Health_FAQ.csv", "intents.json", "train.csv")
$all_files_exist = $true

foreach ($file in $files_to_copy) {
    $source = Join-Path $DATA_SOURCE $file
    $dest = Join-Path $DATA_DEST $file
    
    if (Test-Path $source) {
        if (-not (Test-Path $dest)) {
            Copy-Item $source $dest -Force
            Write-Host "  Copied $file" -ForegroundColor Green
        } else {
            Write-Host "  $file already exists" -ForegroundColor Gray
        }
    } else {
        Write-Host "  WARNING: $file not found in $DATA_SOURCE" -ForegroundColor Red
        $all_files_exist = $false
    }
}

if (-not $all_files_exist) {
    Write-Host ""
    Write-Host "  Please manually copy the following files to $DATA_DEST" -ForegroundColor Yellow
    Write-Host "  - Mental_Health_FAQ.csv" -ForegroundColor Yellow
    Write-Host "  - intents.json" -ForegroundColor Yellow
    Write-Host "  - train.csv" -ForegroundColor Yellow
    Write-Host ""
    $continue = Read-Host "  Continue anyway? (y/n)"
    if ($continue -ne "y") {
        exit
    }
}

Write-Host ""
Write-Host "[2/7] Setting up Python virtual environment..." -ForegroundColor Yellow

# Check if venv exists
if (-not (Test-Path "venv")) {
    python -m venv venv
    Write-Host "  Virtual environment created" -ForegroundColor Green
} else {
    Write-Host "  Virtual environment already exists" -ForegroundColor Gray
}

# Activate virtual environment
Write-Host "  Activating virtual environment..." -ForegroundColor Gray
& ".\venv\Scripts\Activate.ps1"

Write-Host ""
Write-Host "[3/7] Installing dependencies (this may take 5-10 minutes)..." -ForegroundColor Yellow

# Upgrade pip
python -m pip install --upgrade pip --quiet

# Install dependencies
pip install -r requirements.txt --quiet
Write-Host "  All dependencies installed" -ForegroundColor Green

Write-Host ""
Write-Host "[4/7] Downloading NLTK data..." -ForegroundColor Yellow

# Download NLTK data
python -c "import nltk; nltk.download('punkt', quiet=True); nltk.download('stopwords', quiet=True); nltk.download('wordnet', quiet=True); nltk.download('vader_lexicon', quiet=True); print('  NLTK data downloaded')"

Write-Host ""
Write-Host "[5/7] Checking if model is trained..." -ForegroundColor Yellow

# Check if model exists
$MODEL_PATH = "$PROJECT_ROOT\models\trained_intent_classifier"
if (-not (Test-Path $MODEL_PATH)) {
    Write-Host "  Model not found. Training now (this will take 20-30 minutes)..." -ForegroundColor Yellow
    Write-Host "  Please be patient, training BERT model..." -ForegroundColor Cyan
    python train_model.py
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  Model trained successfully!" -ForegroundColor Green
    } else {
        Write-Host "  ERROR: Model training failed. Please check the output above." -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "  Model already trained" -ForegroundColor Gray
}

Write-Host ""
Write-Host "[6/7] Starting backend server..." -ForegroundColor Yellow

# Start backend in background
$backend_job = Start-Job -ScriptBlock {
    Set-Location "d:\Sliit Project\Research\CResearch\empathetic_support_system"
    & ".\venv\Scripts\Activate.ps1"
    python backend\api.py
}

Write-Host "  Backend server starting on http://localhost:8000" -ForegroundColor Green
Write-Host "  Backend Job ID: $($backend_job.Id)" -ForegroundColor Gray

# Wait for backend to start
Write-Host "  Waiting for backend to initialize..." -ForegroundColor Gray
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "[7/7] Starting frontend application..." -ForegroundColor Yellow

# Start frontend in background
$frontend_job = Start-Job -ScriptBlock {
    Set-Location "d:\Sliit Project\Research\CResearch\empathetic_support_system"
    & ".\venv\Scripts\Activate.ps1"
    streamlit run frontend\app.py --server.headless true
}

Write-Host "  Frontend starting on http://localhost:8501" -ForegroundColor Green
Write-Host "  Frontend Job ID: $($frontend_job.Id)" -ForegroundColor Gray

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "  Setup Complete! 🎉" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Backend API: http://localhost:8000" -ForegroundColor Cyan
Write-Host "  API Docs: http://localhost:8000/docs" -ForegroundColor Cyan
Write-Host "  Frontend: http://localhost:8501" -ForegroundColor Cyan
Write-Host ""
Write-Host "  The browser will open automatically in a few seconds..." -ForegroundColor Yellow
Write-Host ""

# Wait a bit more for services to fully start
Start-Sleep -Seconds 5

# Open browser
Start-Process "http://localhost:8501"

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  System is running!" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  To stop the servers:" -ForegroundColor Yellow
Write-Host "  1. Press Ctrl+C" -ForegroundColor Gray
Write-Host "  2. Run: Stop-Job $($backend_job.Id), $($frontend_job.Id)" -ForegroundColor Gray
Write-Host ""
Write-Host "  To view logs:" -ForegroundColor Yellow
Write-Host "  - Backend: Receive-Job $($backend_job.Id)" -ForegroundColor Gray
Write-Host "  - Frontend: Receive-Job $($frontend_job.Id)" -ForegroundColor Gray
Write-Host ""
Write-Host "  Press Ctrl+C to stop all services..." -ForegroundColor Cyan

# Keep script running and show logs
try {
    while ($true) {
        Start-Sleep -Seconds 2
    }
} finally {
    Write-Host ""
    Write-Host "  Stopping servers..." -ForegroundColor Yellow
    Stop-Job $backend_job.Id, $frontend_job.Id
    Remove-Job $backend_job.Id, $frontend_job.Id
    Write-Host "  All services stopped" -ForegroundColor Green
}
