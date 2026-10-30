<#
.SYNOPSIS
    Starts the native Approval Button Auto-Clicker and stops any previous one.

.DESCRIPTION
    There is exactly one implementation: the UI Automation clicker
    (AutoClickDecisionsNative.exe).  The old OpenCV template engine was removed
    because running two engines against the same dialog produced double clicks
    and competing focus changes.

    The clicker only inspects the foreground window, matches labels exactly, and
    never moves the mouse pointer.

.PARAMETER DryRun
    Log what would be clicked without dispatching any input.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File .\run_auto_click_decisions.ps1
    powershell -ExecutionPolicy Bypass -File .\run_auto_click_decisions.ps1 -DryRun
#>
[CmdletBinding()]
param(
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'

$ScriptDir = $PSScriptRoot
if (-not $ScriptDir) { $ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path }
if (-not $ScriptDir) { $ScriptDir = 'F:\src\github.com\intellibitz\archives\ceo-cockpit\automation' }

$NativeExe = Join-Path $ScriptDir 'AutoClickDecisionsNative.exe'
$LogFile = Join-Path ([System.IO.Path]::GetTempPath()) 'auto_click_decisions.log'

if (-not (Test-Path $NativeExe)) { throw "Native clicker not found: $NativeExe" }

# Stop any previous instance so only one clicker is ever running.
Get-CimInstance Win32_Process -Filter "Name = 'AutoClickDecisionsNative.exe'" -ErrorAction SilentlyContinue |
    ForEach-Object {
        Write-Host "Stopping existing auto-clicker (PID $($_.ProcessId))..." -ForegroundColor Yellow
        Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
    }

Start-Sleep -Milliseconds 400

$mode = if ($DryRun) { 'DRY-RUN' } else { 'LIVE' }
Write-Host "Starting UI Automation clicker ($mode): $NativeExe" -ForegroundColor Cyan

$startArgs = @{
    FilePath         = $NativeExe
    WorkingDirectory = $ScriptDir
    WindowStyle      = 'Hidden'
    PassThru         = $true
}
if ($DryRun) { $startArgs.ArgumentList = @('--dry-run') }

$proc = Start-Process @startArgs
if (-not $proc) { throw 'Failed to launch the auto-clicker.' }

Start-Sleep -Seconds 2
if (-not (Get-Process -Id $proc.Id -ErrorAction SilentlyContinue)) {
    Write-Error "Auto-clicker exited immediately. Last log lines:`n$(Get-Content $LogFile -Tail 15 -ErrorAction SilentlyContinue | Out-String)"
} else {
    Write-Host "[OK] Clicker running ($mode) as PID $($proc.Id)." -ForegroundColor Green
    Write-Host "Log: $LogFile" -ForegroundColor DarkGray
}
