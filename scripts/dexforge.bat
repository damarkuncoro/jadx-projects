@echo off
REM DexForge CLI launcher - main script with DexForge branding
REM Called as dexforge or jadx

setlocal enabledelayedexpansion
set PROGDIR=%~dp0..

REM Find the jar file (handles version numbers)
for %%F in ("%PROGDIR%\lib\jadx-cli-all.jar") do (
	set JAR_FILE=%%F
	goto :found
)
for %%F in ("%PROGDIR%\lib\dexforge-cli-all.jar") do (
	set JAR_FILE=%%F
	goto :found
)
echo Error: jadx-cli jar not found in %PROGDIR%\lib\
exit /b 1
:found

java -XX:+IgnoreUnrecognizedVMOptions -Xms256M -XX:MaxRAMPercentage=70.0 -XX:ParallelGCThreads=3 -Djdk.util.zip.disableZip64ExtraFieldValidation=true --enable-native-access=ALL-UNNAMED -cp "%JAR_FILE%" dexforge.cli.DexforgeCLI %*