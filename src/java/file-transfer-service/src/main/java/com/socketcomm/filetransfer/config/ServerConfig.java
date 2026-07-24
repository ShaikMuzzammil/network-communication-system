package com.socketcomm.filetransfer.config;

import java.util.Objects;

/**
 * Configuration class for Socket-based File Server settings.
 * 
 * Uses Builder pattern for flexible, immutable configuration.
 * 
 * <p>Example:</p>
 * <pre>
 * ServerConfig config = new ServerConfig.Builder()
 *     .port(8080)
 *     .fileName("document.pdf")
 *     .bufferSize(8192)
 *     .build();
 * </pre>
 * 
 * @author SocketComm Team
 */
public class ServerConfig {
    
    /** Default values */
    public static final int DEFAULT_PORT = 5000;
    public static final String DEFAULT_FILE_NAME = "sample.txt";
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    
    private final int port;
    private final String fileName;
    private final int bufferSize;
    
    /**
     * Private constructor - use Builder to create instances.
     */
    private ServerConfig(Builder builder) {
        this.port = builder.port;
        this.fileName = builder.fileName;
        this.bufferSize = builder.bufferSize;
    }
    
    // Getters
    public int getPort() { return port; }
    public String getFileName() { return fileName; }
    public int getBufferSize() { return bufferSize; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerConfig that = (ServerConfig) o;
        return port == that.port &&
               bufferSize == that.bufferSize &&
               Objects.equals(fileName, that.fileName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(port, fileName, bufferSize);
    }
    
    @Override
    public String toString() {
        return "ServerConfig{" +
               "port=" + port +
               ", fileName='" + fileName + '\'' +
               ", bufferSize=" + bufferSize +
               '}';
    }
    
    /**
     * Builder for creating ServerConfig instances.
     */
    public static class Builder {
        private int port = DEFAULT_PORT;
        private String fileName = DEFAULT_FILE_NAME;
        private int bufferSize = DEFAULT_BUFFER_SIZE;
        
        public Builder port(int port) {
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Port must be between 1 and 65535");
            }
            this.port = port;
            return this;
        }
        
        public Builder fileName(String fileName) {
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new IllegalArgumentException("File name cannot be null or empty");
            }
            this.fileName = fileName.trim();
            return this;
        }
        
        public Builder bufferSize(int bufferSize) {
            if (bufferSize < 1024) {
                throw new IllegalArgumentException("Buffer size must be at least 1024 bytes");
            }
            this.bufferSize = bufferSize;
            return this;
        }
        
        public ServerConfig build() {
            return new ServerConfig(this);
        }
    }
}
