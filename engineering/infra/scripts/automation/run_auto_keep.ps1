# Compatibility launcher for the previously registered AutoKeepAllService task.
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RenamedRunner = Join-Path $ScriptDir "run_auto_click_decisions.ps1"
& $RenamedRunner
exit $LASTEXITCODE
