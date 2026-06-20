@echo off
REM DexForge GUI compatibility wrapper - forwards to jadx-gui.bat
setlocal
set SCRIPT_DIR=%~dp0
call "%SCRIPT_DIR%jadx-gui.bat" %*