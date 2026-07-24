package com.socketcomm.filetransfer.config;

import java.util.Objects;

/**
 * Configuration class for Socket-based File Client settings.
 * 
 * Uses Builder pattern for flexible, immutable configuration.
 * 
 * @author SocketComm Team
 */
public class ClientConfig {
    
    /** Default values */
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 5000;
    public static final String DEFAULT_OUTPUT_FILE = "received_file.bin";
    public static final int DEFAULT_CONNECT_TIMEOUT = 30; // seconds
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    
    private final String serverHost;
    private final int serverPort;
    private final String outputFile;
    private final int connectTimeout;
    private final int bufferSize;
    
    /**
     * Private constructor - use Builder.
     */
    private ClientConfig(Builder builder) {
        this.serverHost = builder.serverHost;
        this.serverPort = builder.serverPort;
        this.outputFile = builder.outputFile;
        this.connectTimeout = builder.connectTimeout;
        this.bufferSize = builder.bufferSize;
    }
    
    // Getters
    public String getServerHost() { return serverHost; }
    public int getServerPort() { return serverPort; }
    public String getOutputFile() { return outputFile; }
    public int getConnectTimeout() { return connectTimeout; }
    public int getBufferSize() { return bufferSize; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientConfig that = (ClientConfig) o;
        return serverPort == that.serverPort &&
               connectTimeout == that.connectTimeout &&
               bufferSize == that.bufferSize &&
               Objects.equals(serverHost, that.serverHost) &&
               Objects.equals(outputFile, that.outputFile);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(serverHost, serverPort, outputFile, connectTimeout, bufferSize);
    }
    
    @Override
    public String toString() {
        return "ClientConfig{" +
               "serverHost='" + serverHost + '\'' +
               ", serverPort=" + serverPort +
               ", outputFile='" + outputFile + '\'' +
               ", connectTimeout=" + connectTimeout +
               ", bufferSize=" + bufferSize +
               '}';
    }
    
    /**
     * Builder for creating ClientConfig instances.
     */
    public static class Builder {
        private String serverHost = DEFAULT_HOST;
        private int serverPort = DEFAULT_PORT;
        private String outputFile = DEFAULT_OUTPUT_FILE;
        private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private int bufferSize = DEFAULT_BUFFER_SIZE;
        
        public Builder serverHost(String serverHost) {
            if (serverHost == null || serverHost.trim().isEmpty()) {
                throw new IllegalArgumentException("Server host cannot be null or empty");
            }
            this.serverHost = serverHost.trim();
            return this;
        }
        
        public Builder serverPort(int serverPort) {
            if (serverPort < 1 || serverPort > 65535) {
                throw new IllegalArgumentException("Port must be between 1 and 65535");
            }
            this.serverPort = serverPort;
            return this;
        }
        
        public Builder outputFile(String outputFile) {
            if (outputFile == null || outputFile.trim().isEmpty()) {
                throw new IllegalArgumentException("Output file cannot be null or empty");
            }
            this.outputFile = outputFile.trim();
            return this;
        }
        
        public Builder connectTimeout(int seconds) {
            if (seconds < 1) {
                throw new IllegalArgumentException("Timeout must be at least 1 second");
            }
            this.connectTimeout = seconds;
            return this;
        }
        
        public Builder bufferSize(int bufferSize) {
            if (bufferSize < 1024) {
                throw new IllegalArgumentException("Buffer size must be at least 1024 bytes");
            }
            this.bufferSize = bufferSize;
            return this;
        }
        
        public ClientConfig build() {
            return new ClientConfig(this);
        }
    }
}
