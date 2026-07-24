# SocketComm API Reference Documentation

## Overview

SocketComm provides a comprehensive socket-based communication API for real-time messaging and file transfer. This document details all available APIs, their parameters, return values, and usage examples.

---

## Table of Contents

1. [Python Chat Core API](#python-chat-core-api)
2. [Python GUI Client API](#python-gui-client-api)
3. [Java File Transfer API](#java-file-transfer-api)
4. [Protocol Specification](#protocol-specification)
5. [Error Codes](#error-codes)
6. [Events & Callbacks](#events--callbacks)

---

## Python Chat Core API

### ChatServer Class

The `ChatServer` class provides a multi-threaded TCP socket server for handling chat connections.

#### Constructor

```python
ChatServer(
    host: str = '0.0.0.0',
    port: int = 5000,
    max_clients: int = 10,
    buffer_size: int = 4096,
    encoding: str = 'utf-8'
)
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `host` | `str` | `'0.0.0.0'` | Server bind address. Use `'localhost'` for local only |
| `port` | `int` | `5000` | Port number to listen on (1-65535) |
| `max_clients` | `int` | `10` | Maximum simultaneous client connections |
| `buffer_size` | `int` | `4096` | Socket receive buffer size in bytes |
| `encoding` | `str` | `'utf-8'` | Character encoding for message transmission |

**Example:**
```python
from chat_core.server import ChatServer

server = ChatServer(
    host='0.0.0.0',
    port=5000,
    max_clients=50,
    buffer_size=8192
)
```

#### Methods

##### `start() -> None`

Starts the server and begins accepting connections.

```python
server.start()
# Server is now running and accepting clients
```

**Raises:**
- `OSError`: If port is already in use or binding fails
- `PermissionError`: If port < 1024 without root privileges

##### `stop() -> None`

Gracefully stops the server and disconnects all clients.

```python
server.stop()
# All connections closed, server shutdown complete
```

##### `broadcast(message: str, exclude_client=None) -> int`

Sends a message to all connected clients.

**Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `message` | `str` | Message content to broadcast |
| `exclude_client` | `socket.socket \| None` | Client socket to exclude from broadcast |

**Returns:** `int` - Number of clients that received the message

**Example:**
```python
count = server.broadcast("Server: Maintenance in 5 minutes")
print(f"Notified {count} clients")
```

##### `send_private_message(client_socket, message: str) -> bool`

Sends a message to a specific client.

**Returns:** `bool` - True if message sent successfully

##### `get_connected_clients() -> List[dict]`

Returns information about all connected clients.

**Returns:** List of dictionaries containing:
- `address`: Client IP and port tuple
- `connected_since`: Timestamp of connection
- `message_count`: Messages sent by this client

##### `get_server_stats() -> dict`

Returns server statistics.

```python
stats = server.get_server_stats()
# {
#     'total_connections': 150,
#     'active_connections': 5,
#     'messages_sent': 1250,
#     'uptime_seconds': 3600,
#     'start_time': '2024-01-15T10:30:00'
# }
```

---

### ChatClient Class

The `ChatClient` class handles connection to a ChatServer with auto-reconnection support.

#### Constructor

```python
ChatClient(
    host: str = 'localhost',
    port: int = 5000,
    buffer_size: int = 4096,
    encoding: str = 'utf-8',
    reconnect_enabled: bool = True,
    max_reconnect_attempts: int = 5,
    reconnect_delay: float = 1.0
)
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `host` | `str` | `'localhost'` | Server address to connect to |
| `port` | `int` | `5000` | Server port number |
| `buffer_size` | `int` | `4096` | Receive buffer size |
| `encoding` | `str` | `'utf-8'` | Message encoding |
| `reconnect_enabled` | `bool` | `True` | Enable automatic reconnection |
| `max_reconnect_attempts` | `int` | `5` | Max reconnection tries before giving up |
| `reconnect_delay` | `float` | `1.0` | Initial delay between retries (doubles each attempt) |

#### Methods

##### `connect() -> bool`

Establishes connection to the server.

**Returns:** `bool` - True if connection successful

**Example:**
```python
client = ChatClient('192.168.1.100', 5000)
if client.connect():
    print("Connected successfully!")
else:
    print("Connection failed")
```

##### `disconnect() -> None`

Closes the connection to the server.

##### `send_message(message: str) -> bool`

Sends a text message to the server.

**Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `message` | `str` | Message content (max 64KB) |

**Returns:** `bool` - True if sent successfully

##### `receive_message() -> str \| None`

Waits for and returns the next message from server.

**Returns:** Message string or None if disconnected

##### `set_callback(event_type: str, callback: Callable) -> None`

Registers an event callback function.

**Event Types:** `'message'`, `'connect'`, `'disconnect'`, `'error'`

**Example:**
```python
def on_message(msg):
    print(f"Received: {msg}")

client.set_callback('message', on_message)
```

---

## Python GUI Client API

### ServerGUI Class

The main server control center interface built with CustomTkinter.

#### Initialization

```python
from gui_client import ServerGUI

app = ServerGUI()
app.mainloop()
```

#### Configuration Options

| Option | Type | Description |
|--------|------|-------------|
| `window_title` | `str` | Window title bar text |
| `window_size` | `tuple` | Initial (width, height) in pixels |
| `theme` | `str` | Color theme: `'dark-blue'`, `'dark-green'` |
| `log_level` | `str` | Logging verbosity |

#### Public Methods

##### `start_server(host, port) -> bool`

Starts the backend ChatServer with GUI parameters.

##### `stop_server() -> None`

Stops the running server.

##### `log_message(level, message) -> None`

Adds an entry to the GUI log display.

##### `update_client_list(clients) -> None`

Refreshes the connected clients panel.

##### `show_notification(title, message) -> None`

Displays a desktop notification (platform-dependent).

---

### ClientGUI Class

The client workstation interface for end users.

#### Initialization

```python
from gui_client import ClientGUI

app = ClientGUI()
app.mainloop()
```

#### Key Features

| Feature | Method | Description |
|---------|--------|-------------|
| Connection | `connect_to_server(host, port)` | Establishes server connection |
| Messaging | `send_chat_message(text)` | Sends message to chat |
| History | `load_history()` | Loads saved conversation history |
| Settings | `open_settings()` | Opens configuration dialog |
| Status | `update_status(online)` | Updates online indicator |

---

## Java File Transfer API

### FileServer Class

Java-based file transfer server using TCP sockets with streaming IO.

#### Builder Pattern Configuration

```java
FileServer server = new FileServer.Builder()
    .port(8080)
    .saveDirectory("/path/to/save")
    .bufferSize(8192)
    .maxConcurrentTransfers(5)
    .timeout(30000)
    .build();
```

#### Builder Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `port` | `int` | `8080` | Listening port |
| `saveDirectory` | `String` | `"./received"` | Directory for received files |
| `bufferSize` | `int` | `8192` | Transfer buffer size in bytes |
| `maxConcurrentTransfers` | `int` | `5` | Simultaneous transfer limit |
| `timeout` | `int` | `30000` | Connection timeout in milliseconds |

#### Methods

##### `start() throws IOException`

Starts the file server and begins listening for connections.

```java
try {
    server.start();
    System.out.println("File server running on port 8080");
} catch (IOException e) {
    System.err.println("Failed to start: " + e.getMessage());
}
```

##### `stop() -> void`

Gracefully shuts down the server.

##### `getActiveTransfers() -> List<TransferProgress>`

Returns list of ongoing file transfers.

##### `getStatistics() -> ServerStats`

Returns cumulative transfer statistics.

---

### FileClient Class

Client for uploading files to FileServer.

#### Configuration

```java
FileClient client = new FileClient.Builder()
    .serverHost("192.168.1.100")
    .serverPort(8080)
    .bufferSize(8192)
    .retryCount(3)
    .build();
```

#### Methods

##### `uploadFile(File file) -> TransferResult`

Uploads a file to the server.

**Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `file` | `File` | File object to upload |

**Returns:** `TransferResult` containing:
- `success`: Boolean status
- `bytesTransferred`: Total bytes sent
- `durationMs`: Transfer time in milliseconds
- `errorMessage`: Failure reason (if any)

**Example:**
```java
File file = new File("document.pdf");
TransferResult result = client.uploadFile(file);

if (result.isSuccess()) {
    System.out.printf("Uploaded %d bytes in %d ms%n",
        result.getBytesTransferred(),
        result.getDurationMs());
} else {
    System.err.println("Upload failed: " + result.getErrorMessage());
}
```

##### `uploadFile(String filePath) -> TransferResult`

Convenience method accepting file path string.

---

## Protocol Specification

### Message Frame Format

All SocketComm communications use a consistent binary frame format:

```
+------------------+------------------+------------------+
| Header (4 bytes) | Length (4 bytes) | Payload (N bytes)|
+------------------+------------------+------------------+
```

**Header Structure:**

| Field | Size | Description |
|-------|------|-------------|
| Magic Bytes | 2 bytes | `0xCDAB` - Protocol identifier |
| Version | 1 byte | Protocol version (current: `0x01`) |
| Flags | 1 byte | Message type flags |
| Length | 4 bytes | Payload length (big-endian) |
| Payload | N bytes | Variable-length data |

### Message Types (Flags)

| Value | Type | Direction | Description |
|-------|------|-----------|-------------|
| `0x01` | CONNECT | Client->Server | Connection request |
| `0x02` | CONNECT_ACK | Server->Client | Connection accepted |
| `0x03` | MESSAGE | Bidirectional | Text message |
| `0x04` | PRIVATE_MSG | Bidirectional | Private/direct message |
| `0x05` | BROADCAST | Server->Clients | Server announcement |
| `0x06` | FILE_META | Client->Server | File upload metadata |
| `0x07` | FILE_DATA | Client->Server | File chunk data |
| `0x08` | FILE_ACK | Server->Client | File receipt confirmation |
| `0x09` | DISCONNECT | Bidirectional | Graceful disconnect |
| `0x0A` | PING | Bidirectional | Keep-alive heartbeat |
| `0x0B` | PONG | Bidirectional | Heartbeat response |
| `0x0C` | ERROR | Server->Client | Error notification |

### File Transfer Protocol

File transfers use a separate sub-protocol optimized for large binary data:

```
+--------+----------+----------+----------+---------+
| Opcode | File ID  | Chunk #  | Total Chunks| Data   |
| 1 byte | 16 bytes | 4 bytes  | 4 bytes   | Variable|
+--------+----------+----------+----------+---------+
```

**Opcodes:**

| Opcode | Name | Description |
|--------|------|-------------|
| `0x01` | INIT | Initialize transfer (includes filename, size) |
| `0x02` | DATA | File chunk data |
| `0x03` | ACK | Acknowledge chunk receipt |
| `0x04` | COMPLETE | Transfer completed |
| `0x05` | CANCEL | Cancel transfer |
| `0x06` | ERROR | Transfer error |

---

## Error Codes

### Python Error Codes

| Code | Name | Description |
|------|------|-------------|
| `1001` | CONNECTION_REFUSED | Server not accepting connections |
| `1002` | CONNECTION_TIMEOUT | Connection timed out |
| `1003` | AUTHENTICATION_FAILED | Invalid credentials |
| `1004` | MESSAGE_TOO_LARGE | Exceeds 64KB limit |
| `1005` | RATE_LIMIT_EXCEEDED | Too many requests |
| `1006` | SERVER_FULL | Maximum clients reached |
| `1007` | ALREADY_CONNECTED | Duplicate connection attempt |
| `1008` | NOT_CONNECTED | Operation requires active connection |

### Java Error Codes

| Code | Exception | Description |
|------|-----------|-------------|
| `2001` | `FileNotFoundException` | Source file does not exist |
| `2002` | `PermissionException` | Insufficient file permissions |
| `2003` | `DiskSpaceException` | Insufficient storage space |
| `2004` | `TransferInterruptedException` | Transfer cancelled by user |
| `2005` | `ChecksumMismatchException` | Data integrity verification failed |
| `2006` | `ConnectionResetException` | Server closed connection |
| `2007` | `TimeoutException` | Operation timed out |

---

## Events & Callbacks

### Server Events

| Event | Trigger | Callback Signature |
|-------|---------|-------------------|
| `on_client_connect` | New client connects | `(client_address, client_id)` |
| `on_client_disconnect` | Client leaves | `(client_address, message_count)` |
| `on_message_received` | Message arrives | `(client_id, message_text)` |
| `on_broadcast_sent` | Broadcast completes | `(recipient_count, message)` |
| `on_error` | Error occurs | `(error_code, description)` |
| `on_server_start` | Server starts | `(bind_address, port)` |
| `on_server_stop` | Server stops | `(total_connections_served)` |

### Client Events

| Event | Trigger | Callback Signature |
|-------|---------|-------------------|
| `on_connect` | Connected to server | `(server_address)` |
| `on_disconnect` | Lost connection | `(reason, reconnecting)` |
| `on_message` | Message received | `(sender, message, timestamp)` |
| `on_private_message` | DM received | `(sender, message)` |
| `on_user_joined` | User joined chat | `(username, user_count)` |
| `on_user_left` | User left chat | `(username, user_count)` |
| `on_reconnect` | Reconnection success | `(attempt_number)` |
| `on_error` | Error occurred | `(error_code, recoverable)` |

### File Transfer Events

| Event | Trigger | Callback Signature |
|-------|---------|-------------------|
| `on_transfer_start` | Transfer initiated | `(filename, total_size)` |
| `on_progress` | Chunk transferred | `(bytes_sent, percent)` |
| `on_complete` | Transfer finished | `(filename, duration_ms)` |
| `on_error` | Transfer error | `(error_code, message)` |
| `on_pause` | Transfer paused | `(bytes_completed)` |
| `on_resume` | Transfer resumed | `(bytes_completed)` |

---

## Usage Examples

### Complete Chat Application Example

```python
from chat_core.server import ChatServer
from chat_core.client import ChatClient
import threading
import time

# Start server in background thread
server = ChatServer(host='localhost', port=5000, max_clients=5)

def run_server():
    server.start()

server_thread = threading.Thread(target=run_server, daemon=True)
server_thread.start()
time.sleep(1)  # Let server initialize

# Connect multiple clients
clients = []
for i in range(3):
    client = ChatClient(f'Client-{i}', port=5000)
    client.connect()
    
    def make_handler(name):
        def handler(msg):
            print(f"[{name}] Received: {msg}")
        return handler
    
    client.set_callback('message', make_handler(f'Client-{i}'))
    clients.append(client)

# Send messages
clients[0].send_message("Hello everyone!")
clients[1].send_message("Hi from client 1!")

# Cleanup
for client in clients:
    client.disconnect()
server.stop()
```

### File Upload Example (Java)

```java
import com.socketcomm.filetransfer.server.FileServer;
import com.socketcomm.filetransfer.client.FileClient;
import java.io.File;

public class TransferExample {
    public static void main(String[] args) {
        // Start server
        FileServer server = new FileServer.Builder()
            .port(9000)
            .saveDirectory("./downloads")
            .build();
        
        server.start();
        
        // Upload file
        FileClient client = new FileClient.Builder()
            .serverHost("localhost")
            .serverPort(9000)
            .build();
        
        File file = new File("large_video.mp4");
        var result = client.uploadFile(file);
        
        System.out.println("Status: " + (result.isSuccess() ? "Success" : "Failed"));
        System.out.println("Speed: " + 
            (result.getBytesTransferred() / 1024 / 1024) / 
            (result.getDurationMs() / 1000.0) + " MB/s");
        
        server.stop();
    }
}
```

---

## Rate Limiting & Throttling

To prevent abuse, SocketComm implements rate limiting:

| Operation | Limit | Window |
|-----------|-------|--------|
| Messages per client | 100 | 1 minute |
| Connections per IP | 10 | 1 minute |
| File uploads per client | 5 | 1 hour |
| Total bandwidth per client | 100 MB | 1 minute |

When limits are exceeded, error code `1005` (RATE_LIMIT_EXCEEDED) is returned with HTTP-like `429 Too Many Requests` semantics.

---

## Version Information

| Component | Version | Release Date |
|-----------|---------|--------------|
| Chat Core API | 1.2.0 | 2024-01-15 |
| GUI Client API | 1.1.0 | 2024-01-10 |
| File Transfer API | 2.0.0 | 2024-01-20 |
| Protocol | 1.1 | 2024-01-15 |

---

*Last Updated: January 2024*
