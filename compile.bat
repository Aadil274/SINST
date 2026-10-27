@echo off
setlocal enabledelayedexpansion

echo =======================================================
echo Building Smart Internship ^& Skill Tracker (SINST)...
echo =======================================================

REM 1. Verify Java availability
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java runtime not found on PATH.
    echo Please install Java 21 or higher and ensure 'java' is accessible.
    echo Download JDK: https://www.oracle.com/java/technologies/downloads/
    exit /b 1
)

REM 2. Resolve Maven command (mvn on PATH, mvnw.cmd in repo, or MAVEN_HOME)
set "MVN_CMD="

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    set "MVN_CMD=mvn"
) else (
    if exist "%~dp0mvnw.cmd" (
        set "MVN_CMD=%~dp0mvnw.cmd"
    ) else (
        if defined MAVEN_HOME (
            if exist "%MAVEN_HOME%\bin\mvn.cmd" (
                set "MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
            )
        )
    )
)

if "%MVN_CMD%"=="" (
    if exist "%USERPROFILE%\apache-maven-3.9.16\bin\mvn.cmd" (
        set "MVN_CMD=%USERPROFILE%\apache-maven-3.9.16\bin\mvn.cmd"
    )
)

if "%MVN_CMD%"=="" (
    echo [ERROR] Maven not found.
    echo Please install Apache Maven 3.8+ or use the included mvnw.cmd wrapper.
    exit /b 1
)

echo [INFO] Using build tool: %MVN_CMD%
echo.

REM 3. Execute standardized Maven build and package
call "%MVN_CMD%" clean package
if %ERRORLEVEL% equ 0 (
    echo.
    echo =======================================================
    echo [SUCCESS] SINST build completed successfully!
    echo Artifact: target\sinst-1.0.0.jar
    echo Run the application using 'run.bat'
    echo =======================================================
    exit /b 0
) else (
    echo.
    echo =======================================================
    echo [ERROR] Maven build failed with exit code %ERRORLEVEL%.
    echo Review the compiler/test diagnostics above.
    echo =======================================================
    exit /b %ERRORLEVEL%
)
