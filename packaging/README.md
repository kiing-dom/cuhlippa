# 🚀 Native Installer Setup for Cuhlippa

This directory contains the configuration for creating professional installers with bundled Java.

## 🎯 **Primary Build Method: GitHub Actions**

The main way to create installers is through **GitHub Actions automation**:

1. **Push your changes** to the repository
2. **Create a new release** on GitHub 
3. **Installers are automatically built** for all platforms
4. **Download from the release page** - ready to distribute!

## 🧪 **Local Development Builds**

For local testing and development, you can build installers manually:

### Quick Build Commands
```bash
# Test build (compilation + tests)
mvn clean compile test

# Full build with installer (Windows)
mvn clean package -DskipTests
cd packaging && mvn clean package -P windows -DskipTests
```

### Platform-Specific Local Builds
```bash
# Windows (.msi) - requires Windows + JDK 17+
mvn clean package -P windows -DskipTests

# macOS (.dmg) - requires macOS + JDK 17+  
mvn clean package -P macos -DskipTests

# Linux (.deb) - requires Linux + JDK 17+
mvn clean package -P linux -DskipTests
```

## 📦 **What Gets Created**

Each installer includes:
- ✅ **Bundled Java Runtime** - No Java installation required by users
- ✅ **Complete Application** - All JAR files and dependencies  
- ✅ **Desktop Shortcuts** - Professional system integration
- ✅ **Start Menu/Applications** - Native OS integration
- ✅ **Uninstaller** - Clean removal option

## 🎨 **Adding Application Icons**

To make installers look professional, add icon files:

```
packaging/src/main/resources/icons/
├── cuhlippa.ico     # Windows (multi-size .ico file)  
├── cuhlippa.icns    # macOS (bundle icon)
└── cuhlippa.png     # Linux (512x512 PNG)
```

**Icon Creation Tips:**
- Start with a 512x512 PNG image  
- Use online converters or GIMP for format conversion
- Icons are optional but highly recommended for professional appearance

## 🔄 GitHub Actions (Automated Building)

The project includes a GitHub Actions workflow that automatically builds installers for all platforms when you create a release.

**To trigger automated builds:**
1. Push changes to your repository
2. Create a new release on GitHub
3. Installers will be built and attached to the release automatically

## 🧪 Testing Your Installers

### Windows
1. Double-click the `.msi` file
2. Follow the installation wizard
3. Check that shortcuts are created
4. Launch from Start Menu or desktop

### macOS
1. Open the `.dmg` file
2. Drag the app to Applications folder
3. Launch from Launchpad or Applications folder
4. Grant clipboard permissions when prompted

### Linux
```bash
# Install the .deb package
sudo dpkg -i cuhlippa_1.0.0_amd64.deb

# Launch the application
cuhlippa
```

## 🚨 Common Issues

**"jpackage command not found"**
- Make sure you're using JDK 17+ (not just JRE)
- Verify `jpackage` is in your PATH

**Windows: "WiX Toolset not found"** 
- Install WiX Toolset v3.11+
- Add WiX to your PATH

**macOS: Code signing issues**
- Set `<macSign>false</macSign>` for development builds
- For distribution, you'll need an Apple Developer certificate

**Large installer sizes**
- This is normal! Each installer includes a full Java runtime (~50-100MB)
- Much better user experience than requiring separate Java installation

## 📊 Installer Sizes

Expect installer sizes around:
- **Windows .msi**: ~80-120 MB
- **macOS .dmg**: ~90-130 MB  
- **Linux .deb**: ~75-110 MB

This includes the full Java runtime, so users need nothing else installed!

## 🎯 Next Steps

After building installers:
1. Test on clean systems without Java
2. Share with beta testers
3. Create release notes
4. Upload to GitHub releases
5. Update your website/documentation

**Your users will love the professional installation experience!** 🎉
