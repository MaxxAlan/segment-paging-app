@echo off
setlocal

echo [Simulator] Checking environment...

:: 1. Check if JAVA_HOME is set
if defined JAVA_HOME (
    echo [Simulator] Found JAVA_HOME: %JAVA_HOME%
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

:: 2. Check if javac is in PATH
where javac >nul 2>nul
if %ERRORLEVEL% EQ 0 goto :FoundJava

:: 3. Try to find JDK 8 or 15 in standard locations
echo [Simulator] 'javac' not found in PATH. Searching common locations...

if exist "C:\Program Files\Java\jdk1.8.0_202\bin\javac.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202"
    goto :SetJava
)
if exist "C:\Program Files\Java\jdk-15\bin\javac.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-15"
    goto :SetJava
)
:: Generic JDK 8 check
for /d %%i in ("C:\Program Files\Java\jdk1.8.*") do (
    if exist "%%i\bin\javac.exe" (
        set "JAVA_HOME=%%i"
        goto :SetJava
    )
)

echo [Error] Could not find a JDK. Please install JDK 8 or 15, or set JAVA_HOME.
pause
exit /b 1

:SetJava
echo [Simulator] Auto-detected JDK at: %JAVA_HOME%
set "PATH=%JAVA_HOME%\bin;%PATH%"

:FoundJava
echo [Simulator] Java Compiler:
javac -version

echo.
echo [Simulator] Compiling sources...
if not exist bin mkdir bin
javac -d bin -cp src src\Main.java src\GUI\*.java src\model\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [Error] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [Simulator] Compilation successful.
echo.
echo [Simulator] Packaging into JAR...
jar cvfm Simulator.jar src\META-INF\MANIFEST.MF -C bin .

echo [Simulator] JAR created: Simulator.jar
echo.
echo [Simulator] Running Application...
echo.
java -jar Simulator.jar

pause
