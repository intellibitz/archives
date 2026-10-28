# Native Windows Folder Browser Dialog for CEO Cockpit & Cherry Studio Workspace Selection
Add-Type -AssemblyName System.Windows.Forms
$dialog = New-Object System.Windows.Forms.FolderBrowserDialog
$dialog.Description = "CEO Cockpit - Select Active Workspace Folder for Cherry Studio & AI OS"
$dialog.ShowNewFolderButton = $true

$stateUrl = "http://127.0.0.1:8080/v1/workspace"
try {
    $currState = Invoke-RestMethod -Uri $stateUrl -Method GET -ErrorAction SilentlyContinue
    if ($currState.workspace.path -and (Test-Path $currState.workspace.path)) {
        $dialog.SelectedPath = $currState.workspace.path
    }
} catch {}

$result = $dialog.ShowDialog()
if ($result -eq [System.Windows.Forms.DialogResult]::OK) {
    $selectedPath = $dialog.SelectedPath
    $body = @{ path = $selectedPath } | ConvertTo-Json
    try {
        $res = Invoke-RestMethod -Uri $stateUrl -Method POST -ContentType "application/json" -Body $body
        [System.Windows.Forms.MessageBox]::Show("Active Workspace Updated Successfully:`n`n$selectedPath`n`nAll agents in Cherry Studio and CEO Cockpit are now bound to this directory.", "CEO Cockpit Workspace Selector", [System.Windows.Forms.MessageBoxButtons]::OK, [System.Windows.Forms.MessageBoxIcon]::Information)
    } catch {
        [System.Windows.Forms.MessageBox]::Show("Failed to set workspace: $_", "CEO Cockpit Error", [System.Windows.Forms.MessageBoxButtons]::OK, [System.Windows.Forms.MessageBoxIcon]::Error)
    }
}
