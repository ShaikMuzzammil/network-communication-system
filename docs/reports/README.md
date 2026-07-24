# 📚 Lab Reports Documentation

This directory contains comprehensive analysis reports and screenshots for Computer Networks laboratory experiments.

## 📁 Available Lab Reports

### Lab 2: Introduction to Socket Programming Using Python
- **Experiment Date:** 22/06/2026
- **Topic:** TCP Socket Programming with CustomTkinter GUI
- **Files:**
  - `lab-2/analysis/Lab2_Analysis_Report.pdf` - Comprehensive 10-page analysis document
  - `lab-2/screenshots/` - 16 screenshots from original lab document (pages 1-16)

**Key Topics Covered:**
- Python socket module (socket.socket, bind, listen, accept, connect, send, recv)
- Client-server architecture implementation
- CustomTkinter GUI development (Telegram-inspired interface)
- Hostname/IP resolution techniques
- Multi-threading extension for concurrent clients
- Connection state management

---

### Lab 3: File Transfer Protocol (FTP) using Java & Cisco Packet Tracer
- **Experiment Date:** 29/06/2026
- **Topic:** FTP Implementation & Network Simulation
- **Files:**
  - `lab-3/analysis/Lab3_Analysis_Report.pdf` - Comprehensive 12-page analysis document
  - `lab-3/screenshots/` - 25 screenshots from original lab document (pages 1-25)

**Key Topics Covered:**
- Java socket programming (ServerSocket, Socket, FileInputStream, FileOutputStream)
- Buffer-based file streaming (4096-byte chunks)
- Cisco Packet Tracer network topology design
- FTP server configuration (user authentication, permissions)
- IP addressing across LAN/WAN segments
- FTP command-line operations (put, get, dir, quit)
- Viva preparation questions

---

## 📂 Document Structure

```
docs/lab-reports/
├── lab-2/                    # Socket Programming Experiment
│   ├── analysis/
│   │   └── Lab2_Analysis_Report.pdf    # Main analysis document
│   └── screenshots/                  # Original lab pages (16 images)
│       ├── page_01.png               # Cover page
│       ├── page_02.png               # Server code (start)
│       ├── ... 
│       └── page_16.png               # Extension tasks
│
├── lab-3/                    # File Transfer Protocol Experiment
    ├── analysis/
    │   └── Lab3_Analysis_Report.pdf    # Main analysis document
    └── screenshots/                  # Original lab pages (25 images)
        ├── page_01.png               # Cover page
        ├── page_02.png               # Java server code
        ├── ... 
        └── page_25.png               # Viva questions
```

---

## 🔍 How to Use These Reports

1. **View Analysis PDFs:** Open the PDF files in any PDF reader for detailed technical analysis
2. **Reference Screenshots:** Screenshots are organized by page number matching original lab documents
3. **Code Integration:** These reports analyze the same code that exists in the main project:
   - Lab 2 code → `python-modules/gui-client/src/server.py` & `client.py`
   - Lab 3 code → `java-modules/file-transfer-service/src/main/java/com/socketcomm/filetransfer/`

---

## 📊 Quick Reference

| Report | Pages | Topics | Related Code |
|--------|-------|--------|-------------|
| Lab2 Analysis | ~10 | Socket Programming, GUI, Threading | `python-modules/` |
| Lab3 Analysis | ~12 | FTP, Java Sockets, Packet Tracer | `java-modules/` |
