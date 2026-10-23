# PowerShell background launcher for Approval Button Auto-Clicker dialog clicker
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$PythonScript = Join-Path $ScriptDir "auto_click_decisions_win.py"
$LogFile = Join-Path $env:TEMP "auto_click_decisions.log"
$PidFile = Join-Path $env:TEMP "auto_click_decisions.pid"

# Single Instance Guard: Terminate any existing background auto_click_decisions processes via PID file
if (Test-Path $PidFile) {
    $OldPid = Get-Content $PidFile -ErrorAction SilentlyContinue
    if ($OldPid) {
        $OldProcess = Get-CimInstance Win32_Process -Filter "ProcessId = $OldPid" -ErrorAction SilentlyContinue
        if ($OldProcess -and $OldProcess.CommandLine -like '*auto_click_decisions_win.py*') {
            Write-Host "Terminating existing process (PID: $OldPid)..." -ForegroundColor Yellow
            Stop-Process -Id $OldPid -Force -ErrorAction SilentlyContinue
        }
    }
    Remove-Item $PidFile -Force -ErrorAction SilentlyContinue
}

# Fallback search for any orphan auto_click_decisions instances
Get-CimInstance Win32_Process -Filter "Name = 'python.exe' OR Name = 'pythonw.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -like '*auto_click_decisions_win.py*' } | ForEach-Object {
    Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
}

# Locate Python 3.12 or fallback to PATH
$PythonExe = "C:\Users\user\AppData\Local\Programs\Python\Python312\pythonw.exe"
if (-not (Test-Path $PythonExe)) {
    $PythonExe = "C:\Users\user\AppData\Local\Programs\Python\Python312\python.exe"
}
if (-not (Test-Path $PythonExe)) {
    $PythonExe = "python.exe"
}

Write-Host "Starting single instance Approval Button Auto-Clicker dialog clicker service on Windows..." -ForegroundColor Green
Write-Host "Python Executable: $PythonExe"
Write-Host "Script: $PythonScript"
Write-Host "Log File: $LogFile"

Start-Process -FilePath $PythonExe -ArgumentList "`"$PythonScript`"" -WindowStyle Hidden

Start-Sleep -Seconds 2

if (Test-Path $PidFile) {
    $NewPid = Get-Content $PidFile -ErrorAction SilentlyContinue
    if ($NewPid -and (Get-Process -Id $NewPid -ErrorAction SilentlyContinue)) {
        Write-Host "✅ Single Approval Button Auto-Clicker service running successfully! (PID: $NewPid)" -ForegroundColor Green
        exit 0
    }
}

$RunningProc = Get-CimInstance Win32_Process -Filter "Name = 'python.exe' OR Name = 'pythonw.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -like '*auto_click_decisions_win.py*' }
if ($RunningProc) {
    Write-Host "✅ Single Approval Button Auto-Clicker service process running successfully! (PID: $($RunningProc.ProcessId))" -ForegroundColor Green
} else {
    Write-Host "❌ Failed to start background process." -ForegroundColor Red
}
