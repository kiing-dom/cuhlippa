#!/bin/bash

# Version Update Utility for Cuhlippa
# Updates version across all POM files

set -e

# Colors
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}🔧 Cuhlippa Version Manager${NC}"
echo "=============================="

# Get current version
CURRENT_VERSION=$(grep -m1 '<version>' pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
echo -e "${BLUE}Current version:${NC} $CURRENT_VERSION"

# Ask for new version
echo ""
read -p "📋 Enter new version (e.g., 1.0.1): " NEW_VERSION

if [ -z "$NEW_VERSION" ]; then
    echo -e "${YELLOW}❌ No version provided${NC}"
    exit 1
fi

echo ""
read -p "🔧 Update all POM files to v$NEW_VERSION? (y/N): " CONFIRM
CONFIRM=${CONFIRM,,}
if [[ ! $CONFIRM == "y" && ! $CONFIRM == "yes" ]]; then
    echo -e "${YELLOW}❌ Version update cancelled${NC}"
    exit 0
fi

echo ""
echo -e "${BLUE}🔄 Updating version in all POM files...${NC}"

# Update parent POM
sed -i "s/<version>$CURRENT_VERSION<\/version>/<version>$NEW_VERSION<\/version>/g" pom.xml
echo "✅ Updated pom.xml"

# Update client POM dependencies
sed -i "s/<version>$CURRENT_VERSION<\/version>/<version>$NEW_VERSION<\/version>/g" client/pom.xml
echo "✅ Updated client/pom.xml"

# Update server POM
sed -i "s/<version>$CURRENT_VERSION<\/version>/<version>$NEW_VERSION<\/version>/g" server/pom.xml
echo "✅ Updated server/pom.xml"

# Update shared POM
sed -i "s/<version>$CURRENT_VERSION<\/version>/<version>$NEW_VERSION<\/version>/g" shared/pom.xml
echo "✅ Updated shared/pom.xml"

# Update packaging POM
sed -i "s/<version>$CURRENT_VERSION<\/version>/<version>$NEW_VERSION<\/version>/g" packaging/pom.xml
echo "✅ Updated packaging/pom.xml"

# Update version.properties if it exists
if [ -f "version.properties" ]; then
    sed -i "s/version=$CURRENT_VERSION/version=$NEW_VERSION/g" version.properties
    echo "✅ Updated version.properties"
fi

echo ""
echo -e "${GREEN}🎉 Version updated successfully!${NC}"
echo -e "${BLUE}Old version:${NC} $CURRENT_VERSION"
echo -e "${BLUE}New version:${NC} $NEW_VERSION"
echo ""
echo -e "${BLUE}💡 Next steps:${NC}"
echo "1. Test the build: mvn clean compile"
echo "2. Commit changes: git add . && git commit -m 'Bump version to v$NEW_VERSION'"
echo "3. Create release: ./create-release.sh"
