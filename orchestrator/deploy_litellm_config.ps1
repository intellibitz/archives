<#
.SYNOPSIS
    Deploys orchestrator/litellm_config.yaml to the LiteLLM service and restarts it.

.DESCRIPTION
    The repo file is the single source of truth. This copies it to the path the
    Windows service actually reads (C:\ProgramData\litellm\config\config.yaml),
    keeps a timestamped backup of the previous deployed copy, restarts the
    service and waits for the gateway to become live.

.PARAMETER NoRestart
    Deploy the file but leave the service alone.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File orchestrator\deploy_litellm_config.ps1
#>
[CmdletBinding()]
param(
    [switch]$NoRestart
)

$ErrorActionPreference = 'Stop'

$Source = Join-Path $PSScriptRoot 'litellm_config.yaml'
$Target = 'C:\ProgramData\litellm\config\config.yaml'
$Service = 'litellm'
$Gateway = 'http://127.0.0.1:4000'

if (-not (Test-Path $Source)) { throw "source config not found: $Source" }

# 1. validate before touching the live gateway
$validation = & python -c "import sys,yaml; d=yaml.safe_load(open(sys.argv[1],encoding='utf-8')); print(len({e['model_name'] for e in d['model_list']}), len(d['model_list']))" $Source 2>&1
if ($LASTEXITCODE -ne 0) { throw "config does not parse: $validation" }
$parts = $validation -split '\s+'
Write-Host ("validated: {0} model groups, {1} entries" -f $parts[0], $parts[1]) -ForegroundColor Green

# 2. back up and copy
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
if (Test-Path $Target) {
    $backup = "$Target.bak-$stamp"
    Copy-Item $Target $backup -Force
    Write-Host "backup: $backup" -ForegroundColor DarkGray
}
$targetDir = Split-Path -Parent $Target
if (-not (Test-Path $targetDir)) { New-Item -ItemType Directory -Path $targetDir -Force | Out-Null }
Copy-Item $Source $Target -Force
Write-Host "deployed: $Source -> $Target" -ForegroundColor Green

if ($NoRestart) { Write-Host 'skipping restart (-NoRestart)'; return }

# 3. restart and wait
Write-Host "restarting service '$Service'..." -ForegroundColor Cyan
Restart-Service -Name $Service -Force
# litellm validates every deployment at boot; with ~50 entries a cold start
# takes about a minute.  Note: this runs under Windows PowerShell 5.1 too, so
# no -SkipHttpErrorCheck (a 5.1 non-200 throws and is treated as "not up yet").
$deadline = (Get-Date).AddSeconds(300)
$live = $false
while ((Get-Date) -lt $deadline) {
    try {
        $r = Invoke-WebRequest "$Gateway/health/liveliness" -TimeoutSec 3 -UseBasicParsing
        if ($r.StatusCode -eq 200) { $live = $true; break }
    } catch { }
    Start-Sleep -Seconds 3
}
if (-not $live) { throw "gateway did not come back within 300s" }

$masterKey = [Environment]::GetEnvironmentVariable('LITELLM_MASTER_KEY', 'User')
try {
    $models = Invoke-WebRequest "$Gateway/v1/models" -Headers @{ Authorization = "Bearer $masterKey" } -TimeoutSec 10 -UseBasicParsing
    $count = @(($models.Content | ConvertFrom-Json).data).Count
    Write-Host ("gateway live: {0} model groups served" -f $count) -ForegroundColor Green
} catch {
    Write-Warning "gateway is live but /v1/models check failed: $($_.Exception.Message)"
}
