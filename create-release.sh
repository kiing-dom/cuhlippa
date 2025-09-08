#!/bin/bash

# Cuhlippa Release Manager
# This script helps you create GitHub releases with custom messages

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Cuhlippa Release Manager${NC}"
echo "=================================="

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI (gh) is not installed${NC}"
    echo "Please install it: https://cli.github.com/"
    exit 1
fi

# Check if logged in to GitHub
if ! gh auth status &> /dev/null; then
    echo -e "${YELLOW}⚠️  Not logged in to GitHub${NC}"
    echo "Please run: gh auth login"
    exit 1
fi

# Get current version from POM
CURRENT_VERSION=$(grep -m1 '<version>' pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
echo -e "${BLUE}Current version in POM:${NC} $CURRENT_VERSION"

# Ask for release details
echo ""
read -p "📋 Enter release version (e.g., 1.0.1): " VERSION
if [ -z "$VERSION" ]; then
    VERSION=$CURRENT_VERSION
fi

read -p "📝 Enter custom release title (optional): " TITLE
if [ -z "$TITLE" ]; then
    TITLE="Cuhlippa v$VERSION - Professional Clipboard Sync"
fi

echo ""
echo -e "${YELLOW}📄 Enter release notes (markdown supported, press Ctrl+D when done):${NC}"
NOTES=$(cat)

echo ""
read -p "🔧 Create as draft? (y/N): " DRAFT
DRAFT=${DRAFT,,} # Convert to lowercase
if [[ $DRAFT == "y" || $DRAFT == "yes" ]]; then
    DRAFT_FLAG="true"
else
    DRAFT_FLAG="false"
fi

read -p "🧪 Mark as pre-release? (y/N): " PRERELEASE
PRERELEASE=${PRERELEASE,,} # Convert to lowercase
if [[ $PRERELEASE == "y" || $PRERELEASE == "yes" ]]; then
    PRERELEASE_FLAG="true"
else
    PRERELEASE_FLAG="false"
fi

# Show summary
echo ""
echo -e "${BLUE}📋 Release Summary:${NC}"
echo "Version: $VERSION"
echo "Title: $TITLE"
echo "Draft: $DRAFT_FLAG"
echo "Pre-release: $PRERELEASE_FLAG"
echo ""
echo -e "${BLUE}Release Notes:${NC}"
echo "$NOTES"
echo ""

read -p "🚀 Create this release? (y/N): " CONFIRM
CONFIRM=${CONFIRM,,}
if [[ ! $CONFIRM == "y" && ! $CONFIRM == "yes" ]]; then
    echo -e "${YELLOW}❌ Release cancelled${NC}"
    exit 0
fi

# Trigger the GitHub Actions workflow
echo ""
echo -e "${BLUE}🔧 Triggering release workflow...${NC}"

gh workflow run enhanced-release.yml \
    -f version="$VERSION" \
    -f release_title="$TITLE" \
    -f release_notes="$NOTES" \
    -f draft="$DRAFT_FLAG" \
    -f prerelease="$PRERELEASE_FLAG"

echo -e "${GREEN}✅ Release workflow triggered!${NC}"
echo ""
echo -e "${BLUE}📍 You can monitor the progress here:${NC}"
echo "https://github.com/$(gh repo view --json owner,name -q '.owner.login + "/" + .name')/actions"
echo ""
echo -e "${BLUE}💡 The workflow will:${NC}"
echo "• Build Windows MSI installer"
echo "• Build macOS DMG installer" 
echo "• Build Linux DEB installer"
echo "• Create portable ZIP package"
echo "• Create GitHub release with all assets"
echo ""
echo -e "${GREEN}🎉 Release v$VERSION will be ready in ~10-15 minutes!${NC}"
