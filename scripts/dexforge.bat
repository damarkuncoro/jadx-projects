@echo off
REM DexForge compatibility wrapper - forwards to jadx.bat
setlocal
set SCRIPT_DIR=%~dp0
call "%SCRIPT_DIR%jadx.bat" %*