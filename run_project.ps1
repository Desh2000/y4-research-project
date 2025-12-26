# Quick Run Script - For After Initial Setup
# Use this after running setup_and_run.ps1 once

Write-Host "Starting Empathetic Support System..." -ForegroundColor Cyan
Write-Host ""

$PROJECT_ROOT = "d:\Sliit Project\Research\CResearch\empathetic_support_system"
Set-Location $PROJECT_ROOT

# Activate virtual environment
Write-Host "Activating virtual environment..." -ForegroundColor Yellow
& ".\venv\Scripts\Activate.ps1"

# Start backend
Write-Host "Starting backend server..." -ForegroundColor Yellow
$backend = Start-Job -ScriptBlock {
    Set-Location "d:\Sliit Project\Research\CResearch\empathetic_support_system"
    & ".\venv\Scripts\Activate.ps1"
    python backend\api.py
}

Start-Sleep -Seconds 5

# Start frontend
Write-Host "Starting frontend..." -ForegroundColor Yellow
$frontend = Start-Job -ScriptBlock {
    Set-Location "d:\Sliit Project\Research\CResearch\empathetic_support_system"
    & ".\venv\Scripts\Activate.ps1"
    streamlit run frontend\app.py
}

Start-Sleep -Seconds 5

Write-Host ""
Write-Host "✅ System is running!" -ForegroundColor Green
Write-Host "   Backend: http://localhost:8000" -ForegroundColor Cyan
Write-Host "   Frontend: http://localhost:8501" -ForegroundColor Cyan
Write-Host ""

# Open browser
Start-Process "http://localhost:8501"

Write-Host "Press Ctrl+C to stop..." -ForegroundColor Yellow

try {
    while ($true) { Start-Sleep -Seconds 2 }
} finally {
    Stop-Job $backend.Id, $frontend.Id
    Remove-Job $backend.Id, $frontend.Id
    Write-Host "Stopped." -ForegroundColor Green
}
