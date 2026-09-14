@echo off
echo =======================================================
echo Launching Smart Internship ^& Skill Tracker (SINST)...
echo =======================================================

if not exist bin (
    echo Bin directory not found. Compiling first...
    call compile.bat
)

java -cp "bin;lib/*" com.sinst.Main
