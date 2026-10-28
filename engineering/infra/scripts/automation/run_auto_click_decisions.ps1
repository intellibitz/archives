# Backward-compatibility wrapper script delegating to ceo-cockpit automation runner
$ErrorActionPreference = "Stop"

$CanonicalRunner = "F:\src\github.com\intellibitz\archives\ceo-cockpit\automation\run_auto_click_decisions.ps1"
if (Test-Path $CanonicalRunner) {
    & $CanonicalRunner
} else {
    Write-Error "Canonical runner script not found at $CanonicalRunner"
}
