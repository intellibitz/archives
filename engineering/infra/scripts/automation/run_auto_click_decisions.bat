@echo off
:: Launches auto_click_decisions_win.py silently in background on Windows
set SCRIPT_DIR=%~dp0
set LOG_FILE=%TEMP%\auto_click_decisions.log
set PYTHON_EXE=C:\Users\user\AppData\Local\Programs\Python\Python312\pythonw.exe

if not exist "%PYTHON_EXE%" (
    set PYTHON_EXE=pythonw.exe
)

echo Starting Approval Button Auto-Clicker script in background...
start /b "" "%PYTHON_EXE%" "%SCRIPT_DIR%auto_click_decisions_win.py" >> "%LOG_FILE%" 2>&1
echo Approval Button Auto-Clicker is running. Log output: %LOG_FILE%
