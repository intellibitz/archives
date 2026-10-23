# One-Click Launch Script for CEO Cockpit
$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Starting Antigravity CEO Cockpit Mission Control...     " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Check if Windows service is already running
$svc = Get-Service -Name "ceo-cockpit" -ErrorAction SilentlyContinue
if ($svc -and $svc.Status -eq 'Running') {
    Write-Host "[OK] CEO Cockpit Windows Service is already running on port 8080." -ForegroundColor Green
    Start-Process "http://localhost:8080"
    exit 0
}

# Otherwise launch directly in local Python
$pythonExe = "python"
$serverScript = Join-Path $projectRoot "server.py"

Write-Host "Launching local server from $serverScript..." -ForegroundColor Yellow
Start-Process "http://localhost:8080"
& $pythonExe $serverScript
