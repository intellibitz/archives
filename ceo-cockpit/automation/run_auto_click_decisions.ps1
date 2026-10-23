# Auto-Clicker Runner & Background Process Watchdog
$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$TargetScript = Join-Path $ScriptDir "auto_click_decisions.py"

# Stop any duplicate running clicker processes
Get-CimInstance Win32_Process -Filter "Name = 'python.exe' OR Name = 'pythonw.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -like "*auto_click_decisions*" } |
    ForEach-Object {
        Write-Host "Stopping existing auto-clicker process (PID $($_.ProcessId))..." -ForegroundColor Yellow
        Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
    }

Start-Sleep -Milliseconds 400

# Locate pythonw (hidden GUI runner) or fallback to python
$PythonW = "C:\Users\user\AppData\Local\Programs\Python\Python312\pythonw.exe"
if (-not (Test-Path $PythonW)) {
    $PythonW = (Get-Command pythonw.exe -ErrorAction SilentlyContinue).Source
}
if (-not $PythonW -or -not (Test-Path $PythonW)) {
    $PythonW = "C:\Users\user\AppData\Local\Programs\Python\Python312\python.exe"
}
if (-not (Test-Path $PythonW)) {
    $PythonW = (Get-Command python.exe -ErrorAction SilentlyContinue).Source
}

Write-Host "Starting Auto-Clicker using: $PythonW" -ForegroundColor Cyan
Write-Host "Target script: $TargetScript" -ForegroundColor Cyan

# Launch process detached in interactive desktop session
$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = $PythonW
$psi.Arguments = "`"$TargetScript`""
$psi.WorkingDirectory = $ScriptDir
$psi.UseShellExecute = $false
$psi.CreateNoWindow = $true

$proc = [System.Diagnostics.Process]::Start($psi)
if ($proc) {
    Write-Host "[OK] Auto-Clicker started successfully with PID $($proc.Id)." -ForegroundColor Green
} else {
    Write-Error "Failed to launch Auto-Clicker process."
}
