# Native Windows UI Automation Clicker Service Watchdog & Launcher
$ErrorActionPreference = "Stop"

$ScriptDir = $PSScriptRoot
if (-not $ScriptDir) { $ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path }
if (-not $ScriptDir) { $ScriptDir = "F:\src\github.com\intellibitz\archives\ceo-cockpit\automation" }

$NativeExe = Join-Path $ScriptDir "AutoClickDecisionsNative.exe"

# Stop any legacy or duplicate running clicker processes
Get-CimInstance Win32_Process -Filter "Name = 'AutoClickDecisionsNative.exe' OR Name = 'python.exe' OR Name = 'pythonw.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -like "*auto_click_decisions*" -or $_.CommandLine -like "*AutoClickDecisionsNative*" } |
    ForEach-Object {
        Write-Host "Stopping existing auto-clicker process (PID $($_.ProcessId))..." -ForegroundColor Yellow
        Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
    }

Start-Sleep -Milliseconds 300

if (-not (Test-Path $NativeExe)) {
    Write-Error "Native Windows clicker executable not found at: $NativeExe"
    exit 1
}

Write-Host "Starting Native Windows UI Automation Clicker: $NativeExe" -ForegroundColor Cyan

# Launch process in interactive desktop session
$proc = Start-Process -FilePath $NativeExe -WindowStyle Hidden -PassThru
if ($proc) {
    Write-Host "[OK] Native Windows Auto-Clicker started successfully with PID $($proc.Id)." -ForegroundColor Green
} else {
    Write-Error "Failed to launch Native Auto-Clicker process."
}
