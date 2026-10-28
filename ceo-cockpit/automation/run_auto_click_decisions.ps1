# Auto-Clicker Runner & Background Process Watchdog
$ErrorActionPreference = "Stop"

$ScriptDir = $PSScriptRoot
if (-not $ScriptDir) { $ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path }
if (-not $ScriptDir) { $ScriptDir = "F:\src\github.com\intellibitz\archives\ceo-cockpit\automation" }

$TargetScript = Join-Path $ScriptDir "auto_click_decisions.py"
$NativeExe = Join-Path $ScriptDir "AutoClickDecisionsNative.exe"

# Stop any duplicate running clicker processes
Get-CimInstance Win32_Process -Filter "Name = 'python.exe' OR Name = 'pythonw.exe' OR Name = 'AutoClickDecisionsNative.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -like "*auto_click_decisions*" -or $_.CommandLine -like "*AutoClickDecisionsNative*" } |
    ForEach-Object {
        Write-Host "Stopping existing auto-clicker process (PID $($_.ProcessId))..." -ForegroundColor Yellow
        Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
    }

Start-Sleep -Milliseconds 400

# 1. Launch Native C# UI Automation Clicker
if (Test-Path $NativeExe) {
    Write-Host "Starting Native C# UI Automation Clicker: $NativeExe" -ForegroundColor Cyan
    $procNative = Start-Process -FilePath $NativeExe -WorkingDirectory $ScriptDir -WindowStyle Hidden -PassThru
    if ($procNative) {
        Write-Host "[OK] Native C# Auto-Clicker started with PID $($procNative.Id)." -ForegroundColor Green
    }
}

# 2. Locate pythonw (hidden GUI runner) or fallback to python
$PythonW = "C:\Users\user\AppData\Local\Programs\Python\Python312\pythonw.exe"
if (-not (Test-Path $PythonW)) {
    $PythonW = (Get-Command pythonw.exe -ErrorAction SilentlyContinue).Source
}
if (-not $PythonW -or -not (Test-Path $PythonW)) {
    $PythonW = "C:\Users\user\AppData\Local\Programs\Python\Python312\python.exe"
}

Write-Host "Starting Python OpenCV Auto-Clicker using: $PythonW" -ForegroundColor Cyan
Write-Host "Target script: $TargetScript" -ForegroundColor Cyan

# Launch process in interactive desktop session via Start-Process
$proc = Start-Process -FilePath $PythonW -ArgumentList "`"$TargetScript`"" -WorkingDirectory $ScriptDir -WindowStyle Hidden -PassThru
if ($proc) {
    Write-Host "[OK] Python OpenCV Auto-Clicker started with PID $($proc.Id)." -ForegroundColor Green
} else {
    Write-Error "Failed to launch Python Auto-Clicker process."
}
