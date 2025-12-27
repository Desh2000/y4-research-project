# Quick Start After Training - Run Both Servers Simultaneously

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Empathetic Support System - Quick Start" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$PROJECT_ROOT = "d:\Sliit Project\Research\CResearch\empathetic_support_system"
Set-Location $PROJECT_ROOT

# Check if model is trained
if (-not (Test-Path "models\trained_intent_classifier")) {
    Write-Host "❌ Model not trained yet!" -ForegroundColor Red
    Write-Host "   Please wait for train_model.py to complete first." -ForegroundColor Yellow
    Write-Host "   Or run: .\venv\Scripts\python.exe train_model.py" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Model found" -ForegroundColor Green
Write-Host ""
Write-Host "Starting servers..." -ForegroundColor Yellow
Write-Host ""

# Start backend
Write-Host "[1/2] Starting backend API..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PROJECT_ROOT'; .\venv\Scripts\python.exe backend\api.py"

Start-Sleep -Seconds 3

# Start frontend  
Write-Host "[2/2] Starting frontend UI..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PROJECT_ROOT'; .\venv\Scripts\streamlit.exe run frontend\app.py"

Start-Sleep -Seconds 5

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host " System Running!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Backend API:  http://localhost:8000" -ForegroundColor Cyan
Write-Host "  API Docs:     http://localhost:8000/docs" -ForegroundColor Cyan
Write-Host "  Frontend UI:  http://localhost:8501" -ForegroundColor Cyan
Write-Host ""
Write-Host "Opening browser..." -ForegroundColor Yellow
Start-Sleep -Seconds 3
Start-Process "http://localhost:8501"

Write-Host ""
Write-Host "✓ System is ready!" -ForegroundColor Green
Write-Host ""
Write-Host "To stop: Close the two PowerShell windows that opened." -ForegroundColor Gray
Write-Host ""
