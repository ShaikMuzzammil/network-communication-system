# SocketComm Integration Guide

## Integrating SocketComm into Your Applications

This guide provides comprehensive instructions for integrating the SocketComm platform into existing applications or building new solutions on top of its components.

---

## Table of Contents

1. [Quick Integration Examples](#quick-integration-examples)
2. [Python Module Integration](#python-module-integration)
3. [Java Module Integration](#java-module-integration)
4. [Web Application Integration](#web-application-integration)
5. [Database & Persistence Integration](#database--persistence-integration)
6. [Authentication Integration](#authentication-integration)
7. [Monitoring & Observability Integration](#monitoring--observability-integration)
8. [Best Practices](#best-practices)

---

## Quick Integration Examples

### Basic Chat Server (5 lines of code)

```python
from chat_core.server import ChatServer

# Create and start server
server = ChatServer(host='0.0.0.0', port=5000, max_clients=100)
server.start()
```

### Basic Chat Client with Callbacks

```python
from chat_core.client import ChatClient

def on_message(msg):
    print(f"Received: {msg}")

client = ChatClient('localhost', 5000)
client.set_callback('message', on_message)
client.connect()
client.send_message("Hello, World!")
```

### File Upload (Java)

```java
FileClient client = new FileClient.Builder()
    .serverHost("localhost")
    .serverPort(8080)
    .build();

TransferResult result = client.uploadFile(new File("document.pdf"));
System.out.println("Success: " + result.isSuccess());
```

---

## Python Module Integration

### Installation

```bash
# Add to your project's requirements.txt
# For chat core:
git+https://github.com/YOUR_USERNAME/SocketComm.git#subdirectory=python-modules/chat-core

# Or install directly:
cd python-modules/chat-core
pip install -e .
```

### Importing in Your Application

```python
# Option 1: Direct import from source
import sys
sys.path.append('/path/to/SocketComm/python-modules/chat-core/src')
from chat_core.server import ChatServer
from chat_core.client import ChatClient

# Option 2: After pip installation
from chat_core import ChatServer, ChatClient
```

### Custom Message Handler Example

```python
import json
from datetime import datetime

class CommandHandler:
    """Custom command processor for chat messages."""
    
    def __init__(self, server):
        self.server = server
        self.commands = {
            '/help': self.cmd_help,
            '/users': self.cmd_users,
            '/time': self.cmd_time,
            '/pm': self.cmd_private_message,
        }
    
    def process(self, client_socket, message: str) -> str | None:
        """Process message. Returns response or None if not a command."""
        if not message.startswith('/'):
            return None  # Not a command
        
        parts = message.split()
        cmd = parts[0].lower()
        args = parts[1:]
        
        handler = self.commands.get(cmd)
        if handler:
            return handler(client_socket, args)
        else:
            return "Unknown command. Type /help for available commands."
    
    def cmd_help(self, client, args):
        """Show available commands."""
        help_text = """
Available commands:
/help    - Show this help message
/users   - List connected users
/time    - Show current server time
/pm <user> <msg> - Send private message
        """
        return help_text
    
    def cmd_users(self, client, args):
        """List connected users."""
        users = self.server.get_connected_clients()
        user_list = "\n".join([f"  - {u['address']}" for u in users])
        return f"Connected users ({len(users)}):\n{user_list}"
    
    def cmd_time(self, client, args):
        """Show server time."""
        return f"Server time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"
    
    def cmd_private_message(self, client, args):
        """Send private message."""
        if len(args) < 2:
            return "Usage: /pm <username> <message>"
        
        target = args[0]
        msg = ' '.join(args[1:])
        
        # Find target client and send
        # Implementation depends on your user tracking
        return f"[PM to {target}]: {msg}"


# Integrate with ChatServer
class ExtendedChatServer(ChatServer):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.command_handler = CommandHandler(self)
    
    def handle_message(self, client_socket, message: str):
        # Check for commands first
        response = self.command_handler.process(client_socket, message)
        if response:
            self.send_to_client(client_socket, response)
        else:
            # Normal message handling
            super().handle_message(client_socket, message)


# Usage
server = ExtendedChatServer(port=5000)
server.start()
```

---

## Java Module Integration

### Maven Dependency

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.socketcomm</groupId>
    <artifactId>file-transfer-service</artifactId>
    <version>1.0.0</version>
</dependency>
```

Or install locally:

```bash
cd java-modules/file-transfer-service
mvn install:install-file \
    -Dfile=target/file-transfer-service-1.0.0.jar \
    -DgroupId=com.socketcomm \
    -DartifactId=file-transfer-service \
    -Dversion=1.0.0 \
    -Dpackaging=jar
```

### Spring Boot Integration Example

```java
import com.socketcomm.filetransfer.server.FileServer;
import com.socketcomm.filetransfer.client.FileClient;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class SocketCommIntegrationService {
    
    private FileServer fileServer;
    private FileClient fileClient;
    
    @PostConstruct
    public void init() {
        // Initialize file server
        this.fileServer = new FileServer.Builder()
            .port(8080)
            .saveDirectory("/app/uploads")
            .maxConcurrentTransfers(10)
            .bufferSize(16384)  // 16KB buffers
            .build();
        
        // Start in background thread
        new Thread(() -> {
            try {
                fileServer.start();
            } catch (IOException e) {
                throw new RuntimeException("Failed to start file server", e);
            }
        }).start();
        
        // Initialize client for outgoing transfers
        this.fileClient = new FileClient.Builder()
            .serverHost("remote-server.example.com")
            .serverPort(8080)
            .retryCount(5)
            .build();
    }
    
    public TransferResult uploadFile(java.io.File file) {
        return fileClient.uploadFile(file);
    }
    
    public ServerStats getTransferStatistics() {
        return fileServer.getStatistics();
    }
    
    @PreDestroy
    public void cleanup() {
        if (fileServer != null) {
            fileServer.stop();
        }
    }
}
```

### REST API Wrapper

```java
@RestController
@RequestMapping("/api/files")
public class FileTransferController {
    
    @Autowired
    private SocketCommIntegrationService socketCommService;
    
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Save uploaded file temporarily
            java.io.File tempFile = java.io.File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);
            
            // Transfer via SocketComm
            TransferResult result = socketCommService.uploadFile(tempFile);
            
            // Cleanup
            tempFile.delete();
            
            UploadResponse response = UploadResponse.builder()
                .success(result.isSuccess())
                .bytesTransferred(result.getBytesTransferred())
                .durationMs(result.getDurationMs())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ServerStats> getStats() {
        return ResponseEntity.ok(socketCommService.getTransferStatistics());
    }
}
```

---

## Web Application Integration

### Flask Integration

```python
from flask import Flask, render_template, jsonify, request
from flask_socketio import SocketIO, emit, join_room, leave_room
from threading import Thread
from chat_core.server import ChatServer
from chat_core.client import ChatClient
import json

app = Flask(__name__)
app.config['SECRET_KEY'] = 'your-secret-key'
socketio = SocketIO(app, cors_allowed_origins="*")

# Background SocketComm server
chat_server = None

def run_chat_server():
    global chat_server
    chat_server = ChatServer(host='localhost', port=5001)
    chat_server.start()

# Start background server on app startup
Thread(target=run_chat_server, daemon=True).start()

@socketio.on('connect')
def handle_connect():
    print(f"WebSocket client connected: {request.sid}")
    emit('message', {'text': 'Connected to SocketComm Gateway'})

@socketio.on('send_message')
def handle_message(data):
    """Forward WebSocket message to SocketComm."""
    message = data.get('message', '')
    username = data.get('username', 'Anonymous')
    
    # Format and broadcast via SocketComm
    formatted = f"{username}: {message}"
    
    # If SocketComm server is running, broadcast there too
    if chat_server:
        chat_server.broadcast(formatted)
    
    # Broadcast to all WebSocket clients
    emit('message', {'text': formatted, 'sender': username}, broadcast=True)

@socketio.on('join')
def handle_join(data):
    room = data.get('room', 'default')
    join_room(room)
    emit('message', {'text': f'Joined room: {room}'}, room=request.sid)

@app.route('/api/clients')
def get_clients():
    """Get connected clients from SocketComm."""
    if chat_server:
        return jsonify(chat_server.get_connected_clients())
    return jsonify([])

if __name__ == '__main__':
    socketio.run(app, host='0.0.0.0', port=3000, debug=True)
```

### React Frontend Integration

```jsx
// SocketCommContext.js - React context for SocketComm integration
import React, { createContext, useContext, useEffect, useState } from 'react';
import io from 'socket.io-client';

const SocketCommContext = createContext();

export const useSocketComm = () => useContext(SocketCommContext);

export const SocketCommProvider = ({ children }) => {
    const [socket, setSocket] = useState(null);
    const [messages, setMessages] = useState([]);
    const [connected, setConnected] = useState(false);
    const [users, setUsers] = useState([]);

    useEffect(() => {
        // Connect to Flask gateway
        const newSocket = io('http://localhost:3000');
        
        newSocket.on('connect', () => {
            setConnected(true);
            console.log('Connected to SocketComm gateway');
        });

        newSocket.on('disconnect', () => {
            setConnected(false);
        });

        newSocket.on('message', (data) => {
            setMessages(prev => [...prev, {
                id: Date.now(),
                text: data.text,
                sender: data.sender || 'System',
                timestamp: new Date().toISOString(),
            }]);
        });

        setSocket(newSocket);

        return () => newSocket.close();
    }, []);

    const sendMessage = (text, username = 'User') => {
        if (socket) {
            socket.emit('send_message', { message: text, username });
        }
    };

    const fetchUsers = async () => {
        try {
            const response = await fetch('http://localhost:3000/api/clients');
            const data = await response.json();
            setUsers(data);
        } catch (error) {
            console.error('Failed to fetch users:', error);
        }
    };

    return (
        <SocketCommContext.Provider value={{
            socket,
            messages,
            connected,
            users,
            sendMessage,
            fetchUsers,
        }}>
            {children}
        </SocketCommContext.Provider>
    );
};

// Usage in component
const ChatComponent = () => {
    const { messages, connected, sendMessage } = useSocketComm();
    const [input, setInput] = useState('');

    const handleSend = () => {
        if (input.trim()) {
            sendMessage(input);
            setInput('');
        }
    };

    return (
        <div className="chat-container">
            <div className={`status ${connected ? 'online' : 'offline'}`}>
                {connected ? 'Connected' : 'Disconnected'}
            </div>
            <div className="messages">
                {messages.map(msg => (
                    <div key={msg.id} className="message">
                        <span className="sender">{msg.sender}: </span>
                        <span className="text">{msg.text}</span>
                    </div>
                ))}
            </div>
            <div className="input-area">
                <input
                    type="text"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && handleSend()}
                    placeholder="Type a message..."
                />
                <button onClick={handleSend}>Send</button>
            </div>
        </div>
    );
};
```

---

## Database & Persistence Integration

### SQLite Message History

```python
import sqlite3
import json
from datetime import datetime
from contextlib import contextmanager

class MessagePersistence:
    """Persist chat messages to SQLite database."""
    
    def __init__(self, db_path='chat_history.db'):
        self.db_path = db_path
        self._init_db()
    
    @contextmanager
    def get_connection(self):
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        try:
            yield conn
        finally:
            conn.close()
    
    def _init_db(self):
        with self.get_connection() as conn:
            conn.executescript('''
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sender TEXT NOT NULL,
                    content TEXT NOT NULL,
                    room_id TEXT DEFAULT 'general',
                    timestamp REAL NOT NULL,
                    message_type TEXT DEFAULT 'text',
                    metadata TEXT
                );
                
                CREATE INDEX IF NOT EXISTS idx_messages_timestamp 
                ON messages(timestamp);
                
                CREATE INDEX IF NOT EXISTS idx_messages_sender 
                ON messages(sender);
                
                CREATE TABLE IF NOT EXISTS rooms (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL,
                    created_at REAL NOT NULL
                );
            ''')
    
    def save_message(self, sender: str, content: str, 
                     room: str = 'general', 
                     msg_type: str = 'text',
                     metadata: dict = None) -> int:
        """Save a message and return its ID."""
        with self.get_connection() as conn:
            cursor = conn.execute('''
                INSERT INTO messages (sender, content, room_id, timestamp, message_type, metadata)
                VALUES (?, ?, ?, ?, ?, ?)
            ''', (sender, content, room, datetime.now().timestamp(), 
                  msg_type, json.dumps(metadata) if metadata else None))
            conn.commit()
            return cursor.lastrowid
    
    def get_recent_messages(self, limit: int = 50, 
                           before: float = None,
                           room: str = None) -> list:
        """Retrieve recent messages."""
        with self.get_connection() as conn:
            query = '''
                SELECT * FROM messages 
                WHERE 1=1
            '''
            params = []
            
            if before:
                query += ' AND timestamp < ?'
                params.append(before)
            
            if room:
                query += ' AND room_id = ?'
                params.append(room)
            
            query += ' ORDER BY timestamp DESC LIMIT ?'
            params.append(limit)
            
            rows = conn.execute(query, params).fetchall()
            return [dict(row) for row in rows]
    
    def search_messages(self, query: str, limit: int = 20) -> list:
        """Search messages by content."""
        with self.get_connection() as conn:
            rows = conn.execute('''
                SELECT * FROM messages 
                WHERE content LIKE ?
                ORDER BY timestamp DESC 
                LIMIT ?
            ''', (f'%{query}%', limit)).fetchall()
            return [dict(row) for row in rows]


# Integrate with ChatServer
class PersistentChatServer(ChatServer):
    """ChatServer with message persistence."""
    
    def __init__(self, db_path: str = 'chat_history.db', **kwargs):
        super().__init__(**kwargs)
        self.persistence = MessagePersistence(db_path)
    
    def handle_message(self, client_socket, message: str):
        # Get sender info
        sender = self.get_client_info(client_socket, 'address')
        
        # Persist message
        self.persistence.save_message(
            sender=str(sender),
            content=message,
            metadata={'client_id': id(client_socket)}
        )
        
        # Continue normal processing
        super().handle_message(client_socket, message)
    
    def get_history(self, count: int = 50) -> list:
        """Return message history for loading."""
        return self.persistence.get_recent_messages(count)
```

### Redis Integration for Clustering

```python
import redis
import json
import pickle
from typing import Optional

class RedisClusterIntegration:
    """Redis-backed state sharing for multi-instance deployments."""
    
    def __init__(self, redis_url: str = 'redis://localhost:6379'):
        self.redis_client = redis.from_url(redis_url)
        self.channel = 'socketcomm:broadcast'
        self.pubsub = None
    
    def publish_message(self, message: dict, exclude_instance: str = None):
        """Publish message to all instances."""
        data = {
            'type': 'broadcast',
            'message': message,
            'source': exclude_instance,  # Don't echo back
            'timestamp': time.time()
        }
        self.redis_client.publish(self.channel, pickle.dumps(data))
    
    def subscribe(self, callback):
        """Subscribe to cross-instance messages."""
        if not self.pubsub:
            self.pubsub = self.redis_client.pubsub()
            self.pubsub.subscribe(self.channel)
        
        def listener():
            for message in self.pubsub.listen():
                if message['type'] == 'message':
                    data = pickle.loads(message['data'])
                    callback(data)
        
        import threading
        thread = threading.Thread(target=listener, daemon=True)
        thread.start()
    
    def store_session(self, session_id: str, data: dict, ttl: int = 3600):
        """Store session data with TTL."""
        key = f"session:{session_id}"
        self.redis_client.setex(key, ttl, json.dumps(data))
    
    def get_session(self, session_id: str) -> Optional[dict]:
        """Retrieve session data."""
        key = f"session:{session_id}"
        data = self.redis_client.get(key)
        return json.loads(data) if data else None
    
    def increment_metric(self, metric_name: str, amount: int = 1):
        """Increment a counter metric."""
        key = f"metric:{metric_name}"
        self.redis_client.incrby(key, amount)
    
    def get_metrics(self) -> dict:
        """Get all metrics."""
        keys = self.redis_client.keys('metric:*')
        metrics = {}
        for key in keys:
            name = key.decode().replace('metric:', '')
            metrics[name] = int(self.redis_client.get(key))
        return metrics
```

---

## Authentication Integration

### JWT Token Authentication

```python
import jwt
import hashlib
import secrets
from datetime import datetime, timedelta
from functools import wraps

class AuthManager:
    """JWT-based authentication manager for SocketComm."""
    
    def __init__(self, secret_key: str, token_expiry: int = 3600):
        self.secret_key = secret_key
        self.token_expiry = token_expiry
        self.active_tokens = {}  # token -> user_info
    
    def generate_token(self, user_id: str, **claims) -> str:
        """Generate JWT token for authenticated user."""
        payload = {
            'user_id': user_id,
            'iat': datetime.utcnow(),
            'exp': datetime.utcnow() + timedelta(seconds=self.token_expiry),
            **claims
        }
        token = jwt.encode(payload, self.secret_key, algorithm='HS256')
        self.active_tokens[token] = {'user_id': user_id, **claims}
        return token
    
    def verify_token(self, token: str) -> dict:
        """Verify and decode JWT token."""
        try:
            payload = jwt.decode(token, self.secret_key, algorithms=['HS256'])
            if token in self.active_tokens:
                return payload
            return None
        except jwt.ExpiredSignatureError:
            # Clean up expired tokens
            self.active_tokens.pop(token, None)
            return None
        except jwt.InvalidTokenError:
            return None
    
    def invalidate_token(self, token: str):
        """Invalidate a token (logout)."""
        self.active_tokens.pop(token, None)
    
    def require_auth(self, func):
        """Decorator to require authentication for handlers."""
        @wraps(func)
        def wrapper(client_socket, message, *args, **kwargs):
            # Expect format: "TOKEN:message"
            if ':' not in message:
                self.send_error(client_socket, "Authentication required")
                return
            
            token, actual_message = message.split(':', 1)
            user = self.verify_token(token)
            
            if not user:
                self.send_error(client_socket, "Invalid or expired token")
                return
            
            # Add user info to kwargs
            kwargs['authenticated_user'] = user
            return func(client_socket, actual_message, *args, **kwargs)
        return wrapper


# Usage example
auth = AuthManager(secret_key=secrets.token_hex(32))

class SecureChatServer(ChatServer):
    """ChatServer with JWT authentication."""
    
    def __init__(self, auth_manager: AuthManager, **kwargs):
        super().__init__(**kwargs)
        self.auth = auth_manager
    
    def handle_auth_request(self, client_socket, credentials: str):
        """Handle login request."""
        try:
            creds = json.loads(credentials)
            username = creds.get('username')
            password = creds.get('password')
            
            # Validate credentials (check against DB, LDAP, etc.)
            if self.validate_credentials(username, password):
                token = self.auth.generate_token(
                    user_id=username,
                    role='user',
                    address=self.get_client_address(client_socket)
                )
                response = {'status': 'ok', 'token': token}
            else:
                response = {'status': 'error', 'message': 'Invalid credentials'}
            
            self.send_to_client(client_socket, json.dumps(response))
            
        except Exception as e:
            error_resp = {'status': 'error', 'message': str(e)}
            self.send_to_client(client_socket, json.dumps(error_resp))
    
    def validate_credentials(self, username: str, password: str) -> bool:
        """Validate user credentials. Implement your logic here."""
        # Placeholder - integrate with your auth system
        return True  # Always accept for demo
```

---

## Monitoring & Observability Integration

### Prometheus Metrics Exporter

```python
from prometheus_client import Counter, Histogram, Gauge, start_http_server
import time

class MetricsExporter:
    """Prometheus metrics exporter for SocketComm."""
    
    def __init__(self, port: int = 9090):
        # Define metrics
        self.messages_total = Counter(
            'socketcomm_messages_total',
            'Total messages processed',
            ['direction', 'type']
        )
        
        self.message_latency = Histogram(
            'socketcomm_message_latency_seconds',
            'Message processing latency',
            buckets=[0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0]
        )
        
        self.connections_active = Gauge(
            'socketcomm_connections_active',
            'Number of active connections'
        )
        
        self.connections_total = Counter(
            'socketcomm_connections_total',
            'Total connections',
            ['status']  # success, failed, rejected
        )
        
        self.bytes_transferred = Counter(
            'socketcomm_bytes_transferred_total',
            'Total bytes transferred',
            ['direction']  # sent, received
        )
        
        # Start metrics HTTP server
        start_http_server(port)
    
    def track_message(self, direction: str, msg_type: str, duration: float):
        """Record message processing."""
        self.messages_total.labels(direction=direction, type=msg_type).inc()
        self.message_latency.observe(duration)
    
    def track_connection(self, status: str):
        """Record connection event."""
        self.connections_total.labels(status=status).inc()
        if status == 'success':
            self.connections_active.inc()
        elif status in ('failed', 'closed'):
            self.connections_active.dec()
    
    def track_bytes(self, direction: str, count: int):
        """Record bytes transferred."""
        self.bytes_transferred.labels(direction=direction).inc(count)


# Integrate with ChatServer
class ObservableChatServer(ChatServer):
    """ChatServer with Prometheus metrics."""
    
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.metrics = MetricsExporter(port=9090)
    
    def handle_message(self, client_socket, message: str):
        start_time = time.time()
        
        try:
            super().handle_message(client_socket, message)
            duration = time.time() - start_time
            self.metrics.track_message('received', 'text', duration)
        except Exception as e:
            duration = time.time() - start_time
            self.metrics.track_message('received', 'error', duration)
            raise
    
    def on_client_connect(self, client_socket):
        super().on_client_connect(client_socket)
        self.metrics.track_connection('success')
    
    def on_client_disconnect(self, client_socket):
        super().on_client_disconnect(client_socket)
        self.metrics.track_connection('closed')
```

---

## Best Practices

### Connection Management

| Practice | Recommendation | Rationale |
|----------|---------------|-----------|
| Connection Pooling | Reuse connections | Reduces TCP handshake overhead |
| Heartbeat/Ping | Every 30 seconds | Detect dead connections early |
| Graceful Shutdown | Signal handlers | Allow in-flight ops to complete |
| Backoff Strategy | Exponential with jitter | Prevent thundering herd on reconnect |

### Error Handling

```python
# Robust error handling pattern
def safe_operation(operation_name: str):
    """Decorator for safe operation execution."""
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            try:
                return func(*args, **kwargs)
            except ConnectionResetError:
                log.warning(f"{operation_name}: Connection reset by peer")
                raise  # Let reconnection logic handle it
            except TimeoutError:
                log.error(f"{operation_name}: Operation timed out")
                raise
            except MemoryError:
                log.critical(f"{operation_name}: Out of memory!")
                raise  # Should not be caught, let it crash
            except Exception as e:
                log.error(f"{operation_name}: Unexpected error: {e}")
                raise
        return wrapper
    return decorator
```

### Performance Tips

1. **Buffer Size Tuning**: Use 8KB-16KB buffers for most workloads
2. **Async I/O**: Use selectors/asyncio for >1000 connections
3. **Message Batching**: Group small messages when possible
4. **Zero-Copy**: Use memoryview/slices for large payloads
5. **Connection Reuse**: Keep-alive for frequent communicators

### Security Checklist

- [ ] Validate all inputs at API boundaries
- [ ] Sanitize error messages (no stack traces to clients)
- [ ] Implement rate limiting per IP/client
- [ ] Use TLS for production deployments
- [ ] Rotate secrets regularly
- [ ] Log security events appropriately
- [ ] Keep dependencies updated

---

*For additional support, see the main README.md or open an issue on GitHub.*
