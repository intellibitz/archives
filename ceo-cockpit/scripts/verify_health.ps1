# Executive AI Infrastructure Health & Endpoint Verifier
$ErrorActionPreference = 'SilentlyContinue'

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Checking CEO Cockpit Infrastructure Health...           " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$services = @(
    @{ Name = "CEO Cockpit Web"; Url = "http://localhost:8080/api/health"; Port = 8080 },
    @{ Name = "LiteLLM Gateway"; Url = "http://localhost:4000/v1/models"; Port = 4000 },
    @{ Name = "OpenClaw Gateway"; Url = "http://localhost:18789/"; Port = 18789 },
    @{ Name = "Redis / Memurai"; Port = 6379 },
    @{ Name = "Qdrant Vector DB"; Url = "http://localhost:6333/dashboard"; Port = 6333 }
)

$results = foreach ($s in $services) {
    $portOpen = $false
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcp.Connect("127.0.0.1", $s.Port)
        $portOpen = $tcp.Connected
        $tcp.Close()
    } catch {
        $portOpen = $false
    }

    $httpStatus = "N/A"
    if ($s.Url -and $portOpen) {
        try {
            $resp = Invoke-WebRequest -Uri $s.Url -TimeoutSec 3 -UseBasicParsing
            $httpStatus = "$($resp.StatusCode) OK"
        } catch {
            $httpStatus = "HTTP Error"
        }
    }

    [PSCustomObject]@{
        Component   = $s.Name
        Port        = $s.Port
        Listening   = if ($portOpen) { "YES" } else { "NO" }
        HTTP_Health = $httpStatus
        Status      = if ($portOpen) { "HEALTHY" } else { "OFFLINE" }
    }
}

$results | Format-Table -AutoSize

# Check Redis Cache DBSize
$memuraiCli = "C:\Program Files\Memurai\memurai-cli.exe"
if (Test-Path $memuraiCli) {
    $keysCount = & $memuraiCli dbsize
    Write-Host "In-Memory Redis Cache Entries: $keysCount (Exact-Match Zero Cost Active)" -ForegroundColor Green
}

# Check Decision Auto-Clicker Process & Heartbeat
# One implementation: AutoClickDecisionsNative.exe (writes the heartbeat below).
$clickerProc = Get-CimInstance Win32_Process -Filter "Name = 'AutoClickDecisionsNative.exe'" |
    Select-Object -First 1
$heartbeatFile = Join-Path $env:TEMP "auto_click_decisions.heartbeat"
$isFresh = $false
if (Test-Path $heartbeatFile) {
    $ts = [double](Get-Content $heartbeatFile)
    $now = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
    if (($now - $ts) -lt 10) { $isFresh = $true }
}

if ($clickerProc -and $isFresh) {
    Write-Host "Decision Auto-Clicker: ACTIVE (PID $($clickerProc.ProcessId), Live Scanning)" -ForegroundColor Green
} elseif ($clickerProc) {
    Write-Host "Decision Auto-Clicker: RUNNING (PID $($clickerProc.ProcessId))" -ForegroundColor Yellow
} else {
    Write-Host "Decision Auto-Clicker: OFFLINE" -ForegroundColor Red
}
