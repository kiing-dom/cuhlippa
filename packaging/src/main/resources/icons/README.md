# Cuhlippa Application Icons

This directory contains the application icons for different platforms.

## Required Icon Files

To complete the installer setup, you need to create the following icon files:

### Windows
- `cuhlippa.ico` - Windows icon file containing multiple sizes (16x16, 32x32, 48x48, 64x64, 128x128, 256x256)

### macOS  
- `cuhlippa.icns` - macOS icon bundle containing multiple resolutions

### Linux
- `cuhlippa.png` - High-resolution PNG (preferably 512x512 or 1024x1024)

## Creating Icons

You can use tools like:
- **GIMP** (free) - Can export to .ico and create .icns files
- **ImageMagick** - Command line tool for batch conversion
- **Online converters** - For quick conversions
- **Icon editors** - Like Greenfish Icon Editor Pro (Windows)

## Placeholder Icons

For now, you can use any square image and convert it to the required formats to test the installer build process. The packaging will work without icons, but they're recommended for a professional appearance.

## Installation

Once you have the icon files, place them in this directory:
- `packaging/src/main/resources/icons/cuhlippa.ico`
- `packaging/src/main/resources/icons/cuhlippa.icns` 
- `packaging/src/main/resources/icons/cuhlippa.png`
