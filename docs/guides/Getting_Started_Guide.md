# SocketComm Getting Started Guide

## Welcome to SocketComm

SocketComm is a comprehensive socket-based communication platform that enables real-time messaging and file transfer capabilities. This guide will walk you through installation, configuration, and running your first application.

---

## Prerequisites

Before installing SocketComm, ensure your system meets these requirements:

### System Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| Operating System | Windows 10, Ubuntu 20.04, macOS 11 | Latest stable release |
| RAM | 2 GB | 4 GB or more |
| Disk Space | 500 MB | 2 GB (for development) |
| Network | Local network access | Internet for dependencies |

### Software Dependencies

**For Python Modules:**
- Python 3.9 or higher
- pip package manager

**For Java Module:**
- Java Development Kit (JDK) 11 or higher
- Apache Maven 3.6 or higher

**Optional:**
- Docker 20.10+ (for containerized deployment)
- Git (for cloning repository)

---

## Installation Methods

### Method 1: Clone from Repository (Recommended)

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/SocketComm.git
cd SocketComm

# Run setup script (handles everything)
chmod +x scripts/setup.sh
./scripts/setup.sh
```

The setup script automatically:
- Creates Python virtual environment
- Installs all Python dependencies
- Validates Java installation
- Builds Java modules with Maven
- Creates necessary directories
- Runs basic health checks

### Method 2: Manual Installation

#### Step 1: Setup Python Environment

```bash
cd python-modules

# Create virtual environment
python -m venv venv

# Activate environment
source venv/bin/activate  # Linux/Mac
# or
.\venv\Scripts\activate   # Windows

# Install dependencies
pip install -r gui-client/requirements.txt
```

**Required Python Packages:**
```
customtkinter>=5.2.0
threading-utils>=1.0.0
```

#### Step 2: Build Java Module

```bash
cd java-modules/file-transfer-service

# Clean and build with Maven
mvn clean install -DskipTests

# Or build with tests
mvn clean install
```

Maven will download all required dependencies including:
- SLF4J Logging API
- Logback Classic
- JUnit 5 (for testing)

---

## Quick Start: Your First Chat Application

Let's create a simple chat application in under 5 minutes.

### Step 1: Start the Server

Open Terminal 1:

```bash
cd SocketComm/python-modules/gui-client/src
python server.py
```

You should see:
```
============================================
    SocketComm Chat Server v1.0
============================================
[INFO] Starting server on 0.0.0.0:5000...
[INFO] Server started successfully
[INFO] Waiting for connections...
```

### Step 2: Connect a Client

Open Terminal 2:

```bash
cd SocketComm/python-modules/gui-client/src
python client.py
```

The GUI window will open with:
- Connection input fields
- Message display area
- Text input box
- Send button

### Step 3: Connect and Chat

1. In the client GUI, enter server address (`localhost`) and port (`5000`)
2. Click "Connect"
3. Type a message in the input field
4. Press Enter or click "Send"
5. See your message appear in the chat area!

---

## Running the File Transfer Service

### Starting the File Server

```bash
cd java-modules/file-transfer-service

# Run the compiled server
java -cp target/classes:target/dependency/* \
     com.socketcomm.filetransfer.server.FileServer
```

Server output:
```
============================================
    SocketComm File Transfer Service
============================================
[INFO] Binding to port 8080...
[INFO] Save directory: ./received
[INFO] Buffer size: 8192 bytes
[INFO] Max concurrent transfers: 5
[INFO] Server ready, waiting for files...
```

### Uploading a File

Using the Java client programmatically:

```java
import com.socketcomm.filetransfer.client.FileClient;
import java.io.File;

public class UploadExample {
    public static void main(String[] args) throws Exception {
        FileClient client = new FileClient.Builder()
            .serverHost("localhost")
            .serverPort(8080)
            .build();
        
        File file = new File("my_document.pdf");
        var result = client.uploadFile(file);
        
        if (result.isSuccess()) {
            System.out.println("File uploaded successfully!");
        }
    }
}
```

Or use the command-line interface:

```bash
java -cp target/classes:target/dependency/* \
     com.socketcomm.filetransfer.client.FileClient \
     --host localhost --port 8080 --file my_document.pdf
```

---

## Docker Deployment (All-in-One)

For quick deployment without manual setup:

### Using Docker Compose

```bash
cd SocketComm/docker

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

This starts:
- **Python Chat Server**: Port 5000
- **Java File Server**: Port 8080
- **Health Check Endpoint**: Port 8081

### Individual Container Commands

```bash
# Build Python image
docker build -f Dockerfile.python -t socketcomm/chat-server .

# Build Java image
docker build -f Dockerfile.java -t socketcomm/file-server .

# Run containers
docker run -d -p 5000:5000 --name chat-server socketcomm/chat-server
docker run -d -p 8080:8080 --name file-server socketcomm/file-server
```

---

## Configuration Guide

### Application Configuration

Edit `configs/app-config.yaml` to customize behavior:

```yaml
# Server Configuration
server:
  host: "0.0.0.0"
  port: 5000
  max_clients: 50
  timeout: 30
  
# Chat Settings
chat:
  max_message_size: 65536      # 64KB max message
  history_enabled: true
  history_max_messages: 1000
  welcome_message: "Welcome to SocketComm!"
  
# File Transfer Settings
file_transfer:
  enabled: true
  upload_dir: "./uploads"
  max_file_size: 1073741824     # 1GB
  allowed_extensions:
    - ".txt"
    - ".pdf"
    - ".jpg"
    - ".png"
    - ".zip"
    
# Logging
logging:
  level: INFO
  file: "logs/socketcomm.log"
  max_size: 10485760           # 10MB
  backup_count: 5
```

### Environment Variables

Override config using environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `SOCKETCOMM_HOST` | Server bind address | `0.0.0.0` |
| `SOCKETCOMM_PORT` | Server port | `5000` |
| `SOCKETCOMM_MAX_CLIENTS` | Max connections | `50` |
| `SOCKETCOMM_LOG_LEVEL` | Logging level | `INFO` |
| `FILE_TRANSFER_PORT` | File server port | `8080` |
| `FILE_UPLOAD_DIR` | Upload directory | `./uploads` |

Example:
```bash
export SOCKETCOMM_PORT=9000
export SOCKETCOMM_MAX_CLIENTS=100
python server.py
```

---

## Troubleshooting Common Issues

### Port Already in Use

**Error:** `OSError: [Errno 48] Address already in use`

**Solutions:**
```bash
# Find process using the port
lsof -i :5000        # Linux/Mac
netstat -ano | findstr :5000  # Windows

# Kill the process
kill <PID>           # Linux/Mac
taskkill /PID <PID> /F  # Windows

# Or use a different port
python server.py --port 5001
```

### Connection Refused

**Error:** `ConnectionRefusedError: [Errno 111] Connection refused`

**Causes & Solutions:**
1. Server not running → Start the server first
2. Wrong port → Verify port number matches server
3. Firewall blocking → Allow port through firewall
4. Wrong IP → Use `localhost` for local testing

### Permission Denied (Port < 1024)

**Error:** `PermissionError: [Errno 13] Permission denied`

**Solution:** Use ports > 1024 or run with sudo (not recommended):
```bash
# Better: Use high port
python server.py --port 5000

# Or sudo (use caution)
sudo python server.py --port 80
```

### Java Class Not Found

**Error:** `Error: Could not find or load main class`

**Solutions:**
```bash
# Ensure project is built
mvn clean package

# Check classpath includes dependencies
java -cp "target/classes:target/dependency/*" com.socketcomm...
```

### Memory Issues with Large Files

**Symptom:** `OutOfMemoryError` during file transfer

**Solutions:**
1. Increase JVM heap size:
```bash
java -Xmx2g -cp ... com.socketcomm.filetransfer.server.FileServer
```
2. Reduce buffer size in configuration
3. Split large files into smaller chunks

---

## Next Steps

Now that you have SocketComm running:

1. **Read the API Reference**: Learn about all available APIs
2. **Explore Examples**: Check the `examples/` directory
3. **Review Architecture**: Understand system design decisions
4. **Build Custom Features**: Extend the platform for your needs

### Additional Resources

- [API Reference Documentation](../api/API_Reference.md)
- [Architecture Guide](../architecture/System_Design.md)
- [Contributing Guidelines](../../CONTRIBUTING.md)
- [Project README](../../README.md)

---

## Getting Help

If you encounter issues not covered here:

1. Check existing GitHub Issues
2. Create a new issue with details:
   - OS and version
   - Python/Java version
   - Error messages (full stack trace)
   - Steps to reproduce
3. Join our community discussions

---

*Happy coding with SocketComm!*
