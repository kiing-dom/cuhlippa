# Quick Build Scripts for Cuhlippa Installers

## Windows
```powershell
# build-windows.ps1
Write-Host "Building Cuhlippa Windows Installer..." -ForegroundColor Green

# Build all modules first
mvn clean package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "Building Windows installer..." -ForegroundColor Yellow
    cd packaging
    mvn clean package -P windows -DskipTests
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Windows installer created successfully!" -ForegroundColor Green
        Write-Host "Location: packaging/target/installer/Cuhlippa-1.0.0.msi" -ForegroundColor Cyan
    } else {
        Write-Host "❌ Failed to create Windows installer" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Failed to build application modules" -ForegroundColor Red
}
```

## macOS/Linux
```bash
#!/bin/bash
# build-macos.sh or build-linux.sh

echo "🚀 Building Cuhlippa installer..."

# Build all modules first
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "📦 Building platform-specific installer..."
    cd packaging
    
    # Detect platform
    if [[ "$OSTYPE" == "darwin"* ]]; then
        mvn clean package -P macos -DskipTests
        INSTALLER_PATH="packaging/target/installer/Cuhlippa-1.0.0.dmg"
    else
        mvn clean package -P linux -DskipTests
        INSTALLER_PATH="packaging/target/installer/cuhlippa_1.0.0_amd64.deb"
    fi
    
    if [ $? -eq 0 ]; then
        echo "✅ Installer created successfully!"
        echo "📍 Location: $INSTALLER_PATH"
    else
        echo "❌ Failed to create installer"
    fi
else
    echo "❌ Failed to build application modules"
fi
```
