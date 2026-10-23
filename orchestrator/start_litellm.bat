@echo off
setlocal
cd /d "%~dp0"
if not exist ".cache\litellm" mkdir ".cache\litellm"
set PYTHONIOENCODING=utf-8
set PYTHONUTF8=1

rem Ingest credentials from private user secrets folder on C: drive if available
if exist "%USERPROFILE%\.secrets\.env" (
    for /F "usebackq tokens=1* delims==" %%A in ("%USERPROFILE%\.secrets\.env") do (
        if not "%%A"=="" if not "%%A:~0,1%"=="#" set "%%A=%%B"
    )
)

where litellm >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    litellm --config "%~dp0litellm_config.yaml" --port 4000 > "%~dp0litellm_service.log" 2>&1
) else if exist "%LOCALAPPDATA%\Programs\Python\Python312\Scripts\litellm.exe" (
    "%LOCALAPPDATA%\Programs\Python\Python312\Scripts\litellm.exe" --config "%~dp0litellm_config.yaml" --port 4000 > "%~dp0litellm_service.log" 2>&1
) else (
    python -m litellm --config "%~dp0litellm_config.yaml" --port 4000 > "%~dp0litellm_service.log" 2>&1
)
