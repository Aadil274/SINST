@echo off
echo =======================================================
echo Compiling Smart Internship ^& Skill Tracker (SINST)...
echo =======================================================

if not exist bin mkdir bin

REM Find all java files and compile
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% equ 0 (
    echo.
    echo [SUCCESS] Compilation successful!
    echo Run the application using 'run.bat'
) else (
    echo.
    echo [ERROR] Compilation failed. Please check the error messages above.
)
pause
