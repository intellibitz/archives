@echo off
:: Launches auto_keep_all_win.py silently in background on Windows
set SCRIPT_DIR=%~dp0
set LOG_FILE=%TEMP%\keep_all_auto.log
set PYTHON_EXE=C:\Users\user\AppData\Local\Programs\Python\Python312\pythonw.exe

if not exist "%PYTHON_EXE%" (
    set PYTHON_EXE=pythonw.exe
)

echo Starting Auto Keep All script in background...
start /b "" "%PYTHON_EXE%" "%SCRIPT_DIR%auto_keep_all_win.py" >> "%LOG_FILE%" 2>&1
echo Auto Keep All is running. Log output: %LOG_FILE%
