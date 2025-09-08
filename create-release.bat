@echo off
REM Cuhlippa Release Manager for Windows
REM This script helps you create GitHub releases with custom messages

setlocal enabledelayedexpansion

echo 🚀 Cuhlippa Release Manager
echo ==================================

REM Check if gh CLI is installed
gh --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ GitHub CLI (gh) is not installed
    echo Please install it: https://cli.github.com/
    pause
    exit /b 1
)

REM Check if logged in to GitHub
gh auth status >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  Not logged in to GitHub
    echo Please run: gh auth login
    pause
    exit /b 1
)

REM Get current version from POM
for /f "tokens=2 delims=<>" %%a in ('findstr /r "<version>" pom.xml') do (
    set CURRENT_VERSION=%%a
    goto :found_version
)
:found_version
echo Current version in POM: %CURRENT_VERSION%

echo.
set /p VERSION="📋 Enter release version (e.g., 1.0.1): "
if "%VERSION%"=="" set VERSION=%CURRENT_VERSION%

set /p TITLE="📝 Enter custom release title (optional): "
if "%TITLE%"=="" set TITLE=Cuhlippa v%VERSION% - Professional Clipboard Sync

echo.
echo 📄 Enter release notes (markdown supported, type 'END' on a new line when done):
set NOTES=
:notes_loop
set /p line=
if "%line%"=="END" goto :notes_done
if defined NOTES (
    set NOTES=!NOTES!^

!line!
) else (
    set NOTES=!line!
)
goto :notes_loop
:notes_done

echo.
set /p DRAFT="🔧 Create as draft? (y/N): "
if /i "%DRAFT%"=="y" (
    set DRAFT_FLAG=true
) else (
    set DRAFT_FLAG=false
)

set /p PRERELEASE="🧪 Mark as pre-release? (y/N): "
if /i "%PRERELEASE%"=="y" (
    set PRERELEASE_FLAG=true
) else (
    set PRERELEASE_FLAG=false
)

REM Show summary
echo.
echo 📋 Release Summary:
echo Version: %VERSION%
echo Title: %TITLE%
echo Draft: %DRAFT_FLAG%
echo Pre-release: %PRERELEASE_FLAG%
echo.
echo Release Notes:
echo %NOTES%
echo.

set /p CONFIRM="🚀 Create this release? (y/N): "
if /i not "%CONFIRM%"=="y" (
    echo ❌ Release cancelled
    pause
    exit /b 0
)

REM Trigger the GitHub Actions workflow
echo.
echo 🔧 Triggering release workflow...

gh workflow run enhanced-release.yml -f version="%VERSION%" -f release_title="%TITLE%" -f release_notes="%NOTES%" -f draft="%DRAFT_FLAG%" -f prerelease="%PRERELEASE_FLAG%"

echo ✅ Release workflow triggered!
echo.
echo 📍 You can monitor the progress at:
for /f %%i in ('gh repo view --json owner^,name -q ".owner.login + \"/\" + .name"') do echo https://github.com/%%i/actions
echo.
echo 💡 The workflow will:
echo • Build Windows MSI installer
echo • Build macOS DMG installer
echo • Build Linux DEB installer  
echo • Create portable ZIP package
echo • Create GitHub release with all assets
echo.
echo 🎉 Release v%VERSION% will be ready in ~10-15 minutes!
pause
