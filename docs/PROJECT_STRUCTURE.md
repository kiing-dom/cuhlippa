
```plaintext
clipboard-manager/
├── client/
│   ├── src/main/java/com/cm/client/
│   │   ├── Main.java                # JavaFX entry point
│   │   ├── clipboard/               # Clipboard handling
│   │   │   ├── ClipboardManager.java
│   │   │   ├── ClipboardItem.java
│   │   │   ├── ItemType.java
│   │   └── storage/                 # Local DB
│   │       ├── LocalDatabase.java
│   │       └── migrations/
│   │
│   ├── resources/                   # FXML, CSS, icons
│   └── build.gradle or pom.xml
│
├── server/
│   ├── src/main/java/com/cm/server/
│   │   ├── ServerApplication.java   # Spring Boot entry point
│   │   ├── controller/
│   │   ├── service/
│   │   ├── model/
│   │   ├── websocket/
│   ├── resources/
│   └── build.gradle or pom.xml
│
├── common/                          # Shared DTOs and utils
│   ├── src/main/java/com/cm/common/
│   │   ├── dto/
│   │   ├── encryption/
│   │   └── util/
│
├── docs/
│   ├── architecture-diagram.png
│   ├── class-diagram.png
│   └── milestones.md
│
├── README.md
└── .gitignore
```