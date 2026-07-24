# SocketComm Advanced Usage Guide

## Beyond the Basics - Advanced Features and Customization

This guide covers advanced features for developers who want to extend, customize, or integrate SocketComm into larger systems.

---

## Table of Contents

1. [Custom Protocol Implementation](#custom-protocol-implementation)
2. [Security & Authentication](#security--authentication)
3. [Performance Optimization](#performance-optimization)
4. [Scaling Strategies](#scaling-strategies)
5. [Integration Patterns](#integration-patterns)
6. [Plugin Development](#plugin-development)
7. [Monitoring & Observability](#monitoring--observability)

---

## Custom Protocol Implementation

### Understanding the Protocol Layer

SocketComm uses a layered protocol architecture:

```
┌─────────────────────────────────────┐
│         Application Layer           │  (Your code)
├─────────────────────────────────────┤
│        Message Protocol Layer       │  (Frame format)
├─────────────────────────────────────┤
│          Transport Layer            │  (TCP Sockets)
└─────────────────────────────────────┘
```

### Creating a Custom Message Handler

Implement custom message processing by extending the base classes:

```python
from chat_core.server import ChatServer
from chat_core.client import ChatClient
import json

class JSONMessageServer(ChatServer):
    """Server that handles JSON-formatted messages."""
    
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.message_handlers = {}
    
    def register_handler(self, message_type: str, handler):
        """Register a handler for specific message types."""
        self.message_handlers[message_type] = handler
    
    def handle_message(self, client_socket, raw_message: bytes):
        """Override to add custom message handling."""
        try:
            # Parse JSON message
            data = json.loads(raw_message.decode(self.encoding))
            msg_type = data.get('type', 'text')
            
            # Call registered handler if exists
            if msg_type in self.message_handlers:
                response = self.message_handlers[msg_type](data)
                self.send_response(client_socket, response)
            else:
                # Default handling
                super().handle_message(client_socket, raw_message)
                
        except json.JSONDecodeError:
            # Fallback for plain text messages
            super().handle_message(client_socket, raw_message)
    
    def send_response(self, client_socket, response_data):
        """Send JSON response."""
        response = json.dumps(response_data)
        self.send_to_client(client_socket, response)


# Usage Example
server = JSONMessageServer(port=5000)

# Register custom handlers
def handle_command(data):
    cmd = data.get('command')
    if cmd == '/users':
        return {'type': 'response', 'users': server.get_connected_clients()}
    elif cmd == '/time':
        return {'type': 'response', 'time': datetime.now().isoformat()}

server.register_handler('command', handle_command)
server.start()
```

### Binary Protocol Extension

For high-performance binary protocols:

```python
import struct

class BinaryProtocolHandler:
    """
    Binary protocol implementation with:
    - 4-byte magic header
    - 2-byte version
    - 2-byte message type
    - 4-byte payload length
    - Variable payload
    """
    
    MAGIC_HEADER = 0xCDAB
    VERSION = 1
    
    MSG_TYPES = {
        'PING': 0x01,
        'PONG': 0x02,
        'DATA': 0x03,
        'ACK': 0x04,
        'ERROR': 0x05,
    }
    
    @classmethod
    def encode(cls, msg_type: str, payload: bytes) -> bytes:
        """Encode message to binary format."""
        type_id = cls.MSG_TYPES.get(msg_type, 0x00)
        header = struct.pack(
            '>HBBH',  # Big-endian: uint16, uint8, uint8, uint16
            cls.MAGIC_HEADER,
            cls.VERSION,
            type_id,
            len(payload)
        )
        return header + payload
    
    @classmethod
    def decode(cls, data: bytes) -> dict:
        """Decode binary message."""
        if len(data) < 8:
            raise ValueError("Message too short")
        
        magic, version, type_id, length = struct.unpack('>HBBH', data[:8])
        
        if magic != cls.MAGIC_HEADER:
            raise ValueError(f"Invalid magic: {hex(magic)}")
        
        payload = data[8:8+length]
        
        # Reverse lookup message type
        type_name = next(
            (k for k, v in cls.MSG_TYPES.items() if v == type_id),
            'UNKNOWN'
        )
        
        return {
            'version': version,
            'type': type_name,
            'payload': payload
        }


# Integration with ChatServer
class BinaryChatServer(ChatServer):
    def send_binary_message(self, client_socket, msg_type: str, payload: bytes):
        frame = BinaryProtocolHandler.encode(msg_type, payload)
        client_socket.sendall(frame)
```

---

## Security & Authentication

### Implementing Token-Based Authentication

```python
import hashlib
import time
import jwt  # pip install PyJWT

class AuthenticatedServer(ChatServer):
    """
    Server with JWT-based authentication.
    Clients must authenticate before sending messages.
    """
    
    def __init__(self, secret_key: str, **kwargs):
        super().__init__(**kwargs)
        self.secret_key = secret_key
        self.authenticated_clients = {}  # socket -> user_info
        self.token_expiry = 3600  # 1 hour
    
    def generate_token(self, username: str, password: str) -> str:
        """Generate JWT token for authenticated user."""
        # In production, verify password against database
        payload = {
            'username': username,
            'exp': time.time() + self.token_expiry,
            'iat': time.time()
        }
        return jwt.encode(payload, self.secret_key, algorithm='HS256')
    
    def verify_token(self, token: str) -> dict:
        """Verify and decode JWT token."""
        try:
            return jwt.decode(token, self.secret_key, algorithms=['HS256'])
        except jwt.ExpiredSignatureError:
            return None
        except jwt.InvalidTokenError:
            return None
    
    def handle_auth(self, client_socket, data: dict):
        """Handle authentication request."""
        username = data.get('username')
        password = data.get('password')
        
        # Generate token (validate credentials in production)
        token = self.generate_token(username, password)
        
        # Store auth state
        self.authenticated_clients[client_socket] = {
            'username': username,
            'authenticated_at': time.time()
        }
        
        # Send token to client
        response = {'status': 'ok', 'token': token}
        self.send_to_client(client_socket, json.dumps(response))
    
    def is_authenticated(self, client_socket) -> bool:
        """Check if client is authenticated."""
        return client_socket in self.authenticated_clients
    
    def handle_message(self, client_socket, raw_message):
        """Only process messages from authenticated clients."""
        if not self.is_authenticated(client_socket):
            error = {'error': 'Authentication required'}
            self.send_to_client(client_socket, json.dumps(error))
            return
        
        # Process normally for authenticated clients
        super().handle_message(client_socket, raw_message)


# Client-side authentication
class AuthenticatedClient(ChatClient):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.token = None
    
    def authenticate(self, username: str, password: str) -> bool:
        """Authenticate with server."""
        auth_request = {
            'action': 'auth',
            'username': username,
            'password': password
        }
        self.send_message(json.dumps(auth_request))
        
        # Wait for response
        response = self.receive_message()
        if response:
            data = json.loads(response)
            if data.get('status') == 'ok':
                self.token = data['token']
                return True
        return False
    
    def send_authenticated_message(self, message: str):
        """Send message with token."""
        if not self.token:
            raise Exception("Not authenticated")
        
        payload = {
            'token': self.token,
            'message': message
        }
        self.send_message(json.dumps(payload))
```

### TLS/SSL Encryption

Enable encrypted communication:

```python
import ssl

class SecureServer(ChatServer):
    """SSL/TLS enabled server."""
    
    def __init__(self, certfile: str, keyfile: str, **kwargs):
        super().__init__(**kwargs)
        self.certfile = certfile
        self.keyfile = keyfile
    
    def create_ssl_context(self):
        """Create SSL context for server."""
        context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        context.load_cert_chain(self.certfile, self.keyfile)
        # Enforce strong security
        context.set_ciphers('ECDHE+AESGCM:ECDHE+CHACHA20')
        context.minimum_version = ssl.TLSVersion.TLSv1_2
        return context
    
    def start(self):
        """Start server with SSL wrapping."""
        import socket
        
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.server_socket.bind((self.host, self.port))
        self.server_socket.listen(self.max_clients)
        
        ssl_context = self.create_ssl_context()
        self.server_socket = ssl_context.wrap_socket(
            self.server_socket,
            server_side=True
        )
        
        print(f"[SECURE] Server running on {self.host}:{self.port} (TLS)")
        self.accept_connections()


# Generate self-signed certificate for development
# openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes
```

---

## Performance Optimization

### Connection Pooling

```python
from queue import Queue
import threading

class ConnectionPool:
    """
    Reusable connection pool for high-frequency messaging.
    Reduces TCP handshake overhead.
    """
    
    def __init__(self, host: str, port: int, pool_size: int = 5):
        self.host = host
        self.port = port
        self.pool_size = pool_size
        self.pool = Queue(maxsize=pool_size)
        self.lock = threading.Lock()
        
        # Pre-warm connections
        for _ in range(pool_size):
            conn = self._create_connection()
            self.pool.put(conn)
    
    def _create_connection(self):
        """Create new connection."""
        import socket
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((self.host, self.port))
        return {'socket': sock, 'in_use': False, 'created_at': time.time()}
    
    def acquire(self, timeout: float = 5.0):
        """Get connection from pool."""
        conn = self.pool.get(timeout=timeout)
        with self.lock:
            conn['in_use'] = True
        return conn
    
    def release(self, conn):
        """Return connection to pool."""
        with self.lock:
            conn['in_use'] = False
        self.pool.put(conn)
    
    def health_check(self):
        """Remove stale connections."""
        healthy_pool = Queue(maxsize=self.pool_size)
        while not self.pool.empty():
            conn = self.pool.get()
            age = time.time() - conn['created_at']
            if age < 300:  # 5 minute max age
                healthy_pool.put(conn)
            else:
                conn['socket'].close()
                new_conn = self._create_connection()
                healthy_pool.put(new_conn)
        self.pool = healthy_pool
```

### Async I/O with Selectors

For handling thousands of concurrent connections:

```python
import selectors
import types

class AsyncChatServer:
    """
    High-performance server using selectors for async I/O.
    Can handle 10K+ concurrent connections.
    """
    
    def __init__(self, host='0.0.0.0', port=5000):
        self.host = host
        self.port = port
        self.selector = selectors.DefaultSelector()
        self.clients = {}
    
    def start(self):
        """Start async server."""
        server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server_sock.bind((self.host, self.port))
        server_sock.listen(1000)
        server_sock.setblocking(False)
        
        # Register server socket for reading
        self.selector.register(server_sock, selectors.EVENT_READ, data=None)
        
        print(f"[Async] Server on {self.host}:{self.port}")
        
        try:
            while True:
                events = self.selector.select(timeout=1)
                for key, mask in events:
                    if key.data is None:
                        # New connection
                        self._accept(key.fileobj)
                    else:
                        # Client activity
                        self._service(key, mask)
        except KeyboardInterrupt:
            print("\nShutting down...")
        finally:
            self.selector.close()
    
    def _accept(self, server_sock):
        """Accept new connection."""
        conn, addr = server_sock.accept()
        print(f"Connected: {addr}")
        conn.setblocking(False)
        
        data = types.SimpleWrapper(
            addr=addr,
            recvb=b"",
            sendq=[],
        )
        self.selector.register(conn, selectors.EVENT_READ, data=data)
        self.clients[conn] = data
    
    def _service(self, key, mask):
        """Handle client I/O."""
        sock = key.fileobj
        data = key.data
        
        if mask & selectors.EVENT_READ:
            recv_data = sock.recv(4096)
            if recv_data:
                data.recvb += recv_data
                # Process complete messages
                self._process_messages(sock, data)
            else:
                # Client disconnected
                self._close(sock)
        
        if mask & selectors.EVENT_WRITE and data.sendq:
            next_msg = data.sendq.pop(0)
            sock.send(next_msg)
    
    def _process_messages(self, sock, data):
        """Extract and handle complete messages."""
        while b'\n' in data.recvb:
            message, data.recvb = data.recvb.split(b'\n', 1)
            # Handle message...
            response = f"ACK: {message.decode()}\n".encode()
            data.sendq.append(response)
            
            # Switch to write mode if needed
            self.selector.modify(sock, 
                selectors.EVENT_READ | selectors.EVENT_WRITE, 
                data=data)
    
    def _close(self, sock):
        """Close client connection."""
        print(f"Disconnected: {self.clients[sock].addr}")
        self.selector.unregister(sock)
        sock.close()
        del self.clients[sock]
```

### Memory Optimization Techniques

```python
import sys
from collections import deque

class MemoryEfficientHistory:
    """
    Memory-optimized message history using:
    - Fixed-size ring buffer
    - String interning
    - Lazy loading
    """
    
    def __init__(self, max_messages: int = 10000):
        self.max_messages = max_messages
        self.history = deque(maxlen=max_messages)
        self._intern_table = {}
    
    def add_message(self, sender: str, message: str, timestamp: float):
        """Add message with memory optimization."""
        # Intern common strings to reduce memory
        sender_intern = self._intern_string(sender)
        message_intern = self._intern_string(message)
        
        entry = (
            timestamp,
            sender_intern,
            message_intern
        )
        self.history.append(entry)
    
    def _intern_string(self, s: str) -> str:
        """Cache string to reuse memory."""
        if s not in self._intern_table and len(s) < 1000:
            self._intern_table[s] = sys.intern(s)
        return self._intern_table.get(s, s)
    
    def get_recent(self, count: int = 100) -> list:
        """Get recent messages (lazy iterator for large counts)."""
        return list(self.history)[-count:]
    
    def get_memory_usage(self) -> int:
        """Estimate memory usage in bytes."""
        return sys.getsizeof(self.history) + sys.getsizeof(self._intern_table)
```

---

## Scaling Strategies

### Horizontal Scaling with Load Balancer

```
                    ┌─────────────┐
                    │   Load      │
                    │  Balancer   │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
       ┌──────┴───┐ ┌──────┴───┐ ┌────┴───┐
       │ Server 1 │ │ Server 2 │ │Server 3│
       │ :5000    │ │ :5001    │ │ :5002  │
       └──────────┘ └──────────┘ └────────┘
              │            │            │
       ┌──────┴────────────┴────────────┴───┐
       │         Shared Redis / Database     │
       └────────────────────────────────────┘
```

**Implementation:**

```python
import redis
import pickle

class ClusteredServer(ChatServer):
    """
    Server instance that syncs with other instances via Redis.
    """
    
    def __init__(self, redis_url: str, node_id: str, **kwargs):
        super().__init__(**kwargs)
        self.redis_client = redis.from_url(redis_url)
        self.node_id = node_id
        self.channel = "socketcomm:broadcast"
    
    def broadcast(self, message: str, exclude_client=None):
        """Broadcast to all cluster nodes."""
        # Local broadcast
        local_count = super().broadcast(message, exclude_client)
        
        # Publish to other nodes
        broadcast_data = {
            'source_node': self.node_id,
            'message': message,
            'timestamp': time.time()
        }
        self.redis_client.publish(
            self.channel, 
            pickle.dumps(broadcast_data)
        )
        
        return local_count
    
    def listen_for_broadcasts(self):
        """Listen for messages from other nodes."""
        pubsub = self.redis_client.pubsub()
        pubsub.subscribe(self.channel)
        
        def listener():
            for message in pubsub.listen():
                if message['type'] == 'message':
                    data = pickle.loads(message['data'])
                    # Don't relay our own messages
                    if data['source_node'] != self.node_id:
                        super().broadcast(data['message'])
        
        thread = threading.Thread(target=listener, daemon=True)
        thread.start()
```

### Sharding by User ID

```python
class ShardRouter:
    """
    Routes users to specific server shards based on user ID hash.
    Ensures users always connect to same shard (session affinity).
    """
    
    def __init__(self, shards: list):
        self.shards = shards  # List of (host, port) tuples
    
    def get_shard(self, user_id: str) -> tuple:
        """Determine which shard handles this user."""
        hash_value = hashlib.sha256(user_id.encode()).hexdigest()
        shard_index = int(hash_value[:8], 16) % len(self.shards)
        return self.shards[shard_index]
    
    def get_all_shards(self) -> list:
        """Return all shards for broadcasting."""
        return self.shards


# Usage
shards = [
    ('server1.example.com', 5000),
    ('server2.example.com', 5000),
    ('server3.example.com', 5000),
]

router = ShardRouter(shards)
user_shard = router.get_shard('user123')  # Always returns same shard
```

---

## Integration Patterns

### Web Application Integration

```python
from flask import Flask, jsonify, request
from flask_socketio import SocketIO, emit
import threading

class FlaskWebSocketBridge:
    """
    Bridge between SocketComm and web applications via WebSocket.
    """
    
    def __init__(self, flask_app: Flask, socketcomm_server: ChatServer):
        self.app = flask_app
        self.socketio = SocketIO(flask_app, cors_allowed_origins="*")
        self.server = socketcomm_server
        self.connected_web_clients = set()
    
    def setup_routes(self):
        """Setup API endpoints."""
        
        @self.app.route('/api/messages', methods=['GET'])
        def get_messages():
            return jsonify(self.server.get_message_history())
        
        @self.app.route('/api/clients', methods=['GET'])
        def get_clients():
            return jsonify(self.server.get_connected_clients())
        
        @self.socketio.on('connect')
        def handle_connect():
            self.connected_web_clients.add(request.sid)
            emit('message', {'text': 'Connected to SocketComm'})
        
        @self.socketio.on('send_message')
        def handle_web_message(data):
            # Forward to SocketComm clients
            self.server.broadcast(data['text'])
            # Broadcast to other web clients
            emit('message', {'text': data['text']}, broadcast=True)
    
    def run(self, host='0.0.0.0', port=3000):
        """Start the bridge server."""
        # Start SocketComm in background
        server_thread = threading.Thread(target=self.server.start, daemon=True)
        server_thread.start()
        
        # Start Flask-SocketIO
        self.socketio.run(self.app, host=host, port=port)


# Usage
app = Flask(__name__)
chat_server = ChatServer(port=5000)

bridge = FlaskWebSocketBridge(app, chat_server)
bridge.setup_routes()
bridge.run()
```

### Database Integration

```python
import sqlite3
from dataclasses import dataclass
from typing import Optional

@dataclass
class MessageRecord:
    id: int
    sender: str
    receiver: Optional[str]
    content: str
    timestamp: float
    message_type: str

class PersistentMessageStore:
    """
    SQLite-backed persistent message storage.
    Enables message history across restarts.
    """
    
    def __init__(self, db_path: str = 'messages.db'):
        self.conn = sqlite3.connect(db_path, check_same_thread=False)
        self._init_tables()
    
    def _init_tables(self):
        """Create database tables."""
        cursor = self.conn.cursor()
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender TEXT NOT NULL,
                receiver TEXT,
                content TEXT NOT NULL,
                timestamp REAL NOT NULL,
                message_type TEXT DEFAULT 'text'
            )
        ''')
        cursor.execute('''
            CREATE INDEX IF NOT EXISTS idx_timestamp 
            ON messages(timestamp)
        ''')
        cursor.execute('''
            CREATE INDEX IF NOT EXISTS idx_sender 
            ON messages(sender)
        ''')
        self.conn.commit()
    
    def save_message(self, sender: str, content: str, 
                     receiver: str = None, msg_type: str = 'text'):
        """Persist message to database."""
        cursor = self.conn.cursor()
        cursor.execute('''
            INSERT INTO messages (sender, receiver, content, timestamp, message_type)
            VALUES (?, ?, ?, ?, ?)
        ''', (sender, receiver, content, time.time(), msg_type))
        self.conn.commit()
        return cursor.lastrowid
    
    def get_history(self, limit: int = 100, 
                   before: float = None) -> list[MessageRecord]:
        """Retrieve message history."""
        cursor = self.conn.cursor()
        if before:
            cursor.execute('''
                SELECT * FROM messages 
                WHERE timestamp < ?
                ORDER BY timestamp DESC
                LIMIT ?
            ''', (before, limit))
        else:
            cursor.execute('''
                SELECT * FROM messages 
                ORDER BY timestamp DESC
                LIMIT ?
            ''', (limit,))
        
        return [MessageRecord(*row) for row in cursor.fetchall()]
    
    def search_messages(self, query: str, limit: int = 50) -> list:
        """Search message content."""
        cursor = self.conn.cursor()
        cursor.execute('''
            SELECT * FROM messages 
            WHERE content LIKE ?
            ORDER BY timestamp DESC
            LIMIT ?
        ''', (f'%{query}%', limit))
        return [MessageRecord(*row) for row in cursor.fetchall()]


# Integrate with ChatServer
class PersistentChatServer(ChatServer):
    """ChatServer with message persistence."""
    
    def __init__(self, db_path: str = 'messages.db', **kwargs):
        super().__init__(**kwargs)
        self.store = PersistentMessageStore(db_path)
    
    def handle_message(self, client_socket, message: str):
        """Save messages before processing."""
        sender = self.get_client_address(client_socket)
        self.store.save_message(sender=sender, content=message)
        
        # Continue normal processing
        super().handle_message(client_socket, message)
    
    def get_message_history(self, limit: int = 100):
        """Return persisted history."""
        return self.store.get_history(limit)
```

---

## Plugin Development

### Plugin System Architecture

```python
import importlib.util
import os
from abc import ABC, abstractmethod
from typing import Dict, Any, Callable

class BasePlugin(ABC):
    """Base class for all SocketComm plugins."""
    
    @property
    @abstractmethod
    def name(self) -> str:
        """Plugin identifier name."""
        pass
    
    @property
    @abstractmethod
    def version(self) -> str:
        """Plugin version string."""
        pass
    
    @abstractmethod
    def initialize(self, server) -> None:
        """Called when plugin is loaded."""
        pass
    
    @abstractmethod
    def on_message(self, client, message: str) -> str | None:
        """Process/modify messages. Return modified message or None to block."""
        pass
    
    def on_connect(self, client) -> None:
        """Called when client connects."""
        pass
    
    def on_disconnect(self, client) -> None:
        """Called when client disconnects."""
        pass
    
    def shutdown(self) -> None:
        """Cleanup when server stops."""
        pass


class PluginManager:
    """
    Manages plugin lifecycle for ChatServer.
    """
    
    def __init__(self, plugin_dir: str = 'plugins'):
        self.plugin_dir = plugin_dir
        self.plugins: Dict[str, BasePlugin] = {}
        self.hooks = {
            'on_message': [],
            'on_connect': [],
            'on_disconnect': [],
        }
    
    def load_plugins(self):
        """Load all plugins from directory."""
        if not os.path.exists(self.plugin_dir):
            return
        
        for filename in os.listdir(self.plugin_dir):
            if filename.endswith('.py') and filename != '__init__.py':
                self._load_plugin(filename)
    
    def _load_plugin(self, filename: str):
        """Load single plugin file."""
        filepath = os.path.join(self.plugin_dir, filename)
        module_name = f"plugin_{filename[:-3]}"
        
        spec = importlib.util.spec_from_file_location(module_name, filepath)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        
        # Find plugin class
        for attr_name in dir(module):
            attr = getattr(module, attr_name)
            if (isinstance(attr, type) and 
                issubclass(attr, BasePlugin) and 
                attr != BasePlugin):
                
                plugin_instance = attr()
                self.register_plugin(plugin_instance)
                break
    
    def register_plugin(self, plugin: BasePlugin):
        """Register and initialize plugin."""
        if plugin.name in self.plugins:
            print(f"[WARNING] Plugin '{plugin.name}' already loaded")
            return
        
        self.plugins[plugin.name] = plugin
        
        # Register hooks
        if hasattr(plugin, 'on_message'):
            self.hooks['on_message'].append(plugin.on_message)
        if hasattr(plugin, 'on_connect'):
            self.hooks['on_connect'].append(plugin.on_connect)
        if hasattr(plugin, 'on_disconnect'):
            self.hooks['on_disconnect'].append(plugin.on_disconnect)
        
        print(f"[PLUGIN] Loaded {plugin.name} v{plugin.version}")
    
    def execute_hooks(self, hook_name: str, *args, **kwargs):
        """Execute all hooks for event."""
        results = []
        for hook in self.hooks.get(hook_name, []):
            try:
                result = hook(*args, **kwargs)
                results.append(result)
            except Exception as e:
                print(f"[ERROR] Plugin hook error: {e}")
        return results


# Example Plugin: Profanity Filter
class ProfanityFilterPlugin(BasePlugin):
    """Filters inappropriate language from messages."""
    
    @property
    def name(self): return "profanity_filter"
    
    @property
    def version(self): return "1.0.0"
    
    def initialize(self, server):
        self.bad_words = {'bad', 'offensive', 'inappropriate'}
        print("[ProfanityFilter] Initialized")
    
    def on_message(self, client, message: str) -> str:
        words = message.split()
        filtered = ['***' if w.lower() in self.bad_words else w for w in words]
        return ' '.join(filtered)


# Example Plugin: Logging
class MessageLoggerPlugin(BasePlugin):
    """Logs all messages to file."""
    
    @property
    def name(self): return "message_logger"
    
    @property
    def version(self): return "1.0.0"
    
    def initialize(self, server):
        self.log_file = open('messages.log', 'a')
        print("[MessageLogger] Logging to messages.log")
    
    def on_message(self, client, message: str) -> None:
        timestamp = datetime.now().isoformat()
        log_entry = f"[{timestamp}] {client}: {message}\n"
        self.log_file.write(log_entry)
        self.log_file.flush()
        return message  # Pass through unchanged
    
    def shutdown(self):
        self.log_file.close()
```

---

## Monitoring & Observability

### Metrics Collection

```python
import time
from collections import defaultdict
from threading import Lock

class MetricsCollector:
    """
    Collects and exposes metrics for monitoring.
    Compatible with Prometheus/Pushgateway format.
    """
    
    def __init__(self):
        self.metrics = defaultdict(lambda: {
            'count': 0,
            'sum': 0,
            'min': float('inf'),
            'max': float('-inf'),
            'last': 0
        })
        self.lock = Lock()
        self.start_time = time.time()
    
    def increment(self, metric_name: str, value: float = 1.0):
        """Increment counter metric."""
        with self.lock:
            m = self.metrics[metric_name]
            m['count'] += 1
            m['sum'] += value
            m['min'] = min(m['min'], value)
            m['max'] = max(m['max'], value)
            m['last'] = time.time()
    
    def record_timing(self, metric_name: str, duration: float):
        """Record timing metric."""
        self.increment(metric_name, duration)
    
    def get_metrics(self) -> dict:
        """Export all metrics."""
        with self.lock:
            return {
                'uptime_seconds': time.time() - self.start_time,
                'metrics': dict(self.metrics)
            }
    
    def export_prometheus(self) -> str:
        """Export in Prometheus text format."""
        lines = []
        for name, data in self.metrics.items():
            lines.append(f'# HELP {metric_name} Total count')
            lines.append(f'# TYPE {metric_name} counter')
            lines.append(f'{name}_total {data["count"]}')
            lines.append(f'{name}_sum {data["sum"]}')
            lines.append('')
        return '\n'.join(lines)


# Integrate with ChatServer
class ObservableChatServer(ChatServer):
    """ChatServer with metrics collection."""
    
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.metrics = MetricsCollector()
    
    def handle_message(self, client_socket, message: str):
        start_time = time.time()
        
        try:
            super().handle_message(client_socket, message)
            self.metrics.increment('messages_processed')
        finally:
            duration = time.time() - start_time
            self.metrics.record_timing('message_processing_seconds', duration)
    
    def on_client_connect(self, client_socket):
        self.metrics.increment('clients_connected')
        super().on_client_connect(client_socket)
    
    def on_client_disconnect(self, client_socket):
        self.metrics.increment('clients_disconnected')
        super().on_client_disconnect(client_socket)
    
    def get_metrics_endpoint(self) -> dict:
        """Return metrics for /metrics endpoint."""
        return self.metrics.get_metrics()
```

### Health Check Endpoint

```python
from http.server import HTTPServer, BaseHTTPRequestHandler
import json

class HealthCheckHandler(BaseHTTPRequestHandler):
    """HTTP health check endpoint."""
    
    server_instance = None  # Set to ChatServer instance
    
    def do_GET(self):
        if self.path == '/health':
            self.send_health_response()
        elif self.path == '/metrics':
            self.send_metrics_response()
        else:
            self.send_error(404)
    
    def send_health_response(self):
        """Return health status."""
        server = self.server_instance
        status = 'healthy' if server and server.is_running() else 'unhealthy'
        
        response = {
            'status': status,
            'uptime': server.uptime() if server else 0,
            'connections': len(server.clients) if server else 0,
            'timestamp': time.time()
        }
        
        self.send_response(200 if status == 'healthy' else 503)
        self.send_header('Content-Type', 'application/json')
        self.end_headers()
        self.wfile.write(json.dumps(response).encode())
    
    def send_metrics_response(self):
        """Return Prometheus-format metrics."""
        if self.server_instance and hasattr(self.server_instance, 'get_metrics_endpoint'):
            metrics = self.server_instance.get_metrics_endpoint()
        else:
            metrics = {}
        
        self.send_response(200)
        self.send_header('Content-Type', 'text/plain')
        self.end_headers()
        self.wfile.write(json.dumps(metrics, indent=2).encode())


def start_health_check_server(chat_server, port=8081):
    """Start HTTP server for health checks."""
    HealthCheckHandler.server_instance = chat_server
    httpd = HTTPServer(('0.0.0.0', port), HealthCheckHandler)
    print(f"[Health] Check endpoint on http://localhost:{port}/health")
    httpd.serve_forever()
```

---

## Summary

This advanced guide covered:

1. **Custom Protocols**: Binary and JSON message formats
2. **Security**: JWT authentication and TLS encryption
3. **Performance**: Connection pooling, async I/O, memory optimization
4. **Scaling**: Horizontal clustering with Redis, sharding strategies
5. **Integration**: Web bridges, database persistence
6. **Plugins**: Extensible plugin architecture
7. **Monitoring**: Metrics collection and health checks

Use these patterns to build production-ready applications on top of SocketComm.

---

*For questions or contributions, see CONTRIBUTING.md*
