# PowerShell background launcher for Auto Keep All dialog clicker
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$PythonScript = Join-Path $ScriptDir "auto_keep_all_win.py"
$LogFile = Join-Path $env:TEMP "keep_all_auto.log"

Write-Host "Starting Auto Keep All dialog clicker service on Windows..." -ForegroundColor Green
Write-Host "Script: $PythonScript"
Write-Host "Log File: $LogFile"

$ProcessInfo = New-Object System.Diagnostics.ProcessStartInfo
$ProcessInfo.FileName = "python.exe"
$ProcessInfo.Arguments = "`"$PythonScript`""
$ProcessInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Hidden
$ProcessInfo.CreateNoWindow = $true
$ProcessInfo.UseShellExecute = $false
$ProcessInfo.RedirectStandardOutput = $true
$ProcessInfo.RedirectStandardError = $true

$Process = New-Object System.Diagnostics.Process
$Process.StartInfo = $ProcessInfo
$Started = $Process.Start()

if ($Started) {
    Write-Host "✅ Auto Keep All service started successfully in background! (PID: $($Process.Id))" -ForegroundColor Green
} else {
    Write-Host "❌ Failed to start background process." -ForegroundColor Red
}
