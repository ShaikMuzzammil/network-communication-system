#!/usr/bin/env python3
"""
NexusChat Basic Chat Example

This example demonstrates how to use the core chat functionality
without the GUI interface.

Usage:
    python examples/basic-chat.py          # Start as server
    python examples/basic-chat.py --client  # Connect to server
"""

import sys
import argparse
from datetime import datetime

# Add modules to path
sys.path.insert(0, 'python-modules/chat-core/src')

from chat_core.server import ChatServer
from chat_core.client import ChatClient


def run_server(host: str = '127.0.0.1', port: int = 12345):
    """Run a simple chat server."""
    print("=" * 50)
    print("NexusChat Server - Console Mode")
    print("=" * 50)
    print(f"Starting server on {host}:{port}...")
    print()
    
    def on_message(client_id: str, message: str):
        timestamp = datetime.now().strftime("%H:%M:%S")
        print(f"[{timestamp}] [{client_id}]: {message}")
        # Echo to all clients
        server.broadcast_message(f"[{client_id}]: {message}", exclude_client=client_id)
    
    def on_connect(client_id: str, address: tuple):
        print(f"* Client connected from {address[0]}:{address[1]} (ID: {client_id})")
        server.broadcast_message(f"* {client_id} joined the chat")
    
    def on_disconnect(client_id: str):
        print(f"* Client disconnected: {client_id}")
        server.broadcast_message(f"* {client_id} left the chat")
    
    server = ChatServer(host=host, port=port)
    server.on_message_callback = on_message
    server.on_connect_callback = on_connect
    server.on_disconnect_callback = on_disconnect
    
    try:
        if server.start():
            print("\n✓ Server is running!")
            print("Press Ctrl+C to stop\n")
            
            while True:
                import time
                time.sleep(1)
                
    except KeyboardInterrupt:
        print("\n\nShutting down server...")
        server.stop()
        print("✓ Server stopped.")


def run_client(host: str = '127.0.0.1', port: int = 12345):
    """Run a simple chat client."""
    print("=" * 50)
    print("NexusChat Client - Console Mode")
    print("=" * 50)
    print(f"Connecting to {host}:{port}...")
    print()
    
    def on_message(msg):
        timestamp = msg.timestamp.strftime("%H:%M:%S")
        print(f"\r[{timestamp}] {msg.content}")
        print("> ", end="", flush=True)
    
    def on_connect():
        print("* Connected to server!")
        print("> ", end="", flush=True)
    
    def on_disconnect():
        print("\n* Disconnected from server")
    
    client = ChatClient(
        host=host,
        port=port,
        auto_reconnect=True,
        reconnect_delay=2
    )
    client.on_message_callback = on_message
    client.on_connect_callback = on_connect
    client.on_disconnect_callback = on_disconnect
    
    if client.connect():
        print("\n✓ Connected! Type messages and press Enter.")
        print("Type 'quit' or press Ctrl+C to exit.\n")
        
        try:
            while client.is_connected:
                msg = input("> ")
                if msg.lower() == 'quit':
                    break
                if msg.strip():
                    client.send_message(msg)
                    
        except KeyboardInterrupt:
            pass
        
        client.disconnect()
        print("\n✓ Disconnected.")
    else:
        print("✗ Failed to connect to server.")
        return 1
    
    return 0


def main():
    parser = argparse.ArgumentParser(
        description='NexusChat Basic Chat Example',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  %(prog)s                  Start as server (default)
  %(prog)s --client         Start as client
  %(prog)s --port 8080      Use custom port
  %(prog)s --client --host 192.168.1.100
        """
    )
    
    parser.add_argument(
        '--client', '-c',
        action='store_true',
        help='Run as client mode'
    )
    parser.add_argument(
        '--host', '-H',
        default='127.0.0.1',
        help='Host address (default: 127.0.0.1)'
    )
    parser.add_argument(
        '--port', '-p',
        type=int,
        default=12345,
        help='Port number (default: 12345)'
    )
    
    args = parser.parse_args()
    
    if args.client:
        return run_client(args.host, args.port)
    else:
        return run_server(args.host, args.port)


if __name__ == '__main__':
    sys.exit(main())
