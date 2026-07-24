# Java File Transfer Service - Interface & Screen Documentation

## Overview
This document describes the terminal/console interfaces and operational screens for the SocketComm Java File Transfer Service, a command-line TCP file transfer application built with standard Java sockets.

## Console Interface Screenshots

### 1. Application Startup Banner
**File:** `startup_banner.png`  
**Description:** Initial CLI output when launching the application

**Display Content:**
```
╔══════════════════════════════════════════════╗
║      SocketComm File Transfer Service       ║
║              Version 1.0.0                  ║
╚══════════════════════════════════════════════╝
```
- Unicode box-drawing characters for professional appearance
- Centered title with version number
- Clean separator before usage instructions

### 2. Server Mode Startup
**File:** `server_start.png`  
**Command:** `java -jar file-transfer-service.jar server --port 5000 --file document.pdf`

**Console Output Sequence:**
```
Starting in SERVER mode...
  Port: 5000
  File: document.pdf

═══════════════════════════════════════════════════
SocketComm File Server starting...
  Time: 2026-07-24 14:30:00
  Port: 5000
  File: document.pdf
  Buffer Size: 4096 bytes
═══════════════════════════════════════════════════
  File Size: 2457600 bytes (2.3 MB)

✓ Server started on port 5000
⏳ Waiting for client...
```

**Key Information Displayed:**
- Mode confirmation (SERVER)
- Configuration summary (port, file, buffer)
- File validation result with human-readable size
- Status indicators (✓ for success, ⏳ for waiting)

### 3. Client Connection Received (Server Side)
**File:** `server_client_connected.png`

**Output:**
```
✓ Client connected from 192.168.1.100
📤 Starting file transfer...

  Progress: 0.0% (0 B / 2.3 MB)
  Progress: 5.2% (128 KB / 2.3 MB)
  Progress: 10.4% (256 KB / 2.3 MB)
  ...
```

**Visual Elements:**
- Checkmark + IP address for connection confirmation
- Upload emoji (📤) indicating direction
- Real-time progress bar using carriage return (\r)
- Percentage and absolute values displayed

### 4. Transfer Completion (Server Side)
**File:** `server_transfer_complete.png`

**Final Output:**
```
  Progress: 100.0% (2.3 MB / 2.3 MB)
Transfer completed at: 2026-07-24 14:30:45

✓ File sent successfully!
```

**Post-Transfer Information:**
- Final progress at 100%
- Completion timestamp
- Success/failure indicator

### 5. Client Mode Startup
**File:** `client_start.png`  
**Command:** `java -jar file-transfer-service.jar client --host 192.168.1.100 --port 5000 --output received_file.pdf`

**Console Output:**
```
Starting in CLIENT mode...
  Server: 192.168.1.100:5000
  Output: received_file.pdf

═══════════════════════════════════════════════════
SocketComm File Client starting...
  Time: 2026-07-24 14:30:15
  Target: 192.168.1.100:5000
  Output: received_file.pdf
  Buffer Size: 4096 bytes
═══════════════════════════════════════════════════
```

### 6. Download Progress (Client Side)
**File:** `client_download_progress.png`

**Active Transfer Display:**
```
✓ Connected to server 192.168.1.100:5000
📥 Receiving file...

  Received: 128 KB
  Received: 512 KB
  Received: 1.2 MB
  ...
```

**Progress Indicators:**
- Connection success confirmation
- Download emoji (📥) for direction
- Cumulative received bytes in human-readable format
- Periodic updates (every ~100KB by default)

### 7. Download Complete (Client Side)
**File:** `client_download_complete.png`

**Completion Output:**
```
  Received: 2.3 MB
Transfer completed at: 2026-07-24 14:30:45

✓ File received successfully!
📁 Saved as: received_file.pdf
📊 Size: 2.3 MB
```

**Verification Data:**
- Final received size
- Saved filename confirmation
- Actual file size on disk

## Error Handling Screenshots

### 8. Port Already In Use
**File:** `error_port_in_use.png`

**Error Display:**
```
[ERROR] Port 5000 already in use
Error: Port 5000 is already in use
```
- Clean error message without stack trace
- User-actionable information

### 9. File Not Found
**File:** `error_file_not_found.png`

**Server-Side Error:**
```
[ERROR] File not found: nonexistent.pdf
Error: File not found - nonexistent.pdf
```
- Early validation prevents confusing later errors
- Shows exact filename that failed

### 10. Connection Refused
**File:** `error_connection_refused.png`

**Client-Side Error:**
```
[ERROR] Connection failed: Connection timed out after 10 seconds
Error: Connection refused - Is the server running?
```
- Timeout value included for diagnostics
- Helpful suggestion for troubleshooting

### 11. Connection Timeout
**File:** `error_timeout.png`

**Timeout Error:**
```
[ERROR] Connection failed: Connection timed out after 10 seconds
```
- Configurable timeout value reflected
- Clear failure indication

## Help & Usage Screenshots

### 12. Help Display
**File:** `help_screen.png`  
**Command:** `java -jar file-transfer-service.jar --help`

**Usage Information:**
```
USAGE:
  java -jar file-transfer-service.jar <MODE> [OPTIONS]

MODES:
  server    Start in server mode (host files)
  client    Start in client mode (download files)

SERVER OPTIONS:
  -p, --port <port>    Port to listen on (default: 5000)
  -f, --file <file>    File to serve (default: sample.txt)

CLIENT OPTIONS:
  -H, --host <host>    Server address (default: 127.0.0.1)
  -p, --port <port>    Server port (default: 5000)
  -o, --output <file>  Output filename (default: received_file.bin)

EXAMPLES:
  java -jar file-transfer-service.jar server --port 8080
  java -jar file-transfer-service.jar client --host 192.168.1.100

For more information, visit: https://docs.socketcomm.dev
```

### 13. Version Display
**File:** `version_screen.png`  
**Command:** `java -jar file-transfer-service.jar --version`

**Output:**
```
SocketComm File Transfer Service v1.0.0
```

## Advanced Features Screenshots

### 14. Custom Buffer Size
**File:** `custom_buffer.png`  
**Command:** `java -jar ... server --port 8080 --buffer-size 8192`

**Configuration Display:**
```
  Buffer Size: 8192 bytes
```
- Shows custom buffer configuration
- Affects throughput vs memory tradeoff

### 15. Large File Transfer
**File:** `large_file_transfer.png`

**Progress Display for Large Files (>100MB):**
```
  Progress: 0.5% (512 KB / 100.0 MB)
  Progress: 1.0% (1.0 MB / 100.0 MB)
  ...
  Progress: 99.5% (99.5 MB / 100.0 MB)
```
- Demonstrates scalability
- Consistent progress reporting format

## Integration Examples

### 16. Programmatic Server Usage
**File:** `api_server_example.png`

**Java Code Example:**
```java
ServerConfig config = new ServerConfig.Builder()
    .port(8080)
    .fileName("large_dataset.zip")
    .bufferSize(8192)
    .build();

FileServer server = new FileServer(config);
boolean success = server.start();
```

### 17. Programmatic Client Usage
**File:** `api_client_example.png`

**Java Code Example:**
```java
ClientConfig config = new ClientConfig.Builder()
    .serverHost("192.168.1.100")
    .serverPort(8080)
    .outputFile("download.zip")
    .connectTimeout(15)
    .build();

FileClient client = new FileClient(config);
boolean success = client.connectAndReceive();
```

---

*All screenshots should be captured from actual terminal sessions with appropriate font (Monospace, 11-12pt) and window size (minimum 80×24 characters).*
