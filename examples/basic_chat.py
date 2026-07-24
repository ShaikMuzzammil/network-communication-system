#!/usr/bin/env python3
"""
Basic Chat Example - SocketCommunication
========================================

A simple example demonstrating client-server chat functionality.
Run this script to start a basic chat server and client.
"""

import sys
import threading
import time
from socketcomm import SocketServer, SocketClient


def run_server(host: str = 'localhost', port: int = 8080):
    """Start a simple chat server."""
    print(f"🚀 Starting server on {host}:{port}...")
    
    server = SocketServer(host=host, port=port)
    server.start()
    
    print(f"✅ Server is running! Waiting for connections...")
    print("Press Ctrl+C to stop the server.\n")
    
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n🛑 Shutting down server...")
        server.stop()
        print("👋 Server stopped.")


def run_client(host: str = 'localhost', port: int = 8080):
    """Start a simple chat client."""
    print(f"🔗 Connecting to server at {host}:{port}...")
    
    client = SocketClient(host=host, port=port)
    client.connect()
    
    print("✅ Connected to server!")
    print("Type your messages and press Enter to send.")
    Press 'quit' to disconnect.\n")
    
    def receive_messages():
        """Background thread to receive messages."""
        while client.is_connected():
            try:
                message = client.receive_message()
                if message:
                    print(f"\n📨 Received: {message}")
                    print("> ", end="", flush=True)
            except Exception as e:
                break
    
    # Start receiver thread
    receiver_thread = threading.Thread(target=receive_messages, daemon=True)
    receiver_thread.start()
    
    try:
        while True:
            message = input("> ")
            
            if message.lower() == 'quit':
                break
            
            if message:
                client.send_message(message)
                print("✉️ Message sent!")
    
    except KeyboardInterrupt:
        pass
    
    finally:
        print("\n🔌 Disconnecting...")
        client.disconnect()
        print("👋 Goodbye!")


def main():
    """Main entry point."""
    import argparse
    
    parser = argparse.ArgumentParser(
        description='Basic Chat Example - SocketCommunication',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  %(prog)s --mode server         Start a chat server
  %(prog)s --mode client         Start a chat client
  %(prog)s --mode server --port 9000  Start server on custom port
        """
    )
    
    parser.add_argument(
        '--mode', '-m',
        choices=['server', 'client'],
        required=True,
        help='Run as server or client'
    )
    
    parser.add_argument(
        '--host', '-H',
        default='localhost',
        help='Host address (default: localhost)'
    )
    
    parser.add_argument(
        '--port', '-p',
        type=int,
        default=8080,
        help='Port number (default: 8080)'
    )
    
    args = parser.parse_args()
    
    if args.mode == 'server':
        run_server(args.host, args.port)
    else:
        run_client(args.host, args.port)


if __name__ == '__main__':
    main()
