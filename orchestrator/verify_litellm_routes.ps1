<#
.SYNOPSIS
    Calls every LiteLLM model group and reports which ones actually answer.

.DESCRIPTION
    Reads the served model list from the gateway, sends a one-token chat request
    to each group (or an embedding request for embedding groups) and prints a
    status table.  Exits non-zero if any tested route fails, so it can gate a
    deploy.

    local-* routes are skipped unless -IncludeLocal is passed, because they need
    ollama (:11434) / lm-studio (:1234) running.

.PARAMETER IncludeLocal
    Also test the local-* routes.

.PARAMETER TimeoutSec
    Per-request timeout. Default 90 (some reasoning routes are slow to answer).

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File orchestrator\verify_litellm_routes.ps1
#>
[CmdletBinding()]
param(
    [switch]$IncludeLocal,
    [int]$TimeoutSec = 90
)

$ErrorActionPreference = 'Stop'
$Gateway = 'http://127.0.0.1:4000'
$MasterKey = [Environment]::GetEnvironmentVariable('LITELLM_MASTER_KEY', 'User')
if (-not $MasterKey) { throw 'LITELLM_MASTER_KEY is not set for this user' }

try {
    $models = Invoke-WebRequest "$Gateway/v1/models" -Headers @{ Authorization = "Bearer $MasterKey" } -TimeoutSec 10 -UseBasicParsing
} catch {
    throw "gateway is not answering on $Gateway : $($_.Exception.Message)"
}

$groups = @(($models.Content | ConvertFrom-Json).data | ForEach-Object { $_.id }) | Sort-Object
$isEmbedding = { param($name) $name -match 'embedding' }
$isLocal = { param($name) $name -like 'local-*' -or $name -like 'local*' }

$rows = foreach ($g in $groups) {
    if ((& $isLocal $g) -and -not $IncludeLocal) {
        [pscustomobject]@{ Route = $g; Kind = 'chat'; Status = 'skipped'; Ms = 0; Ok = $null; Note = 'local runtime not tested' }
        continue
    }

    $kind = if (& $isEmbedding $g) { 'embedding' } else { 'chat' }
    $body = if ($kind -eq 'embedding') {
        @{ model = $g; input = 'ping' } | ConvertTo-Json -Depth 4
    } else {
        @{ model = $g; messages = @(@{ role = 'user'; content = 'ping' }); max_tokens = 1 } | ConvertTo-Json -Depth 5
    }
    $path = if ($kind -eq 'embedding') { '/v1/embeddings' } else { '/v1/chat/completions' }

    $sw = [Diagnostics.Stopwatch]::StartNew()
    try {
        $resp = Invoke-WebRequest "$Gateway$path" -Method Post -Headers @{ Authorization = "Bearer $MasterKey" } `
            -ContentType 'application/json' -Body $body -TimeoutSec $TimeoutSec -UseBasicParsing
        $sw.Stop()
        [pscustomobject]@{ Route = $g; Kind = $kind; Status = $resp.StatusCode; Ms = $sw.ElapsedMilliseconds; Ok = $true; Note = '' }
    } catch {
        $sw.Stop()
        $code = 'ERR'
        $note = $_.Exception.Message
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            try { $note = ($_.ErrorDetails.Message | ConvertFrom-Json).error.message } catch { $note = $_.ErrorDetails.Message }
        }
        if ($note.Length -gt 70) { $note = $note.Substring(0, 70) }
        [pscustomobject]@{ Route = $g; Kind = $kind; Status = $code; Ms = $sw.ElapsedMilliseconds; Ok = $false; Note = ($note -replace '\s+', ' ') }
    }
}

$rows | Format-Table Route, Kind, Status, Ms, Ok -AutoSize

$tested = @($rows | Where-Object { $_.Ok -ne $null })
$failed = @($tested | Where-Object { -not $_.Ok })
Write-Host ""
Write-Host ("tested {0} route(s): {1} ok, {2} failed, {3} skipped" -f `
    $tested.Count, ($tested | Where-Object Ok).Count, $failed.Count, ($rows | Where-Object { $_.Ok -eq $null }).Count)

if ($failed.Count -gt 0) {
    Write-Host "`nfailures:" -ForegroundColor Red
    $failed | ForEach-Object { Write-Host ("  {0,-26} {1}" -f $_.Route, $_.Note) -ForegroundColor Red }
    exit 1
}
exit 0
