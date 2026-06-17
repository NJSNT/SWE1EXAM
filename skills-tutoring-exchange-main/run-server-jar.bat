@echo off
rem -------------------------------------------------
rem Start Skills Tutoring Exchange Server
rem Auto-detects Java 17+ on the user's machine
rem -------------------------------------------------

call :find_java
if "%JAVA%"=="" (
    echo ERROR: Java 17 or higher is required but was not found.
    echo Please install JDK 17+ and ensure JAVA_HOME is set or java is in PATH.
    pause
    exit /b 1
)

set "JAR=%~dp0target\skills-tutoring-exchange-1.0-SNAPSHOT-server.jar"
if not exist "%JAR%" (
    echo Server JAR not found: %JAR%
    pause
    exit /b 1
)

echo Using Java: %JAVA%
echo Starting server...
"%JAVA%" -jar "%JAR%"
pause
exit /b 0

:find_java
rem 1) Try JAVA_HOME first
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA=%JAVA_HOME%\bin\java.exe"
        exit /b 0
    )
)

rem 2) Scan Program Files for JDK 17+
for /d %%D in ("C:\Program Files\Java\jdk-*") do (
    if exist "%%D\bin\java.exe" (
        set "JAVA=%%D\bin\java.exe"
    )
)
if defined JAVA exit /b 0

rem 3) Scan Program Files for Eclipse Adoptium / Temurin
for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-*") do (
    if exist "%%D\bin\java.exe" (
        set "JAVA=%%D\bin\java.exe"
    )
)
if defined JAVA exit /b 0

rem 4) Fall back to PATH
where java >nul 2>nul
if %errorlevel%==0 (
    for /f "delims=" %%J in ('where java') do (
        set "JAVA=%%J"
        exit /b 0
    )
)

set "JAVA="
exit /b 1
