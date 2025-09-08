# 📋 Cuhlippa - Share Your Clipboard Between Computers

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Release](https://img.shields.io/github/v/release/kiing-dom/cuhlippa.svg)](https://github.com/kiing-dom/cuhlippa/releases)
[![Status](https://img.shields.io/badge/Status-Public%20Release-brightgreen.svg)](#)

> **Copy on one computer, paste on another - automatically!**

Cuhlippa makes it easy to share your clipboard between all your computers. Just install it, and your clipboard works seamlessly across Windows, macOS, and Linux devices on your network.

## 🚀 Quick Start

### Option 1: Download & Install (Recommended)

1. **Download** the latest installer from [Releases](https://github.com/kiing-dom/cuhlippa/releases)
2. **Install** using the native installer for your platform:
   - Windows: `.msi` installer 
   - macOS: `.dmg` installer
   - Linux: `.deb` package
3. **Launch** Cuhlippa from your applications menu
4. **Follow** the setup wizard to connect your computers

### Option 2: Manual Setup

If you prefer to run from JAR files:

1. **Download** the latest `cuhlippa-app.jar` from [Releases](https://github.com/kiing-dom/cuhlippa/releases)
2. **Run** with Java 17+:
   ```bash
   java -jar cuhlippa-app.jar
   ```
3. **Configure** through Settings → "Connect to Other Computers"

## ✨ What You Can Do

- **📝 Share Text**: Copy text on one computer, paste on another
- **🖼️ Share Images**: Screenshots and images sync automatically  
- **📁 Share Files**: File paths transfer between computers
- **📋 Clipboard History**: See and reuse your recent clipboard items
- **🔍 Auto-Discovery**: Finds your other computers automatically
- **🎨 Modern Interface**: Clean, professional design with light/dark themes

## 🔧 How It Works

Cuhlippa automatically handles the technical setup:

1. **Smart Discovery**: Finds other computers running Cuhlippa on your network
2. **Auto-Connection**: Connects automatically, no manual server setup needed
3. **Instant Sync**: Your clipboard syncs in real-time across all connected devices
4. **Zero Configuration**: Works out of the box on home and office networks

## 🚨 Troubleshooting

**Can't find other computers?**
- Make sure all computers are on the same Wi-Fi network
- Check that Windows Firewall or antivirus isn't blocking Cuhlippa
- Try the manual connection option in Settings

**Sync not working?**
- Restart Cuhlippa on all computers
- Check your network connection
- Make sure you're running the latest version

## 📋 System Requirements

- **Operating System**: Windows 10+, macOS 10.14+, or Ubuntu 18.04+
- **Java**: 17 or higher (bundled with installers)
- **Network**: Wi-Fi or wired connection to same network
- **Memory**: 512MB available RAM

## 🔒 Privacy & Security

- **Local Network Only**: Your clipboard data never leaves your network
- **No Cloud Storage**: Everything stays between your computers
- **Optional Encryption**: Enable AES encryption for sensitive data
- **Open Source**: Full source code available for review

## 🤝 Contributing

Found a bug or want to suggest a feature? 

- **Issues**: [Report bugs here](https://github.com/kiing-dom/cuhlippa/issues)
- **Discussions**: [Feature requests and questions](https://github.com/kiing-dom/cuhlippa/discussions)
- **Contributing**: See our [contribution guidelines](CONTRIBUTING.md)

## 📖 More Information

- 📖 [Installation Guide](docs/INSTALLATION.md)
- 🎯 [Project Milestones](docs/MILESTONES.md)
- 🏗️ [Technical Documentation](docs/PROJECT_STRUCTURE.md)

---

<div align="center">

**Built with ❤️ for people who work across multiple computers**

[Download Latest Release](https://github.com/kiing-dom/cuhlippa/releases) • [View Documentation](docs/) • [Report Issues](https://github.com/kiing-dom/cuhlippa/issues)

</div>

## 🔧 Development

Want to contribute or build from source?

### Build from Source
```bash
git clone https://github.com/kiing-dom/cuhlippa.git
cd cuhlippa
mvn clean package
```

### Architecture Overview
- **Multi-module Maven** project structure
- **Spring Boot WebSocket** server for real-time communication  
- **Modern Swing UI** with custom theming
- **SQLite** for local data storage and history

For detailed technical documentation, see [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md).