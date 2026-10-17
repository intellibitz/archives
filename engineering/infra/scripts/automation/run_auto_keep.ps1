# PowerShell background launcher for Auto Keep All dialog clicker
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$PythonScript = Join-Path $ScriptDir "auto_keep_all_win.py"
$LogFile = Join-Path $env:TEMP "keep_all_auto.log"

# Single Instance Guard: Terminate any existing background auto_keep_all processes
Get-CimInstance Win32_Process -Filter "CommandLine LIKE '%auto_keep_all%'" -ErrorAction SilentlyContinue | ForEach-Object {
    Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
}

# Locate Python 3.12 or fallback to PATH
$PythonExe = "C:\Users\user\AppData\Local\Programs\Python\Python312\python.exe"
if (-not (Test-Path $PythonExe)) {
    $PythonExe = "python.exe"
}

Write-Host "Starting single instance Auto Keep All dialog clicker service on Windows..." -ForegroundColor Green
Write-Host "Python Executable: $PythonExe"
Write-Host "Script: $PythonScript"
Write-Host "Log File: $LogFile"

Start-Process -FilePath $PythonExe -ArgumentList "`"$PythonScript`"" -WindowStyle Hidden

Start-Sleep -Seconds 2
$RunningProc = Get-CimInstance Win32_Process -Filter "CommandLine LIKE '%auto_keep_all%'" -ErrorAction SilentlyContinue
if ($RunningProc) {
    Write-Host "✅ Single Auto Keep All service process running successfully! (PID: $($RunningProc.ProcessId))" -ForegroundColor Green
} else {
    Write-Host "❌ Failed to start background process." -ForegroundColor Red
}
