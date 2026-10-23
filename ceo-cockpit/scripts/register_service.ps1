# Register CEO Cockpit as an Automatic Windows Service from Archives Repository
$ErrorActionPreference = 'Stop'

$nssm = 'C:\ProgramData\chocolatey\bin\nssm.exe'
$pythonExe = 'C:\Users\user\AppData\Local\Programs\Python\Python312\python.exe'
$projectRoot = 'F:\src\github.com\intellibitz\archives\ceo-cockpit'
$serverScript = "$projectRoot\server.py"
$logDir = 'C:\ProgramData\ceo_cockpit\logs'
$serviceName = 'ceo-cockpit'

if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force | Out-Null
}

$existing = Get-Service -Name $serviceName -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "Stopping and updating existing service $serviceName..."
    & $nssm stop $serviceName | Out-Null
    Start-Sleep -Seconds 2
    & $nssm remove $serviceName confirm | Out-Null
    Start-Sleep -Seconds 1
}

Write-Host "Installing service $serviceName with NSSM pointing to $serverScript..."
& $nssm install $serviceName $pythonExe "`"$serverScript`""

& $nssm set $serviceName AppDirectory $projectRoot
& $nssm set $serviceName DisplayName "CEO Executive Cockpit"
& $nssm set $serviceName Description "Executive Mission Control Web Cockpit on port 8080 (Archives Repo)"
& $nssm set $serviceName Start SERVICE_AUTO_START

& $nssm set $serviceName AppStdout "$logDir\cockpit.log"
& $nssm set $serviceName AppStderr "$logDir\cockpit-error.log"
& $nssm set $serviceName AppStdoutCreationDisposition 4
& $nssm set $serviceName AppStderrCreationDisposition 4
& $nssm set $serviceName AppRotateFiles 1
& $nssm set $serviceName AppRotateOnline 1
& $nssm set $serviceName AppRotateSeconds 86400
& $nssm set $serviceName AppRotateBytes 10485760

& $nssm set $serviceName AppExit Default Restart
& $nssm set $serviceName AppRestartDelay 3000

& $nssm set $serviceName AppEnvironmentExtra @(
    "PYTHONIOENCODING=utf-8",
    "PYTHONUTF8=1"
)

Write-Host "Starting service $serviceName..."
& $nssm start $serviceName

Start-Sleep -Seconds 3
$svc = Get-Service -Name $serviceName
Write-Host "Service Status: $($svc.Status)"
