# Registers the approval button clicker as an interactive logon task.
$ErrorActionPreference = "Stop"

$TaskName = "ApprovalButtonAutoClicker"
$LegacyTaskName = "AutoKeepAllService"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RunnerScript = Join-Path $ScriptDir "run_auto_click_decisions.ps1"

Write-Host "Registering Windows Background Service task '$TaskName'..." -ForegroundColor Cyan

# Remove both the renamed task and its previous registration to prevent duplicates.
foreach ($Name in @($LegacyTaskName, $TaskName)) {
    $ExistingTask = Get-ScheduledTask -TaskName $Name -ErrorAction SilentlyContinue
    if ($ExistingTask) {
        Write-Host "Removing existing scheduled task '$Name'..." -ForegroundColor Yellow
        Stop-ScheduledTask -TaskName $Name -ErrorAction SilentlyContinue
        try {
            Unregister-ScheduledTask -TaskName $Name -Confirm:$false -ErrorAction Stop
        } catch {
            if ($Name -eq $LegacyTaskName) {
                Write-Warning "Could not remove legacy task '$Name' (it may belong to another account); its compatibility launcher will keep it working."
            } else {
                throw
            }
        }
    }
}

# Define Task Action via PowerShell runner script
$Action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument "-WindowStyle Hidden -ExecutionPolicy Bypass -File `"$RunnerScript`""

# Define Task Trigger (At User Logon / System Startup)
$Trigger = New-ScheduledTaskTrigger -AtLogOn

# Define Task Settings
$Settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -ExecutionTimeLimit (New-TimeSpan -Hours 0) -RestartCount 3 -RestartInterval (New-TimeSpan -Minutes 1)

# Register the Scheduled Task
try {
    Register-ScheduledTask -TaskName $TaskName -Action $Action -Trigger $Trigger -Settings $Settings -Description "Approval Button Auto-Clicker for IDEs and Prompt Windows" -ErrorAction Stop
    Start-ScheduledTask -TaskName $TaskName -ErrorAction Stop
    Write-Host "Task '$TaskName' registered and started." -ForegroundColor Green
} catch {
    Write-Warning "Windows blocked scheduled task registration: $($_.Exception.Message)"
    Write-Host "Starting the clicker in the current desktop session instead..." -ForegroundColor Yellow
    & $RunnerScript
}
