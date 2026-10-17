@echo off
:: Launches auto_keep_all_win.py silently in background on Windows
set SCRIPT_DIR=%~dp0
set LOG_FILE=%TEMP%\keep_all_auto.log

echo Starting Auto Keep All script in background...
start /b "" pythonw "%SCRIPT_DIR%auto_keep_all_win.py" >> "%LOG_FILE%" 2>&1
if %ERRORLEVEL% NEQ 0 (
    start /b "" python "%SCRIPT_DIR%auto_keep_all_win.py" >> "%LOG_FILE%" 2>&1
)
echo Auto Keep All is running. Log output: %LOG_FILE%
