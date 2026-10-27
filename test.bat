@echo off
setlocal enabledelayedexpansion

echo =======================================================
echo Running SINST Automated Test Suite...
echo =======================================================

REM 1. Verify Java availability
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java runtime not found on PATH.
    echo Please install Java 21 or higher.
    exit /b 1
)

REM 2. Resolve Maven command
set "MVN_CMD="

if exist "%~dp0mvnw.cmd" (
    set "MVN_CMD=%~dp0mvnw.cmd"
) else (
    where mvn.cmd >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        set "MVN_CMD=mvn.cmd"
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

REM 3. Execute test phase
call "%MVN_CMD%" test %*
exit /b %ERRORLEVEL%
