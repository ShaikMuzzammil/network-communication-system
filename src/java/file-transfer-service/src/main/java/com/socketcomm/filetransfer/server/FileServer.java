package com.socketcomm.filetransfer.server;

import com.socketcomm.filetransfer.config.ServerConfig;
import com.socketcomm.filetransfer.utils.TransferProgress;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Enterprise-grade Socket-based File Server for SocketComm Platform.
 * 
 * This server provides high-performance TCP socket file transfer capabilities with:
 * - Optimized buffer-based streaming for large files
 * - Configurable buffer sizes and timeouts
 * - Progress tracking and logging
 * - Graceful connection handling
 * - Error recovery support
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Create server with custom configuration
 * ServerConfig config = new ServerConfig.Builder()
 *     .port(8080)
 *     .fileName("large_file.zip")
 *     .bufferSize(8192)
 *     .build();
 * 
 * FileServer server = new FileServer(config);
 * server.start(); // Blocks until transfer complete
 * </pre>
 * 
 * @author SocketComm Team
 * @version 1.0.0
 * @see FileClient
 * @see ServerConfig
 */
public class FileServer {
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /** Server configuration */
    private final ServerConfig config;
    
    /** Server socket for accepting connections */
    private ServerSocket serverSocket;
    
    /** Flag indicating if server is running */
    private volatile boolean running;
    
    /**
     * Creates a FileServer with specified port and file name.
     * 
     * @param port The port number to listen on
     * @param fileName The path of the file to serve
     * @throws IllegalArgumentException if parameters are invalid
     */
    public FileServer(int port, String fileName) {
        this(new ServerConfig.Builder()
            .port(port)
            .fileName(fileName)
            .build());
    }
    
    /**
     * Creates a FileServer with full configuration.
     * 
     * @param config The server configuration
     * @throws IllegalArgumentException if config is null
     */
    public FileServer(ServerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        this.config = config;
        this.running = false;
    }
    
    /**
     * Starts the socket file server and waits for a client connection.
     * 
     * <p>This method blocks until:</p>
     * <ul>
     *   <li>A client connects and receives the file successfully</li>
     *   <li>An error occurs during transfer</li>
     *   <li>The server is stopped via {@link #stop()}</li>
     * </ul>
     * 
     * @return true if file was transferred successfully, false otherwise
     */
    public boolean start() {
        if (running) {
            System.out.println("[WARN] Server is already running");
            return false;
        }
        
        running = true;
        LocalDateTime startTime = LocalDateTime.now();
        
        System.out.println("═".repeat(50));
        System.out.println("SocketComm File Server starting...");
        System.out.println("  Time: " + startTime.format(TIME_FORMAT));
        System.out.println("  Port: " + config.getPort());
        System.out.println("  File: " + config.getFileName());
        System.out.println("  Buffer Size: " + config.getBufferSize() + " bytes");
        System.out.println("═".repeat(50));
        
        try {
            // Validate file exists
            File file = new File(config.getFileName());
            if (!file.exists()) {
                System.err.println("[ERROR] File not found: " + config.getFileName());
                System.err.println("Error: File not found - " + config.getFileName());
                return false;
            }
            
            long fileSize = file.length();
            System.out.println("  File Size: " + fileSize + " bytes (" + formatFileSize(fileSize) + ")");
            
            // Create server socket
            serverSocket = new ServerSocket(config.getPort());
            System.out.println("Server started. Waiting for client connection...");
            System.out.println("\n✓ Server started on port " + config.getPort());
            System.out.println("⏳ Waiting for client...\n");
            
            // Accept client connection
            Socket clientSocket = serverSocket.accept();
            InetAddress clientAddress = clientSocket.getInetAddress();
            
            System.out.println("✓ Client connected from " + clientAddress.getHostAddress());
            System.out.println("📤 Starting file transfer...\n");
            
            // Perform file transfer
            boolean success = transferFile(clientSocket, file, fileSize);
            
            // Cleanup
            closeConnection(clientSocket);
            
            LocalDateTime endTime = LocalDateTime.now();
            System.out.println("Transfer completed at: " + endTime.format(TIME_FORMAT));
            
            if (success) {
                System.out.println("\n✓ File sent successfully!");
            } else {
                System.out.println("\n✗ File transfer failed!");
            }
            
            return success;
            
        } catch (BindException e) {
            System.err.println("[ERROR] Port " + config.getPort() + " already in use");
            System.err.println("Error: Port " + config.getPort() + " is already in use");
            return false;
            
        } catch (IOException e) {
            System.err.println("[ERROR] Server I/O error: " + e.getMessage());
            return false;
            
        } finally {
            stop();
        }
    }
    
    /**
     * Transfers the file content to the connected client over TCP socket.
     * 
     * @param clientSocket The connected client socket
     * @param file The file to transfer
     * @param fileSize The size of the file in bytes
     * @return true if transfer completed successfully
     */
    private boolean transferFile(Socket clientSocket, File file, long fileSize) {
        TransferProgress progress = new TransferProgress(fileSize);
        
        try (
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = clientSocket.getOutputStream()
        ) {
            byte[] buffer = new byte[config.getBufferSize()];
            int bytesRead;
            long totalBytesRead = 0;
            
            while ((bytesRead = fis.read(buffer)) != -1 && running) {
                os.write(buffer, 0, bytesRead);
                
                totalBytesRead += bytesRead;
                progress.update(totalBytesRead);
                
                // Log progress periodically
                if (progress.shouldLog()) {
                    double percent = progress.getPercentComplete();
                    
                    // Console progress indicator
                    if (totalBytesRead % (config.getBufferSize() * 100) < config.getBufferSize()) {
                        System.out.printf("\r  Progress: %.1f%% (%s / %s)",
                            percent,
                            formatFileSize(totalBytesRead),
                            formatFileSize(fileSize)
                        );
                    }
                }
            }
            
            // Ensure all data is flushed
            os.flush();
            System.out.println(); // New line after progress
            
            return totalBytesRead == fileSize || !running;
            
        } catch (IOException e) {
            System.err.println("[ERROR] Transfer error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Stops the server gracefully.
     * 
     * Closes the server socket and sets the running flag to false,
     * which will cause any ongoing transfer to stop.
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        System.out.println("Stopping server...");
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("Server socket closed");
            } catch (IOException e) {
                System.err.println("[WARN] Error closing server socket: " + e.getMessage());
            }
        }
    }
    
    /**
     * Checks if the server is currently running.
     * 
     * @return true if server is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Gets the current server configuration.
     * 
     * @return The server configuration object
     */
    public ServerConfig getConfig() {
        return config;
    }
    
    /**
     * Safely closes a client socket connection.
     * 
     * @param socket The socket to close
     */
    private void closeConnection(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("[WARN] Error closing client connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Formats file size into human-readable string.
     * 
     * @param bytes The size in bytes
     * @return Formatted string (e.g., "1.5 MB")
     */
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
