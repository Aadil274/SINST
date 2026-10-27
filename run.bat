@echo off
setlocal enabledelayedexpansion

echo =======================================================
echo Launching Smart Internship ^& Skill Tracker (SINST)...
echo =======================================================

REM 1. Verify Java availability
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java runtime not found on PATH.
    echo Please install Java 21 or higher and ensure 'java' is accessible.
    echo Download JDK: https://www.oracle.com/java/technologies/downloads/
    exit /b 1
)

REM 2. Verify packaged application JAR exists; build if missing
set "APP_JAR=%~dp0target\sinst-1.0.0.jar"

if not exist "%APP_JAR%" (
    echo [INFO] Packaged JAR not found. Building project first...
    echo.
    call "%~dp0compile.bat"
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] Build failed. Cannot launch application.
        exit /b %ERRORLEVEL%
    )
)

if not exist "%APP_JAR%" (
    echo [ERROR] Target artifact '%APP_JAR%' does not exist.
    exit /b 1
)

REM 3. Launch packaged desktop application
echo [INFO] Launching application from: %APP_JAR%
echo.
java -jar "%APP_JAR%" %*

if %ERRORLEVEL% neq 0 (
    echo [INFO] Application exited with code %ERRORLEVEL%.
)
