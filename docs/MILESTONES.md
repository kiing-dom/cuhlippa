### **Milestone 1 — Local MVP**
**Goal:** Local clipboard history with search.
- [ ] Implement `ClipboardItem` and `ItemType` enums.
- [ ] Implement `ClipboardManager`:
  - Detect clipboard changes (text/images).
  - Store new items in `LocalDatabase`.
- [ ] Set up SQLite DB with migration script.
- [ ] Build JavaFX UI:
  - List view of clipboard items.
  - Search bar for filtering history.
- [ ] Implement basic encryption for local storage.

---

### **Milestone 2 — Sync-Ready**
**Goal:** Send and receive encrypted clipboard items via cloud.
- [ ] Create `SyncService` in client.
- [ ] Implement minimal Spring Boot WebSocket server:
  - Auth with API token.
  - Broadcast updates to connected devices of same user.
- [ ] Implement end-to-end encryption using shared secret key.
- [ ] Handle sync conflicts by timestamp.

---

### **Milestone 3 — Polish & Features**
**Goal:** Make it production-ready & appealing for GitHub stars.
- [ ] Add tagging and categorization.
- [ ] Add pinned items.
- [ ] Snippet templates.
- [ ] Hotkeys for quick copy/paste.
- [ ] Cross-platform packaging with `jlink` or `jpackage`.
- [ ] Dark mode and theming.

---

## 3. Tech Stack Choices

### **Client**
- Java 21 (or latest LTS)
- JavaFX for UI
- SQLite (via JDBC)
- JNativeHook for system clipboard events (more reliable than AWT on some OS)
- AES/GCM for encryption

### **Server**
- Java 21
- Spring Boot 3
- WebSocket (Spring Messaging)
- PostgreSQL
- JWT for auth (API tokens)

---

## 4. Development Flow
1. Start with **Milestone 1** — get the clipboard history working locally.
2. Once stable, **spin up the server** and implement sync.
3. Only after sync is smooth, **add the “fun” features** (tags, templates, hotkeys).
4. Keep docs/screenshots updated for GitHub appeal.

---

## 5. Future Extensions
- Android app for mobile sync.
- OCR for images to make text searchable.
- Cloudless P2P sync mode.
- Self-hostable server with Docker.

---
```