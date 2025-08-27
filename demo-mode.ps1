# Cuhlippa Demo Mode Launcher (PowerShell)
# Launches two demo clients to demonstrate cross-device clipboard synchronization

Write-Host ""
Write-Host "========================================"
Write-Host "   🎬 Cuhlippa Demo Mode Launcher"
Write-Host "========================================"
Write-Host ""
Write-Host "This script will launch two demo clients:"
Write-Host "  • Device A (Blue theme)"
Write-Host "  • Device B (Green theme)"
Write-Host ""
Write-Host "Both clients will run in virtual clipboard mode,"
Write-Host "allowing you to demonstrate real network sync"
Write-Host "without interfering with your system clipboard."
Write-Host ""
Write-Host "Press any key to start the demo..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Check if Maven build is current
if (-not (Test-Path "client\target\classes\com\cuhlippa\client\Main.class")) {
    Write-Host ""
    Write-Host "Building project..."
    & mvn clean compile -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Failed to build project. Please run 'mvn clean compile' manually." -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
}

# Start first demo client (Device A)
Write-Host ""
Write-Host "Starting Demo Device A (Blue)..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD/client'; mvn exec:java '-Dexec.mainClass=com.cuhlippa.client.Main' '-Dexec.args=--demo-mode --device-name=Device-A'" -WindowStyle Normal

# Wait a moment for first client to initialize
Start-Sleep -Seconds 2

# Start second demo client (Device B)
Write-Host "Starting Demo Device B (Green)..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD/client'; mvn exec:java '-Dexec.mainClass=com.cuhlippa.client.Main' '-Dexec.args=--demo-mode --device-name=Device-B'" -WindowStyle Normal

Write-Host ""
Write-Host "✅ Demo clients launched successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Demo Instructions:"
Write-Host "  1. Use the demo controls in each window to copy content"
Write-Host "  2. Watch as content syncs between the two devices"
Write-Host "  3. Try copying text, images, and files"
Write-Host "  4. Notice the virtual clipboard isolation"
Write-Host ""
Write-Host "🔧 Demo Features:"
Write-Host "  • Virtual clipboard per device (no system interference)"
Write-Host "  • Real-time network synchronization"
Write-Host "  • Device-specific content labeling"
Write-Host "  • Professional UI with demo indicators"
Write-Host ""
Write-Host "Close this window when done with the demo."
Write-Host "The demo clients will continue running independently."
Write-Host ""
Read-Host "Press Enter to exit this launcher"
