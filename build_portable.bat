@echo off
setlocal

echo [Build] Starting Portable Build Process...

:: ==========================================
:: 1. FIND JAVA
:: ==========================================
if defined JAVA_HOME (
    set "MY_JAVA_HOME=%JAVA_HOME%"
) else (
    if exist "C:\Program Files\Java\jdk1.8.0_202" set "MY_JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202"
    if exist "C:\Program Files\Java\jdk-15" set "MY_JAVA_HOME=C:\Program Files\Java\jdk-15"
)

if not defined MY_JAVA_HOME (
    echo [Error] Could not find JDK directory to bundle.
    echo Please set JAVA_HOME or edit this script.
    pause
    exit /b 1
)
echo [Build] Found Java to bundle: %MY_JAVA_HOME%
set "PATH=%MY_JAVA_HOME%\bin;%PATH%"

:: ==========================================
:: 2. COMPILE JAR
:: ==========================================
echo [Build] Compiling Java...
if not exist bin mkdir bin
javac -d bin -cp src src\Main.java src\GUI\*.java src\model\*.java
if %ERRORLEVEL% NEQ 0 exit /b 1

echo [Build] Packaging JAR...
jar cvfm Simulator.jar src\META-INF\MANIFEST.MF -C bin .

:: ==========================================
:: 3. FIND C# COMPILER (csc.exe)
:: ==========================================
echo [Build] Locating C# Compiler...
set "CSC_PATH="
for /r "C:\Windows\Microsoft.NET\Framework64" %%f in (csc.exe) do set "CSC_PATH=%%f" & goto :FoundCSC
if not defined CSC_PATH (
    for /r "C:\Windows\Microsoft.NET\Framework" %%f in (csc.exe) do set "CSC_PATH=%%f" & goto :FoundCSC
)

:FoundCSC
if not defined CSC_PATH (
    echo [Error] Could not find csc.exe (C# Compiler).
    pause
    exit /b 1
)
echo [Build] Found csc.exe at: %CSC_PATH%

:: ==========================================
:: 4. BUILD RELEASE FOLDER
:: ==========================================
set "OUT_DIR=Portable_Release"
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
mkdir "%OUT_DIR%"

echo [Build] Compiling Launcher.exe...
"%CSC_PATH%" /target:winexe /out:"%OUT_DIR%\Simulator.exe" "src\launcher\Program.cs"
if %ERRORLEVEL% NEQ 0 (
    echo [Error] Failed to compile launcher.
    pause
    exit /b 1
)

echo [Build] Copying Simulator.jar...
copy /Y "Simulator.jar" "%OUT_DIR%" >nul

echo [Build] Bundling Java Runtime (This may take a moment)...
:: Exclude src.zip and other dev files to save space if possible, but keeping it simple
robocopy "%MY_JAVA_HOME%" "%OUT_DIR%untime" /E /XD "src.zip" "lib\src.zip" "jmods" >nul
if %ERRORLEVEL% GEQ 8 (
    echo [Warning] Robocopy reported errors. Check output.
)

echo.
echo ========================================================
echo BUILD SUCCESSFUL!
echo ========================================================
echo.
echo Your portable application is located in:
echo    %CD%\%OUT_DIR%
echo.
echo 1. Open that folder.
echo 2. Run 'Simulator.exe'.
echo 3. You can ZIP the '%OUT_DIR%' folder and send it to anyone.
echo    It contains its own Java and will run anywhere on Windows.
echo.
pause
