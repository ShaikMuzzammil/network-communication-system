# Java File Transfer Service - SocketComm Platform

This module provides enterprise-grade TCP socket file transfer capabilities built entirely in Java using standard library sockets. The service supports both server (file hosting) and client (file downloading) modes with comprehensive error handling and progress tracking.

## Module Overview

The File Transfer Service is a Maven-based Java project that implements reliable file distribution over TCP networks. It features a clean API design with Builder pattern configuration, making it suitable for both command-line usage and programmatic integration.

## Package Structure

```
com.socketcomm.filetransfer/
├── FileTransferApplication.java    # Main entry point, CLI argument parsing
├── server/
│   └── FileServer.java            # TCP server for file hosting
├── client/
│   └── FileClient.java            # TCP client for file downloading
├── config/
│   ├── ServerConfig.java          # Server configuration (Builder pattern)
│   └── ClientConfig.java          # Client configuration (Builder pattern)
└── utils/
    └── TransferProgress.java      # Progress tracking utility
```

## Key Features

### Server Capabilities (`FileServer`)
- **High-Performance Streaming**: Configurable buffer sizes (default 4KB) for optimal throughput
- **Single-File Hosting**: Serve one file per server instance for simplicity and reliability
- **Progress Tracking**: Real-time transfer progress with percentage completion
- **Graceful Shutdown**: Clean resource cleanup on termination
- **Validation**: Pre-transfer file existence verification

### Client Capabilities (`FileClient`)
- **Automatic Directory Creation**: Output directories created as needed
- **Timeout Handling**: Configurable connection timeouts prevent indefinite blocking
- **TCP Optimization**: Nagle's algorithm disabled for improved responsiveness
- **Progress Display**: Human-readable byte counts during download
- **Error Recovery**: Meaningful error messages for troubleshooting

### Configuration System
Both server and client utilize the **Builder Pattern** for flexible, readable configuration:

```java
// Server configuration example
ServerConfig config = new ServerConfig.Builder()
    .port(8080)
    .fileName("large_dataset.zip")
    .bufferSize(8192)  // 8KB buffer for high-bandwidth networks
    .build();

// Client configuration example
ClientConfig config = new ClientConfig.Builder()
    .serverHost("192.168.1.100")
    .serverPort(8080)
    .outputFile("download.zip")
    .connectTimeout(15)  // 15 second timeout
    .bufferSize(8192)
    .build();
```

## Building the Project

### Prerequisites
- **Java Development Kit (JDK)** 11 or higher
- **Apache Maven** 3.6 or higher

### Build Commands

```bash
# Navigate to the module
cd java-modules/file-transfer-service

# Clean build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Output location
# target/file-transfer-service-1.0.0.jar
```

## Usage

### Command-Line Interface

#### Starting the Server
```bash
# Basic usage (default port 5000, serves sample.txt)
java -jar target/file-transfer-service-1.0.0.jar server

# Custom configuration
java -jar target/file-transfer-service-1.0.0.jar server \
    --port 8080 \
    --file document.pdf

# Short options
java -jar target/file-transfer-service-1.0.0.jar server -p 8080 -f document.pdf
```

#### Downloading Files (Client)
```bash
# Basic usage (connects to localhost:5000)
java -jar target/file-transfer-service-1.0.0.jar client

# Custom configuration
java -jar target/file-transfer-service-1.0.0.jar client \
    --host 192.168.1.100 \
    --port 8080 \
    --output downloaded_file.pdf

# Short options
java -jar target/file-transfer-service-1.0.0.jar client -H 192.168.1.100 -p 8080 -o output.pdf
```

#### Help & Version
```bash
# Display help information
java -jar target/file-transfer-service-1.0.0.jar --help

# Show version
java -jar target/file-transfer-service-1.0.0.jar --version
```

### Programmatic API

#### As a Library
```java
import com.socketcomm.filetransfer.server.FileServer;
import com.socketcomm.filetransfer.client.FileClient;
import com.socketcomm.filetransfer.config.ServerConfig;
import com.socketcomm.filetransfer.config.ClientConfig;

// Server-side integration
public class MyApplication {
    public void hostFile(String filePath, int port) {
        ServerConfig config = new ServerConfig.Builder()
            .port(port)
            .fileName(filePath)
            .build();
        
        FileServer server = new FileServer(config);
        boolean success = server.start(); // Blocks until transfer complete
        
        if (success) {
            System.out.println("File transferred successfully!");
        }
    }
}

// Client-side integration
public class Downloader {
    public void downloadFile(String host, int port, String outputFile) {
        ClientConfig config = new ClientConfig.Builder()
            .serverHost(host)
            .serverPort(port)
            .outputFile(outputFile)
            .build();
        
        FileClient client = new FileClient(config);
        boolean success = client.connectAndReceive();
        
        if (success) {
            System.out.println("Download complete!");
        }
    }
}
```

## Console Output Examples

### Successful Server Session
```
╔══════════════════════════════════════════════╗
║      SocketComm File Transfer Service       ║
║              Version 1.0.0                  ║
╚══════════════════════════════════════════════╝

Starting in SERVER mode...
  Port: 5000
  File: presentation.pdf

═══════════════════════════════════════════════════
SocketComm File Server starting...
  Time: 2026-07-24 14:30:00
  Port: 5000
  File: presentation.pdf
  Buffer Size: 4096 bytes
═══════════════════════════════════════════════════
  File Size: 15728640 bytes (15.0 MB)

✓ Server started on port 5000
⏳ Waiting for client...

✓ Client connected from 192.168.1.100
📤 Starting file transfer...

  Progress: 0.0% (0 B / 15.0 MB)
  Progress: 10.0% (1.5 MB / 15.0 MB)
  ...
  Progress: 100.0% (15.0 MB / 15.0 MB)
Transfer completed at: 2026-07-24 14:30:45

✓ File sent successfully!
```

### Successful Client Session
```
╔══════════════════════════════════════════════╗
║      SocketComm File Transfer Service       ║
║              Version 1.0.0                  ║
╚══════════════════════════════════════════════╝

Starting in CLIENT mode...
  Server: 192.168.1.100:5000
  Output: received_presentation.pdf

═══════════════════════════════════════════════════
SocketComm File Client starting...
  Time: 2026-07-24 14:30:15
  Target: 192.168.1.100:5000
  Output: received_presentation.pdf
  Buffer Size: 4096 bytes
═══════════════════════════════════════════════════

✓ Connected to server 192.168.1.100:5000
📥 Receiving file...

  Received: 512 KB
  Received: 1.5 MB
  ...
  Received: 15.0 MB
Transfer completed at: 2026-07-24 14:30:45

✓ File received successfully!
📁 Saved as: received_presentation.pdf
📊 Size: 15.0 MB
```

## Error Handling

The service provides clear, actionable error messages for common failure scenarios:

| Error Type | Cause | User Message |
|------------|-------|--------------|
| BindException | Port already in use | "Port X is already in use" |
| FileNotFoundException | File not found on server | "File not found - filename" |
| ConnectException (timeout) | Server unreachable | "Connection timed out after X seconds" |
| ConnectException (refused) | Server not running | "Connection refused" |
| IOException | Network I/O failure | "I/O error during transfer" |

## Technical Specifications

### Default Configuration
| Parameter | Server Default | Client Default | Range |
|-----------|---------------|----------------|-------|
| Port | 5000 | 5000 (via host) | 1024-65535 |
| Buffer Size | 4096 bytes | 4096 bytes | 512-65536 |
| Host | 0.0.0.0 (all) | 127.0.0.1 | Valid IP/hostname |
| Timeout | N/A | 10 seconds | 1-300 seconds |

### Performance Characteristics
- **Throughput**: Limited by network bandwidth and buffer size
- **Memory Usage**: O(buffer_size) per active transfer
- **Concurrency**: Single-file serving (one client at a time)
- **Latency**: Minimal; direct socket streaming without protocol overhead

## Docker Deployment

The SocketComm platform includes containerization support:

```bash
# Build Java image
docker build -f docker/Dockerfile.java -t socketcomm/file-transfer .

# Run server container
docker run -p 5000:5000 -v /path/to/files:/data socketcomm/file-transfer server --file /data/document.pdf
```

See `docker/Dockerfile.java` for multi-stage build configuration.

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FileServerTest

# Integration test (requires manual setup)
# 1. Start server in one terminal
# 2. Run client in another terminal
```

## Technical Analysis Documentation

Comprehensive technical analysis is available in PDF format:

**[Java File Transfer Service Technical Analysis](../../docs/analysis/java-file-transfer/Java_File_Transfer_Service_Technical_Analysis.pdf)**

This document covers:
- Architecture overview and package structure
- Server/client implementation details
- Configuration system (Builder pattern)
- CLI interface specification
- Error handling strategies
- Progress tracking system
- Build and deployment procedures

**Screenshot Guide:** `../../docs/analysis/java-file-transfer/screenshots/SCREENSHOT_GUIDE.md`

## Dependencies

| Dependency | Scope | Purpose |
|-------------|-------|---------|
| JRE (Runtime) | Required | Java execution environment |
| Maven (Build) | Build time | Compilation and packaging |

No external runtime dependencies - uses only Java standard library.

## Integration Examples

### Embedded in Spring Boot Application
```java
@Service
public class FileDistributionService {
    
    @Value("${file.server.port:5000}")
    private int serverPort;
    
    @Value("${file.storage.location:/tmp/uploads}")
    private String storageLocation;
    
    public void serveFile(String filename) {
        Path filePath = Paths.get(storageLocation, filename);
        
        ServerConfig config = new ServerConfig.Builder()
            .port(serverPort)
            .fileName(filePath.toString())
            .build();
            
        // Run in background thread
        CompletableFuture.runAsync(() -> {
            FileServer server = new FileServer(config);
            server.start();
        });
    }
}
```

### Batch Download Utility
```java
public class BatchDownloader {
    
    private final String serverHost;
    private final int serverPort;
    private final String outputDir;
    
    public List<String> downloadMultiple(List<String> filenames) {
        List<String> downloaded = new ArrayList<>();
        
        for (String filename : filenames) {
            String outputPath = Paths.get(outputDir, filename).toString();
            
            ClientConfig config = new ClientConfig.Builder()
                .serverHost(serverHost)
                .serverPort(serverPort)
                .outputFile(outputPath)
                .build();
                
            FileClient client = new FileClient(config);
            if (client.connectAndReceive()) {
                downloaded.add(outputPath);
            }
        }
        
        return downloaded;
    }
}
```

## Related Documentation

- [Main Project README](../../README.md)
- [Technical Analysis PDF](../../docs/analysis/java-file-transfer/Java_File_Transfer_Service_Technical_Analysis.pdf)
- [Docker Deployment Guide](../../docker/README.md)
- [CI/CD Pipeline](../../.github/workflows/ci-java.yml)

---

**Last Updated:** July 2026  
**Module Version:** 1.0.0  
**Minimum Java Version:** 11  
**Build Tool:** Maven 3.6+
