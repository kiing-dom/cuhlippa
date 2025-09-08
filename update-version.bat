@echo off
REM Version Update Utility for Cuhlippa
REM Updates version across all POM files

setlocal enabledelayedexpansion

echo 🔧 Cuhlippa Version Manager
echo ==============================

REM Get current version
for /f "tokens=2 delims=<>" %%a in ('findstr /r "<version>" pom.xml') do (
    set CURRENT_VERSION=%%a
    goto :found_version
)
:found_version
echo Current version: %CURRENT_VERSION%

echo.
set /p NEW_VERSION="📋 Enter new version (e.g., 1.0.1): "

if "%NEW_VERSION%"=="" (
    echo ❌ No version provided
    pause
    exit /b 1
)

echo.
set /p CONFIRM="🔧 Update all POM files to v%NEW_VERSION%? (y/N): "
if /i not "%CONFIRM%"=="y" (
    echo ❌ Version update cancelled
    pause
    exit /b 0
)

echo.
echo 🔄 Updating version in all POM files...

REM Update parent POM
powershell -Command "(Get-Content pom.xml) -replace '<version>%CURRENT_VERSION%</version>', '<version>%NEW_VERSION%</version>' | Set-Content pom.xml"
echo ✅ Updated pom.xml

REM Update client POM
powershell -Command "(Get-Content client/pom.xml) -replace '<version>%CURRENT_VERSION%</version>', '<version>%NEW_VERSION%</version>' | Set-Content client/pom.xml"
echo ✅ Updated client/pom.xml

REM Update server POM
powershell -Command "(Get-Content server/pom.xml) -replace '<version>%CURRENT_VERSION%</version>', '<version>%NEW_VERSION%</version>' | Set-Content server/pom.xml"
echo ✅ Updated server/pom.xml

REM Update shared POM
powershell -Command "(Get-Content shared/pom.xml) -replace '<version>%CURRENT_VERSION%</version>', '<version>%NEW_VERSION%</version>' | Set-Content shared/pom.xml"
echo ✅ Updated shared/pom.xml

REM Update packaging POM
powershell -Command "(Get-Content packaging/pom.xml) -replace '<version>%CURRENT_VERSION%</version>', '<version>%NEW_VERSION%</version>' | Set-Content packaging/pom.xml"
echo ✅ Updated packaging/pom.xml

REM Update version.properties if it exists
if exist "version.properties" (
    powershell -Command "(Get-Content version.properties) -replace 'version=%CURRENT_VERSION%', 'version=%NEW_VERSION%' | Set-Content version.properties"
    echo ✅ Updated version.properties
)

echo.
echo 🎉 Version updated successfully!
echo Old version: %CURRENT_VERSION%
echo New version: %NEW_VERSION%
echo.
echo 💡 Next steps:
echo 1. Test the build: mvn clean compile
echo 2. Commit changes: git add . ^&^& git commit -m "Bump version to v%NEW_VERSION%"
echo 3. Create release: create-release.bat
pause
