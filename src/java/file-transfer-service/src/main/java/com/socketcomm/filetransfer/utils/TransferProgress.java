package com.socketcomm.filetransfer.utils;

/**
 * Utility class for tracking socket file transfer progress.
 * 
 * Provides functionality for:
 * - Tracking bytes transferred vs total size over TCP sockets
 * - Calculating percentage complete
 * - Determining when to log progress updates
 * - Estimating transfer speed (future enhancement)
 * 
 * @author SocketComm Team
 */
public class TransferProgress {
    
    /** Total size of transfer (-1 if unknown) */
    private final long totalBytes;
    
    /** Bytes transferred so far */
    private long transferredBytes;
    
    /** Time when transfer started */
    private final long startTimeMillis;
    
    /** Last time progress was logged */
    private long lastLogTimeMillis;
    
    /** Minimum interval between log updates (ms) */
    private static final long LOG_INTERVAL_MS = 1000;
    
    /**
     * Creates a new progress tracker.
     * 
     * @param totalBytes Total bytes to transfer (-1 if unknown)
     */
    public TransferProgress(long totalBytes) {
        this.totalBytes = totalBytes;
        this.transferredBytes = 0;
        this.startTimeMillis = System.currentTimeMillis();
        this.lastLogTimeMillis = 0;
    }
    
    /**
     * Updates the number of bytes transferred.
     * 
     * @param bytesTransferred New total of bytes transferred
     */
    public synchronized void update(long bytesTransferred) {
        this.transferredBytes = bytesTransferred;
    }
    
    /**
     * Gets the current percentage complete.
     * 
     * @return Percentage between 0.0 and 100.0, or -1 if total is unknown
     */
    public double getPercentComplete() {
        if (totalBytes <= 0) {
            return -1.0;
        }
        return Math.min(100.0, (transferredBytes * 100.0) / totalBytes);
    }
    
    /**
     * Checks if it's time to log a progress update.
     * 
     * @return true if enough time has passed since last log
     */
    public synchronized boolean shouldLog() {
        long now = System.currentTimeMillis();
        if (now - lastLogTimeMillis >= LOG_INTERVAL_MS) {
            lastLogTimeMillis = now;
            return true;
        }
        return false;
    }
    
    /**
     * Gets the number of bytes transferred so far.
     * 
     * @return Transferred byte count
     */
    public long getTransferredBytes() {
        return transferredBytes;
    }
    
    /**
     * Gets the total bytes expected.
     * 
     * @return Total bytes, or -1 if unknown
     */
    public long getTotalBytes() {
        return totalBytes;
    }
    
    /**
     * Checks if transfer is complete.
     * 
     * @return true if all bytes have been transferred
     */
    public boolean isComplete() {
        if (totalBytes <= 0) {
            return false;
        }
        return transferredBytes >= totalBytes;
    }
    
    /**
     * Gets elapsed time since start in milliseconds.
     * 
     * @return Elapsed milliseconds
     */
    public long getElapsedTimeMs() {
        return System.currentTimeMillis() - startTimeMillis;
    }
    
    /**
     * Calculates approximate transfer speed in bytes per second.
     * 
     * @return Speed in bytes/second, or 0 if not enough data
     */
    public double getSpeedBytesPerSecond() {
        long elapsed = getElapsedTimeMs();
        if (elapsed < 1000) {
            return 0.0;
        }
        return (transferredBytes * 1000.0) / elapsed;
    }
    
    @Override
    public String toString() {
        if (totalBytes > 0) {
            return String.format("Progress: %.1f%% (%d/%d bytes)",
                getPercentComplete(), transferredBytes, totalBytes);
        } else {
            return String.format("Progress: %d bytes transferred", transferredBytes);
        }
    }
}
