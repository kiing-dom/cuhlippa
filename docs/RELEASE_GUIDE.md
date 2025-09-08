# 🚀 Release Management Guide

This guide explains how to create and manage releases for Cuhlippa with automated builds and custom messages.

## 📋 Quick Release Process

### Method 1: Automated Scripts (Recommended)

**Windows:**
```bash
# Update version across all POMs
update-version.bat

# Create release with custom message
create-release.bat
```

**Linux/macOS:**
```bash
# Update version across all POMs
./update-version.sh

# Create release with custom message  
./create-release.sh
```

### Method 2: Manual GitHub Actions

1. Go to your repository on GitHub
2. Click **Actions** → **Enhanced Release**
3. Click **Run workflow**
4. Fill in the form:
   - **Version**: e.g., `1.0.1`
   - **Release Title**: e.g., `Cuhlippa v1.0.1 - Bug Fixes`
   - **Release Notes**: Markdown description of changes
   - **Draft**: Check to create as draft
   - **Pre-release**: Check for beta/alpha releases

### Method 3: Git Tags (Automatic)

```bash
# Tag and push (triggers automatic release)
git tag v1.0.1
git push origin v1.0.1
```

## 🔧 What Gets Built

The release workflow automatically creates:

### Native Installers
- **Windows**: `Cuhlippa-Windows-1.0.1.msi` (87MB) - Includes Java runtime
- **macOS**: `Cuhlippa-macOS-1.0.1.dmg` - Drag-to-install package  
- **Linux**: `Cuhlippa-Linux-1.0.1.deb` - APT-installable package

### Portable Package
- **All Platforms**: `cuhlippa-1.0.1.zip` - Requires Java 17+
  - Includes launcher scripts for all platforms
  - Complete documentation and quick-start guide
  - SHA256 checksum for verification

## 📝 Release Notes Best Practices

### Good Release Notes Example:
```markdown
# 🚀 Major Feature Update

## ✨ New Features
- **🔍 Smart Device Discovery** - Automatically finds devices on your network
- **🎨 Dark Mode** - Professional dark theme with blue accents
- **📱 Mobile Support** - Android and iOS companion apps

## 🐛 Bug Fixes
- Fixed clipboard sync delay on large images
- Resolved network discovery issues on Windows 11
- Improved error handling for network timeouts

## 🔧 Improvements
- 40% faster image processing
- Reduced memory usage by 25%
- Better firewall compatibility

## 💔 Breaking Changes
- Removed deprecated legacy sync protocol
- Config file format updated (auto-migrated)

## 📋 Upgrade Notes
- Existing settings will be preserved
- First launch may take extra time for migration
- Windows users: Uninstall old version first
```

### Quick Release Notes:
```markdown
## What's New
- Fixed sync issues with large files
- Improved Windows 11 compatibility  
- Updated bundled Java to 17.0.8

## Download
- Windows: Download the MSI installer
- macOS: Download the DMG file
- Linux: Download the DEB package
```

## 🎯 Release Types

### Production Release
- **When**: Stable features ready for all users
- **Version**: `1.0.0`, `1.1.0`, `2.0.0`
- **Settings**: Draft=false, Pre-release=false

### Beta Release  
- **When**: Testing new features with early adopters
- **Version**: `1.1.0-beta.1`, `2.0.0-beta.2`
- **Settings**: Draft=false, Pre-release=true

### Release Candidate
- **When**: Final testing before production
- **Version**: `1.1.0-rc.1`, `2.0.0-rc.1`  
- **Settings**: Draft=false, Pre-release=true

### Draft Release
- **When**: Preparing release but not ready to publish
- **Version**: Any
- **Settings**: Draft=true, Pre-release=any

## 🔍 Monitoring Releases

### GitHub Actions
- Watch progress: `https://github.com/YOUR_USERNAME/cuhlippa/actions`
- Build time: ~10-15 minutes for all platforms
- Email notifications on completion/failure

### Release Assets
- All files are automatically uploaded to the GitHub release
- Checksums are generated for verification
- Download counts are tracked automatically

## 🚨 Troubleshooting

### Build Failures
```bash
# Check Java version
java -version  # Should be 17+

# Test local build
mvn clean package -pl packaging -am -DskipTests

# Check specific platform
mvn clean package -pl packaging -am -DskipTests -P windows
```

### Version Conflicts
```bash
# Reset versions if needed
git checkout pom.xml client/pom.xml server/pom.xml shared/pom.xml packaging/pom.xml

# Update versions properly
./update-version.sh  # or update-version.bat
```

### Release Already Exists
- Either delete the existing release/tag on GitHub
- Or increment the version number
- Or use draft mode to prepare without publishing

## 📋 Checklist for Major Releases

- [ ] Update version numbers with `update-version.sh/.bat`
- [ ] Test build locally: `mvn clean package`
- [ ] Update README.md with new features
- [ ] Write comprehensive release notes
- [ ] Test installers on target platforms
- [ ] Create release with `create-release.sh/.bat`
- [ ] Monitor build progress on GitHub Actions
- [ ] Test download and installation
- [ ] Announce release (social media, forums, etc.)

## 🎉 Post-Release

After a successful release:

1. **Download and test** the installers on different platforms
2. **Update documentation** if needed
3. **Plan next release** features and timeline
4. **Monitor user feedback** and bug reports
5. **Prepare hotfixes** if critical issues are found

---

**The release system is fully automated** - just run the scripts and let GitHub Actions handle the rest! 🚀
