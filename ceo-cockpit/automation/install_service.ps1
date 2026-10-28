# Registers the Decision Auto-Clicker as an interactive user logon task.
$ErrorActionPreference = "Stop"

$TaskName = "ApprovalButtonAutoClicker"
$LegacyTaskName = "AutoKeepAllService"
$ScriptDir = "F:\src\github.com\intellibitz\archives\ceo-cockpit\automation"
$RunnerScript = Join-Path $ScriptDir "run_auto_click_decisions.ps1"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Registering CEO Cockpit Auto-Decision Service...         " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Unregister any stale or duplicate scheduled tasks
foreach ($Name in @($LegacyTaskName, $TaskName)) {
    $ExistingTask = Get-ScheduledTask -TaskName $Name -ErrorAction SilentlyContinue
    if ($ExistingTask) {
        Write-Host "Unregistering stale scheduled task '$Name'..." -ForegroundColor Yellow
        Stop-ScheduledTask -TaskName $Name -ErrorAction SilentlyContinue
        try {
            Unregister-ScheduledTask -TaskName $Name -Confirm:$false -ErrorAction Stop
        } catch {
            Write-Warning "Could not unregister $($Name): $($_.Exception.Message)"
        }
    }
}

# Task Action: runs the powershell runner script with hidden window
$Action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument "-WindowStyle Hidden -ExecutionPolicy Bypass -File `"$RunnerScript`""

# Task Trigger: At User Logon
$Trigger = New-ScheduledTaskTrigger -AtLogOn

# Task Settings: Never stop, allow on battery, restart on failure
$Settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -ExecutionTimeLimit (New-TimeSpan -Days 0)

try {
    Register-ScheduledTask -TaskName $TaskName -Action $Action -Trigger $Trigger -Settings $Settings -Description "Approval Button Auto-Clicker for IDEs and Prompt Windows"
    Start-ScheduledTask -TaskName $TaskName -ErrorAction Stop
    Write-Host "[OK] Task '$TaskName' registered and started successfully!" -ForegroundColor Green
} catch {
    Write-Warning "Scheduled task registration error: $($_.Exception.Message)"
    Write-Host "Starting interactive process directly..." -ForegroundColor Yellow
    & $RunnerScript
}
