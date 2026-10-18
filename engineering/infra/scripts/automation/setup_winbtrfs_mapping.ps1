# Setup WinBtrfs User/Group Mapping & Git Environment on Windows
# Must be executed in PowerShell as Administrator

[CmdletBinding()]
param(
    [string]$TargetUser = $env:USERNAME,
    [int]$LinuxUid = 1000,
    [int]$LinuxGid = 1000
)

# Ensure elevated administrator privileges
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Warning "This script requires Administrator privileges to modify the WinBtrfs driver registry."
    Write-Host "Please right-click PowerShell and choose 'Run as Administrator'." -ForegroundColor Red
    exit 1
}

Write-Host "=== WinBtrfs & Git Cross-Platform Configuration ===" -ForegroundColor Cyan
Write-Host "Configuring for Windows User: $TargetUser"
Write-Host "Mapping to Linux UID: $LinuxUid, GID: $LinuxGid"

# 1. Resolve User SID
try {
    $Account = New-Object System.Security.Principal.NTAccount($TargetUser)
    $UserSid = $Account.Translate([System.Security.Principal.SecurityIdentifier]).Value
    Write-Host "Found User SID: $UserSid" -ForegroundColor Green
} catch {
    Write-Error "Could not resolve SID for user '$TargetUser': $_"
    exit 1
}

# 2. Configure WinBtrfs Driver Mappings in Registry
$BtrfsPath = "HKLM:\SYSTEM\CurrentControlSet\Services\btrfs"
$MappingsPath = "$BtrfsPath\Mappings"
$GroupMappingsPath = "$BtrfsPath\GroupMappings"

if (-not (Test-Path $BtrfsPath)) {
    Write-Warning "WinBtrfs service key not found at $BtrfsPath. Is the WinBtrfs driver installed?"
} else {
    # User Mappings (Windows SID -> Linux UID)
    if (-not (Test-Path $MappingsPath)) {
        New-Item -Path $MappingsPath -Force | Out-Null
    }
    Set-ItemProperty -Path $MappingsPath -Name $UserSid -Value $LinuxUid -Type DWord
    Write-Host "Configured WinBtrfs User Mapping: $UserSid -> UID $LinuxUid" -ForegroundColor Green

    # Group Mappings (Windows SID -> Linux GID)
    if (-not (Test-Path $GroupMappingsPath)) {
        New-Item -Path $GroupMappingsPath -Force | Out-Null
    }
    Set-ItemProperty -Path $GroupMappingsPath -Name $UserSid -Value $LinuxGid -Type DWord
    Write-Host "Configured WinBtrfs Group Mapping: $UserSid -> GID $LinuxGid" -ForegroundColor Green
}

# 3. Configure Git Global Settings for Cross-Platform Compatibility
Write-Host "`nConfiguring Git global settings..." -ForegroundColor Cyan

$gitCommands = @(
    @("config", "--global", "--add", "safe.directory", "*"),
    @("config", "--global", "core.filemode", "false"),
    @("config", "--global", "core.trustctime", "false"),
    @("config", "--global", "core.autocrlf", "false")
)

foreach ($args in $gitCommands) {
    try {
        & git $args
        Write-Host "git $($args -join ' ') [OK]" -ForegroundColor Green
    } catch {
        Write-Warning "Could not run: git $($args -join ' ') - Is Git in PATH?"
    }
}

Write-Host "`n Setup Complete!" -ForegroundColor Green
Write-Host "Note: WinBtrfs registry mappings take effect after rebooting Windows." -ForegroundColor Yellow
