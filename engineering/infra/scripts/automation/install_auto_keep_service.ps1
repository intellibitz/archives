# Registers Auto Keep All Dialog Clicker as a Windows Scheduled Task / Service
# Runs automatically at user logon in background with highest available privileges

$TaskName = "AutoKeepAllService"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$PythonScript = Join-Path $ScriptDir "auto_keep_all_win.py"

Write-Host "Registering Windows Background Service task '$TaskName'..." -ForegroundColor Cyan

# Check if task already exists and unregister it
$ExistingTask = Get-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
if ($ExistingTask) {
    Write-Host "Removing existing scheduled task '$TaskName'..." -ForegroundColor Yellow
    Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
}

# Define Task Action
$Action = New-ScheduledTaskAction -Execute "python.exe" -Argument "`"$PythonScript`""

# Define Task Trigger (At User Logon / System Startup)
$Trigger = New-ScheduledTaskTrigger -AtLogOn

# Define Task Settings
$Settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -ExecutionTimeLimit (New-TimeSpan -Hours 0) -RestartCount 3 -RestartInterval (New-TimeSpan -Minutes 1)

# Register the Scheduled Task
Register-ScheduledTask -TaskName $TaskName -Action $Action -Trigger $Trigger -Settings $Settings -Description "Auto Keep All Dialog Button Clicker Service for IDEs and Prompt Windows"

Write-Host "✅ Task '$TaskName' successfully registered!" -ForegroundColor Green
Write-Host "Starting the service now..." -ForegroundColor Green
Start-ScheduledTask -TaskName $TaskName
Write-Host "Service '$TaskName' is now running in the background." -ForegroundColor Green
