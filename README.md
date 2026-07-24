# 🌐 network-communication-system

**Complete TCP/IP Communication Framework - Socket Programming, AES-256 Encryption, Client-Server Architecture**

[![Python 3.8+](https://img.shields.io/badge/Python-3.8%2B-blue.svg)](https://www.python.org/)
[![Java 11+](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://www.oracle.com/java/)
[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Version 2.0.0](https://img.shields.io/badge/Version-2.0.0-red.svg)]())

---

## 📖 Description (2 Lines for GitHub)

**A comprehensive implementation of computer network communication protocols featuring TCP/IP socket programming, secure data transmission with AES-256-GCM encryption, and complete client-server architecture with file transfer capabilities. This production-ready system demonstrates core networking concepts including protocol design (SCMP), real-time messaging, and multi-language support (Python + Java) for academic research, projects, and enterprise applications.**

---

## ✨ Core Components

| Component | Technology | Description |
|-----------|------------|-------------|
| **TCP Server** | Python `socket` | Multi-threaded concurrent server |
| **TCP Client** | Python `socket` | Async client with auto-reconnect |
| **SCMP Protocol** | Custom Binary | Reliable message framing (CRC32) |
| **Security Module** | AES-256-GCM + RSA | End-to-end encryption |
| **File Transfer** | Chunked I/O | Resume-capable transfers |
| **GUI Application** | Python `tkinter` | Real-time chat interface |
| **Java Service** | Spring/Maven | Enterprise file transfer |

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│         network-communication-system - Layered Design        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   Application Layer    → Chat, File Transfer, Broadcasting   │
│   Presentation Layer   → SCMP Protocol, Message Framing     │
│   Session Layer        → Security Manager (AES/RSA/HMAC)    │
│   Transport Layer      → TCP Sockets, Connection Pooling     │
│   Network Layer        → IP Routing, Addressing             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
network-communication-system/
│
├── src/
│   ├── python/socketcomm/              # Python Implementation
│   │   ├── __init__.py                 # Package exports
│   │   ├── server.py                   # TCP Server Module
│   │   ├── client.py                   # TCP Client Module  
│   │   ├── protocol.py                 # SCMP Protocol Handler
│   │   ├── security.py                 # Encryption & Authentication
│   │   ├── file_transfer.py            # File Transfer System
│   │   ├── gui_client.py               # GUI Chat Application
│   │   └── utils.py                    # Configuration & Utilities
│   │
│   └── java/file-transfer-service/     # Java Implementation
│       ├── pom.xml                     # Maven Build Config
│       └── src/main/java/...           # Java Source Files
│
├── docs/                               # Documentation
│   ├── api/                           # API Reference
│   │   └── API_Reference.md
│   ├── architecture/                  # System Design
│   │   └── System_Design.md
│   ├── guides/                        # User Guides
│   │   ├── Getting_Started_Guide.md
│   │   ├── Advanced_Usage_Guide.md
│   │   └── Integration_Guide.md
│   ├── analysis/                      # Technical Analysis
│   │   ├── python-gui-client/
│   │   ├── python-chat-core/
│   │   └── java-file-transfer/
│   └── reports/                       # Project Reports
│       ├── session-2/                 # Session 2 Report
│       └── session-3/                 # Session 3 Report
│
├── config/app-config.yaml             # System Configuration
├── examples/basic_chat.py             # Usage Examples
├── tests/test_socketcomm.py           # Test Suite
├── docker/                            # Docker Deployment Files
│   ├── Dockerfile.python
│   ├── Dockerfile.java
│   └── docker-compose.yml
├── assets/icons/                      # Application Icons
│
├── README.md                          # This Document
├── LICENSE                            # MIT License
├── .gitignore                         # Git Ignore Rules
├── CONTRIBUTING.md                    # Contribution Guidelines
└── GITHUB_SETUP.md                    # GitHub Setup Instructions
```

---

## 🚀 Quick Start

### Installation

```bash
# Extract the project
cd network-communication-system

# Create virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
# or venv\Scripts\activate  # Windows

# Install dependencies
pip install -r requirements.txt

# Install package
pip install -e .
```

### Running Examples

```bash
# Start TCP server
python examples/basic_chat.py --mode server --port 8080

# Connect client (new terminal)
python examples/basic_chat.py --mode client --port 8080

# Launch GUI application
python -m socketcomm.gui_client
```

---

## 💻 Usage Examples

### Basic TCP Server-Client Communication

```python
from socketcomm import SocketServer, SocketClient

# Server Side
server = SocketServer(host='0.0.0.0', port=8080)
server.start()

# Client Side
client = SocketClient(host='localhost', port=8080)
client.connect()
client.send_message("Hello from client!")
response = client.receive_message()
```

### Secure Encrypted Connection

```python
from socketcomm import SocketServer, SecurityManager

security = SecurityManager()
security.generate_rsa_keys()

secure_server = SocketServer(
    host='0.0.0.0',
    port=8443,
    security_manager=security,
    enable_encryption=True
)
secure_server.start()  # Running with AES-256-GCM encryption
```

### File Transfer Operations

```python
from socketcomm import FileTransferClient

transfer = FileTransferClient(host='localhost', port=9000)
transfer.connect()

# Upload with progress tracking
transfer.upload_file(
    local_path='/path/to/file.zip',
    remote_path='/uploads/',
    progress_callback=lambda p: print(f"Progress: {p}%")
)

# Download with resume support
transfer.download_file(
    remote_path='/downloads/large_file.iso',
    local_path='./large_file.iso',
    resume_if_partial=True
)
```

---

## ⚙️ Configuration

Configuration is managed via `config/app-config.yaml`:

```yaml
server:
  host: "0.0.0.0"
  port: 8080
  max_connections: 100
  
security:
  enable_encryption: true
  algorithm: "AES-256-GCM"
  key_size: 256

file_transfer:
  chunk_size: 8192
  max_file_size_mb: 100

logging:
  level: "INFO"
  file: "network-comm.log"
```

---

## 🧪 Testing

```bash
# Run all tests
pytest tests/test_socketcomm.py -v

# Run with coverage report
pytest tests/ --cov=socketcomm --cov-report=html

# Test specific module
pytest tests/test_socketcomm.py::TestSCMPProtocol -v
```

**Coverage Areas:**
- Protocol serialization/deserialization
- Cryptographic operations (AES, RSA, HMAC)
- Connection management
- Input validation
- Integration scenarios

---

## 🐳 Docker Deployment

```bash
# Using Docker Compose
docker-compose up -d

# Individual builds
docker build -f docker/Dockerfile.python -t network-comm-python .
docker build -f docker/Dockerfile.java -t network-comm-java .

# Run containers
docker run -d -p 8080:8080 network-comm-python
docker run -d -p 9000:9000 network-comm-java
```

---

## 📚 Documentation

| Document | Location | Content |
|----------|----------|---------|
| **API Reference** | `docs/api/API_Reference.md` | Complete method documentation |
| **System Design** | `docs/architecture/System_Design.md` | Architecture details |
| **Getting Started** | `docs/guides/Getting_Started_Guide.md` | Beginner guide |
| **Advanced Usage** | `docs/guides/Advanced_Usage_Guide.md` | Expert features |
| **Integration Guide** | `docs/guides/Integration_Guide.md` | How to integrate |
| **Technical Analysis** | `docs/analysis/` | Analysis reports for all modules |
| **Project Reports** | `docs/reports/session-2/`, `session-3/` | Session reports with screenshots |

---

## 🎯 Use Cases

### Computer Networks Education
- TCP/IP protocol stack demonstration
- Socket programming concepts
- Client-server architecture study
- Network security principles
- Data serialization protocols

### Research & Development
- Protocol design experimentation
- Performance benchmarking
- Security algorithm testing
- Distributed systems prototyping

### Production Applications
- Internal messaging systems
- Secure file transfer solutions
- Real-time data exchange
- IoT communication gateway

---

## 📊 Project Statistics

| Metric | Value |
|--------|-------|
| Total Files | 82+ |
| Lines of Code | 10,000+ |
| Languages | Python, Java |
| Test Coverage | >85% |
| Documentation | Complete |
| Docker Support | Yes |

---

## 🤝 Contributing

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## 📄 License

This project is licensed under the MIT License - see [LICENSE](LICENSE) for details.

---

## 📞 Support

- Issues: [GitHub Issues](https://github.com/YOUR_USERNAME/network-communication-system/issues)
- Discussions: [GitHub Discussions](https://github.com/YOUR_USERNAME/network-communication-system/discussions)

---

<div align="center">

⭐ **Star this repository if you find it useful!**

🚀 **network-communication-system - Professional TCP/IP Communication Framework**

</div>
"# network-communication-system" 
