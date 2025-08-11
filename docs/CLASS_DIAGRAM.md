```
+-------------------+
| ClipboardManager  |
+-------------------+
| - history: List<ClipboardItem>
| - db: LocalDatabase
| - sync: SyncService
+-------------------+
| + startListening()|
| + addItem()       |
| + searchHistory() |
+-------------------+

+-------------------+
| ClipboardItem     |
+-------------------+
| - type: ItemType  |
| - content: byte[] |
| - timestamp: Date |
| - hash: String    |
+-------------------+

+-------------------+
| LocalDatabase     |
+-------------------+
| - connection: Connection
+-------------------+
| + saveItem()      |
| + getItems()      |
| + searchItems()   |
+-------------------+

+-------------------+
| SyncService       |
+-------------------+
| - socket: WebSocketClient
| - encryptor: Encryptor
+-------------------+
| + connect()       |
| + sendItem()      |
| + receiveItems()  |
+-------------------+

+-------------------+
| Encryptor         |
+-------------------+
| - key: SecretKey  |
+-------------------+
| + encrypt()       |
| + decrypt()       |
+-------------------+
