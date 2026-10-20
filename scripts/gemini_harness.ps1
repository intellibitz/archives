<#
.SYNOPSIS
    Gemini API Pay-As-You-Go Unified Management Harness (PowerShell Entrypoint).
#>
param(
    [ValidateSet("healthcheck", "start-proxy", "stop-proxy", "status-proxy", "eval", "aider")]
    [string]$Action = "healthcheck"
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
python "$ScriptDir\gemini_harness.py" $Action
