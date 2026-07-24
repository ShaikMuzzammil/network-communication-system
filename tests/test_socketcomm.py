#!/usr/bin/env python3
"""
SocketCommunication Test Suite
==============================

Comprehensive tests for all SocketCommunication modules.
Run with: pytest tests/test_socketcomm.py -v
"""

import pytest
import threading
import time
import tempfile
import os
from unittest.mock import Mock, patch, MagicMock

# Import socketcomm modules
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

from socketcomm.protocol import SCMPProtocol, MessageType, SCMPHeader, SCMPMessage
from socketcomm.security import SecurityManager
from socketcomm.utils import ConfigManager, Logger, validate_host, validate_port
from socketcomm.server import SocketServer, ConnectionPool, Connection
from socketcomm.client import SocketClient


class TestSCMPProtocol:
    """Tests for SCMP Protocol implementation."""
    
    def test_header_creation(self):
        """Test SCMP header creation."""
        header = SCMPHeader(
            version=1,
            msg_type=MessageType.DATA,
            length=100,
            msg_id=12345,
            checksum=0xDEADBEEF
        )
        
        assert header.version == 1
        assert header.msg_type == MessageType.DATA
        assert header.length == 100
        
    def test_message_creation(self):
        """Test SCMP message creation."""
        msg = SCMPMessage(
            msg_type=MessageType.DATA,
            payload=b"Hello, World!"
        )
        
        assert msg.payload == b"Hello, World!"
        assert msg.header.msg_type == MessageType.DATA
    
    def test_message_serialization(self):
        """Test message serialization and deserialization."""
        protocol = SCMPProtocol()
        original_msg = SCMPMessage(
            msg_type=MessageType.DATA,
            payload=b"Test data for serialization"
        )
        
        # Serialize
        serialized = protocol.serialize(original_msg)
        assert isinstance(serialized, bytes)
        assert len(serialized) > 0
        
        # Deserialize
        deserialized_msg = protocol.deserialize(serialized)
        
        assert deserialized_msg.payload == original_msg.payload
        assert deserialized_msg.header.msg_type == original_msg.header.msg_type
    
    def test_checksum_calculation(self):
        """Test CRC32 checksum calculation."""
        protocol = SCMPProtocol()
        data = b"Test data for checksum"
        
        checksum = protocol.calculate_checksum(data)
        assert isinstance(checksum, int)
        assert checksum != 0
    
    def test_checksum_validation(self):
        """Test checksum validation."""
        protocol = SCMPProtocol()
        data = b"Valid data"
        
        # Valid checksum should pass
        valid_checksum = protocol.calculate_checksum(data)
        assert protocol.validate_checksum(data, valid_checksum) is True
        
        # Invalid checksum should fail
        assert protocol.validate_checksum(data, 0x00000000) is False


class TestSecurityManager:
    """Tests for Security Manager."""
    
    def setup_method(self):
        """Set up test fixtures."""
        self.security = SecurityManager()
    
    def test_rsa_key_generation(self):
        """Test RSA key pair generation."""
        self.security.generate_rsa_keys()
        
        assert self.security.private_key is not None
        assert self.security.public_key is not None
    
    def test_aes_encryption_decryption(self):
        """Test AES encryption and decryption."""
        plaintext = b"This is a secret message"
        
        # Encrypt
        ciphertext = self.security.aes_encrypt(plaintext)
        assert ciphertext != plaintext
        assert len(ciphertext) > 0
        
        # Decrypt
        decrypted = self.security.aes_decrypt(ciphertext)
        assert decrypted == plaintext
    
    def test_rsa_encryption_decryption(self):
        """Test RSA encryption and decryption."""
        self.security.generate_rsa_keys()
        
        plaintext = b"Secret key material"
        
        # Encrypt with public key
        encrypted = self.security.rsa_encrypt(plaintext)
        assert encrypted != plaintext
        
        # Decrypt with private key
        decrypted = self.security.rsa_decrypt(encrypted)
        assert decrypted == plaintext
    
    def test_hmac_generation_validation(self):
        """Test HMAC generation and validation."""
        message = b"Authenticated message"
        
        # Generate HMAC
        hmac_signature = self.security.generate_hmac(message)
        assert hmac_signature is not None
        
        # Validate HMAC (should pass)
        assert self.security.validate_hmac(message, hmac_signature) is True
        
        # Tampered message should fail
        assert self.security.validate_hmac(b"Tampered!", hmac_signature) is False


class TestConfigManager:
    """Tests for Configuration Manager."""
    
    def test_default_config(self):
        """Test default configuration loading."""
        config = ConfigManager()
        
        assert config.get('server.host', 'localhost') == 'localhost'
        assert config.get('server.port', 8080) == 8080
    
    def test_yaml_config_loading(self):
        """Test YAML configuration loading."""
        with tempfile.NamedTemporaryFile(mode='w', suffix='.yaml', delete=False) as f:
            f.write("""
server:
  host: "127.0.0.1"
  port: 9090
  max_connections: 50
""")
            config_path = f.name
        
        try:
            config = ConfigManager.load(config_path)
            
            assert config.get('server.host') == '127.0.0.1'
            assert config.get('server.port') == 9090
            assert config.get('server.max_connections') == 50
        finally:
            os.unlink(config_path)


class TestValidationFunctions:
    """Tests for validation utility functions."""
    
    def test_valid_host(self):
        """Test valid host validation."""
        assert validate_host('localhost') is True
        assert validate_host('127.0.0.1') is True
        assert validate_host('192.168.1.1') is True
        assert validate_host('example.com') is True
    
    def test_invalid_host(self):
        """Test invalid host rejection."""
        assert validate_host('') is False
        assert validate_host('invalid host') is False
        assert validate_host('256.256.256.256') is False
    
    def test_valid_port(self):
        """Test valid port validation."""
        assert validate_port(80) is True
        assert validate_port(8080) is True
        assert validate_port(65535) is True
    
    def test_invalid_port(self):
        """Test invalid port rejection."""
        assert validate_port(-1) is False
        assert validate_port(0) is False
        assert validate_port(65536) is False
        assert validate_port('abc') is False


class TestConnectionPool:
    """Tests for Connection Pool."""
    
    def setup_method(self):
        """Set up test fixtures."""
        self.pool = ConnectionPool(max_size=10)
    
    def test_empty_pool(self):
        """Test empty connection pool."""
        assert len(self.pool) == 0
        assert self.pool.is_empty() is True
    
    def test_add_connection(self):
        """Test adding connections to pool."""
        mock_conn = Mock(spec=Connection)
        mock_conn.client_id = "test-client-1"
        
        self.pool.add(mock_conn)
        
        assert len(self.pool) == 1
        assert self.pool.is_empty() is False
    
    def test_remove_connection(self):
        """Test removing connections from pool."""
        mock_conn = Mock(spec=Connection)
        mock_conn.client_id = "test-client-1"
        
        self.pool.add(mock_conn)
        self.pool.remove("test-client-1")
        
        assert len(self.pool) == 0
        assert self.pool.is_empty() is True
    
    def test_max_capacity(self):
        """Test pool maximum capacity enforcement."""
        for i in range(15):
            mock_conn = Mock(spec=Connection)
            mock_conn.client_id = f"client-{i}"
            self.pool.add(mock_conn)
        
        # Pool should not exceed max size
        assert len(self.pool) <= 10


class TestIntegration:
    """Integration tests for client-server communication."""
    
    @pytest.fixture(scope='class')
    def server_instance(self):
        """Start a test server."""
        server = SocketServer(host='localhost', port=18901)
        server.start()
        time.sleep(0.5)  # Allow server to start
        
        yield server
        
        server.stop()
    
    def test_server_startup(self, server_instance):
        """Test server starts successfully."""
        assert server_instance.is_running() is True
    
    def test_client_connection(self, server_instance):
        """Test client can connect to server."""
        client = SocketClient(host='localhost', port=18901)
        client.connect()
        
        assert client.is_connected() is True
        
        client.disconnect()


# Run tests if executed directly
if __name__ == '__main__':
    pytest.main([__file__, '-v', '--tb=short'])
