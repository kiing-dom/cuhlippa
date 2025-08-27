@echo off
REM Cuhlippa Demo Mode Launcher
REM Launches two demo clients to demonstrate cross-device clipboard synchronization

echo.
echo ========================================
echo    🎬 Cuhlippa Demo Mode Launcher
echo ========================================
echo.
echo This script will launch two demo clients:
echo   • Device A (Blue theme)
echo   • Device B (Green theme)
echo.
echo Both clients will run in virtual clipboard mode,
echo allowing you to demonstrate real network sync
echo without interfering with your system clipboard.
echo.
echo Press any key to start the demo...
pause >nul

REM Check if Maven build is current
if not exist "client\target\classes\com\cuhlippa\client\Main.class" (
    echo Building project...
    call mvn clean compile -q
    if errorlevel 1 (
        echo Error: Failed to build project. Please run 'mvn clean compile' manually.
        pause
        exit /b 1
    )
)

REM Start first demo client (Device A)
echo.
echo Starting Demo Device A (Blue)...
start "Cuhlippa Demo - Device A" cmd /k "cd /d %~dp0\client && mvn exec:java -Dexec.mainClass=com.cuhlippa.client.Main -Dexec.args=\"--demo-mode --device-name=Device-A\""

REM Wait a moment for first client to initialize
timeout /t 2 /nobreak >nul

REM Start second demo client (Device B)
echo Starting Demo Device B (Green)...
start "Cuhlippa Demo - Device B" cmd /k "cd /d %~dp0\client && mvn exec:java -Dexec.mainClass=com.cuhlippa.client.Main -Dexec.args=\"--demo-mode --device-name=Device-B\""

echo.
echo ✅ Demo clients launched successfully!
echo.
echo 📋 Demo Instructions:
echo   1. Use the demo controls in each window to copy content
echo   2. Watch as content syncs between the two devices
echo   3. Try copying text, images, and files
echo   4. Notice the virtual clipboard isolation
echo.
echo 🔧 Demo Features:
echo   • Virtual clipboard per device (no system interference)
echo   • Real-time network synchronization
echo   • Device-specific content labeling
echo   • Professional UI with demo indicators
echo.
echo Close this window when done with the demo.
echo The demo clients will continue running independently.
echo.
pause
