@echo off
REM DexForge PATH Installer for Windows
REM Adds dexforge executable to system PATH

setlocal enabledelayedexpansion

set JADX_DIR=%~dp0
set INSTALL_DIR=%DEXFORGE_INSTALL_DIR%

if "%INSTALL_DIR%"=="" (
    set INSTALL_DIR=%USERPROFILE%\bin
)

if not exist "%INSTALL_DIR%" (
    echo Creating directory: %INSTALL_DIR%
    mkdir "%INSTALL_DIR%"
)

echo Installing DexForge to %INSTALL_DIR%...

REM Copy executables
copy "%JADX_DIR%bin\dexforge.bat" "%INSTALL_DIR%\dexforge.bat" >nul
copy "%JADX_DIR%bin\dexforge-gui.bat" "%INSTALL_DIR%\dexforge-gui.bat" >nul

echo DexForge installed to %INSTALL_DIR%

REM Add to user PATH
for /f "tokens=2*" %%A in ('reg query "HKCU\Environment" /v PATH 2^>nul') do set "CURRENT_PATH=%%B"

echo %CURRENT_PATH% | findstr /C:"dexforge" >nul
if %ERRORLEVEL% neq 0 (
    echo Adding DEXFORGE_HOME to user PATH...
    setx DEXFORGE_HOME "%JADX_DIR%" >nul
    setx PATH "%CURRENT_PATH%;%INSTALL_DIR%" >nul
    echo Restart your command prompt or terminal for changes to take effect.
) else (
    echo DEXFORGE_HOME already in PATH.
)

echo.
echo Installation complete. Run 'dexforge' from any command prompt.