# Cuhlippa Installation Guide

## 🚀 Quick Installation

### Step 1: Download
1. Download the latest release from GitHub: `cuhlippa-1.0.0.zip`
2. Extract to your preferred location (e.g., `C:\Programs\Cuhlippa\`)

### Step 2: Install Java (if needed)
**Check if Java is installed:**
```bash
java -version
```

**If Java 17+ is not installed:**
- **Windows**: Download from [Adoptium](https://adoptium.net/temurin/releases/) 
- **macOS**: `brew install openjdk@17` or download from Adoptium
- **Linux**: `sudo apt install openjdk-17-jre` (Ubuntu/Debian)

### Step 3: Configure Firewall
**Windows:**
- Allow `java.exe` through Windows Defender Firewall
- Or manually allow ports 8080 and 8081

**macOS:**
- System Preferences → Security & Privacy → Firewall → Allow java

**Linux:**
```bash
sudo ufw allow 8080
sudo ufw allow 8081
```

### Step 4: Start Cuhlippa

**Server (run on one device):**
```bash
# Windows
start-server.bat

# macOS/Linux  
./start-server.sh
```

**Client (run on all devices):**
```bash
# Windows
start-client.bat

# macOS/Linux
./start-client.sh  
```

### Step 5: Connect Devices
1. Open client on each device
2. Go to **Settings → Sync → Enable Sync**
3. Click **"🔍 Discover Devices"**
4. Select your server and click **"Connect"**
5. Start copying - clipboard now syncs across all devices!

## 🔧 Advanced Installation

### Custom Server Configuration
1. Copy `config/server-config-template.properties`
2. Modify settings as needed
3. Place in server directory
4. Restart server

### Client Settings
1. Copy `config/client-settings-template.json` 
2. Customize theme, history size, etc.
3. Place in client `config/` directory

### Running as Service (Linux)
Create systemd service file:
```ini
[Unit]
Description=Cuhlippa Sync Server
After=network.target

[Service]
Type=simple
User=cuhlippa
WorkingDirectory=/opt/cuhlippa
ExecStart=/usr/bin/java -jar cuhlippa-server.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

## 🚨 Troubleshooting

### Discovery Not Working
1. **Firewall**: Ensure ports 8080/8081 are open
2. **Network**: Some WiFi networks block multicast
3. **Manual**: Use direct IP if auto-discovery fails

### macOS Permission Issues
```bash
# Grant accessibility permissions
System Preferences → Security & Privacy → Privacy → Accessibility
# Add Java application
```

### Linux Clipboard Issues
```bash
# Install X11 clipboard support
sudo apt install xclip xsel
```

## 📱 Multiple Network Setup

### Corporate Networks
- Discovery may be blocked by enterprise firewalls
- Use manual IP entry: `ws://SERVER_IP:8080/sync`
- Contact IT for firewall exceptions

### VPN Networks
- Ensure all devices on same VPN subnet
- May need manual IP configuration
- Check VPN multicast support

## 🎯 Performance Optimization

### Large Image Handling
- Default limit: 10MB per image
- Adjust in server configuration if needed
- Consider network bandwidth

### Memory Tuning
```bash
# Increase memory for large images
java -Xmx1g -jar cuhlippa-client.jar
java -Xmx2g -jar cuhlippa-server.jar
```

## 🔄 Updating

1. Stop all Cuhlippa processes
2. Backup configuration files
3. Download new release
4. Extract over existing installation
5. Restore configuration files
6. Restart

## 🆘 Getting Help

- **Documentation**: See README.md
- **Issues**: GitHub Issues
- **Logs**: Check console output for errors
- **Network**: Use `telnet SERVER_IP 8080` to test connectivity
