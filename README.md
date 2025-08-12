# Cuhlippa - Clipboard History Manager

A Java-based clipboard history manager that tracks and displays your clipboard contents with a clean Swing GUI.

## Features

- **Real-time clipboard monitoring** - Automatically captures text, images, and file paths
- **History viewer** - Browse through your clipboard history with a user-friendly interface
- **Image support** - View images directly in the application with automatic scaling
- **Duplicate detection** - Prevents saving duplicate clipboard entries
- **SQLite storage** - Persistent storage of clipboard history using SQLite database

## Screenshots

- List view showing clipboard history with timestamps
- Text preview for text content
- Image viewer for copied images
- Automatic refresh functionality

## Requirements

- Java 17+
- Maven 3.6+

## Getting Started

### Building the Project

```bash
mvn clean compile
```

### Running the Application

```bash
mvn exec:java -Dexec.mainClass="com.cuhlippa
```