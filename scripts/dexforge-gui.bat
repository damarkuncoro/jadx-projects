@echo off
REM DexForge GUI launcher - main script with DexForge branding
REM Called as dexforge-gui or jadx-gui

setlocal enabledelayedexpansion
set PROGDIR=%~dp0..

REM Find the jar file (handles version numbers)
for %%F in ("%PROGDIR%\lib\jadx-gui-all.jar") do (
	set JAR_FILE=%%F
	goto :found
)
for %%F in ("%PROGDIR%\lib\dexforge-gui-all.jar") do (
	set JAR_FILE=%%F
	goto :found
)
echo Error: jadx-gui jar not found in %PROGDIR%\lib\
exit /b 1
:found

java -Xms128M -XX:MaxRAMPercentage=70.0 -Dawt.useSystemAAFontSettings=lcd -Dswing.aatext=true -Djava.util.Arrays.useLegacyMergeSort=true -Djdk.util.zip.disableZip64ExtraFieldValidation=true -XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/java.lang=ALL-UNNAMED --enable-native-access=ALL-UNNAMED -cp "%JAR_FILE%" jadx.gui.JadxGUI %*