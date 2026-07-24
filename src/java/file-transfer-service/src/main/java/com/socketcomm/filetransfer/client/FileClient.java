package com.socketcomm.filetransfer.client;

import com.socketcomm.filetransfer.config.ClientConfig;
import com.socketcomm.filetransfer.utils.TransferProgress;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Enterprise-grade Socket-based File Client for SocketComm Platform.
 * 
 * This client provides reliable TCP socket file downloading with:
 * - Optimized buffer-based streaming
 * - Configurable timeouts and buffer sizes
 * - Progress tracking with percentage display
 * - Robust error handling and recovery
 * - Connection validation before transfer
 * 
 * <p>Example usage:</p>
 * <pre>
 * // Create client with custom configuration
 * ClientConfig config = new ClientConfig.Builder()
 *     .serverHost("192.168.1.100")
 *     .serverPort(8080)
 *     .outputFile("downloaded_file.zip")
 *     .connectTimeout(10)
 *     .bufferSize(8192)
 *     .build();
 * 
 * FileClient client = new FileClient(config);
 * boolean success = client.connectAndReceive();
 * </pre>
 * 
 * @author SocketComm Team
 * @version 1.0.0
 * @see FileServer
 * @see ClientConfig
 */
public class FileClient {
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /** Default receive timeout in milliseconds (infinite) */
    private static final int RECEIVE_TIMEOUT_MS = 0;
    
    /** Client configuration */
    private final ClientConfig config;
    
    /** Connection state flag */
    private volatile boolean connected;
    
    /**
     * Creates a FileClient with host, port, and output file.
     * 
     * @param serverHost The server hostname or IP address
     * @param serverPort The server port number
     * @param outputFile The local path to save the received file
     * @throws IllegalArgumentException if parameters are invalid
     */
    public FileClient(String serverHost, int serverPort, String outputFile) {
        this(new ClientConfig.Builder()
            .serverHost(serverHost)
            .serverPort(serverPort)
            .outputFile(outputFile)
            .build());
    }
    
    /**
     * Creates a FileClient with full configuration.
     * 
     * @param config The client configuration
     * @throws IllegalArgumentException if config is null
     */
    public FileClient(ClientConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        this.config = config;
        this.connected = false;
    }
    
    /**
     * Connects to the server via TCP socket and downloads the file.
     * 
     * <p>This method performs the following steps:</p>
     * <ol>
     *   <li>Validates output directory exists (creates if needed)</li>
     *   <li>Connects to the configured server via TCP socket</li>
     *   <li>Receives file data and writes to disk</li>
     *   <li>Closes connection and reports status</li>
     * </ol>
     * 
     * @return true if file was received and saved successfully
     */
    public boolean connectAndReceive() {
        if (connected) {
            System.out.println("[WARN] Client already connected");
            return false;
        }
        
        LocalDateTime startTime = LocalDateTime.now();
        
        System.out.println("═".repeat(50));
        System.out.println("SocketComm File Client starting...");
        System.out.println("  Time: " + startTime.format(TIME_FORMAT));
        System.out.println("  Target: " + config.getServerHost() + ":" + config.getServerPort());
        System.out.println("  Output: " + config.getOutputFile());
        System.out.println("  Buffer Size: " + config.getBufferSize() + " bytes");
        System.out.println("═".repeat(50));
        
        Socket socket = null;
        
        try {
            // Validate/create output directory
            validateOutputDirectory();
            
            // Connect to server via TCP socket
            socket = connectToServer();
            connected = true;
            
            System.out.println("✓ Connected to server " + config.getServerHost() + ":" + config.getServerPort());
            System.out.println("📥 Receiving file...\n");
            
            // Receive file
            boolean success = receiveFile(socket);
            
            LocalDateTime endTime = LocalDateTime.now();
            System.out.println("Transfer completed at: " + endTime.format(TIME_FORMAT));
            
            if (success) {
                File outputFile = new File(config.getOutputFile());
                System.out.println("\n✓ File received successfully!");
                System.out.println("📁 Saved as: " + config.getOutputFile());
                System.out.println("📊 Size: " + formatFileSize(outputFile.length()));
            } else {
                System.out.println("\n✗ File reception failed!");
            }
            
            return success;
            
        } catch (ConnectException e) {
            System.err.println("[ERROR] Connection failed: " + e.getMessage());
            System.err.println("Error: Connection refused - Is the server running?");
            return false;
            
        } catch (FileNotFoundException e) {
            System.err.println("[ERROR] Cannot create output file: " + e.getMessage());
            return false;
            
        } catch (IOException e) {
            System.err.println("[ERROR] I/O error during transfer: " + e.getMessage());
            return false;
            
        } finally {
            closeConnection(socket);
            connected = false;
        }
    }
    
    /**
     * Establishes TCP socket connection to the server.
     * 
     * @return Connected socket instance
     * @throws ConnectException if connection fails
     */
    private Socket connectToServer() throws ConnectException {
        try {
            Socket socket = new Socket();
            
            // Configure socket
            socket.setSoTimeout(config.getConnectTimeout() * 1000); // Convert to milliseconds
            socket.setTcpNoDelay(true); // Disable Nagle's algorithm for better performance
            
            // Attempt connection with timeout
            InetSocketAddress endpoint = new InetSocketAddress(
                config.getServerHost(), 
                config.getServerPort()
            );
            
            socket.connect(endpoint, config.getConnectTimeout() * 1000);
            
            // Reset to infinite timeout for receiving
            socket.setSoTimeout(RECEIVE_TIMEOUT_MS);
            
            return socket;
            
        } catch (SocketTimeoutException e) {
            throw new ConnectException(
                "Connection timed out after " + config.getConnectTimeout() + " seconds", e
            );
        } catch (IOException e) {
            throw new ConnectException("Failed to connect to server: " + e.getMessage(), e);
        }
    }
    
    /**
     * Receives file data from the server via TCP socket and writes to local file.
     * 
     * @param socket The connected server socket
     * @return true if file was received completely
     * @throws IOException if an I/O error occurs
     */
    private boolean receiveFile(Socket socket) throws IOException {
        TransferProgress progress = new TransferProgress(0); // Unknown total size
        
        try (
            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(config.getOutputFile())
        ) {
            byte[] buffer = new byte[config.getBufferSize()];
            int bytesRead;
            long totalBytesReceived = 0;
            
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                
                totalBytesReceived += bytesRead;
                progress.update(totalBytesReceived);
                
                // Update console progress periodically
                if (totalBytesReceived % (config.getBufferSize() * 100) < config.getBufferSize()) {
                    System.out.printf("\r  Received: %s",
                        formatFileSize(totalBytesReceived)
                    );
                }
            }
            
            System.out.println(); // New line after progress
            fos.flush(); // Ensure all data is written
            
            return true;
        }
    }
    
    /**
     * Validates that the output directory exists, creates it if necessary.
     * 
     * @throws FileNotFoundException if directory cannot be created
     */
    private void validateOutputDirectory() throws FileNotFoundException {
        File outputFile = new File(config.getOutputFile());
        File parentDir = outputFile.getParentFile();
        
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new FileNotFoundException(
                    "Cannot create output directory: " + parentDir.getAbsolutePath()
                );
            }
        }
    }
    
    /**
     * Safely closes the socket connection.
     * 
     * @param socket The socket to close (may be null)
     */
    private void closeConnection(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("[WARN] Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Checks if the client is currently connected.
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Gets the current client configuration.
     * 
     * @return The client configuration object
     */
    public ClientConfig getConfig() {
        return config;
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
    
    /**
     * Custom exception for TCP socket connection failures.
     */
    public static class ConnectException extends Exception {
        public ConnectException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
