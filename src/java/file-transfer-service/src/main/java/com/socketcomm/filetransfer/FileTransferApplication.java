package com.socketcomm.filetransfer;

/**
 * SocketComm File Transfer Service - Main Application Entry Point
 * 
 * This class serves as the main entry point for the Socket-based File Transfer Service,
 * providing a unified interface to start either server or client mode.
 * 
 * <p>The application supports:</p>
 * <ul>
 *   <li>Server mode: Host files for download by clients via TCP Sockets</li>
 *   <li>Client mode: Connect to servers and download files via TCP Sockets</li>
 *   <li>Configurable buffer sizes and timeouts</li>
 *   <li>Progress tracking and error handling</li>
 * </ul>
 * 
 * @author SocketComm Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class FileTransferApplication {
    
    /** Application version constant */
    public static final String VERSION = "1.0.0";
    
    /** Default server port */
    public static final int DEFAULT_PORT = 5000;
    
    /** Default buffer size for file transfers (4KB) */
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    
    /** Default host address */
    public static final String DEFAULT_HOST = "127.0.0.1";
    
    /**
     * Main entry point for the File Transfer application.
     * 
     * <p>Usage examples:</p>
     * <pre>
     * # Start as server on default port
     * java -jar file-transfer-service.jar server
     * 
     * # Start as client connecting to server
     * java -jar file-transfer-service.jar client --host 192.168.1.100
     * 
     * # Custom port and file
     * java -jar file-transfer-service.jar server --port 8080 --file document.pdf
     * </pre>
     * 
     * @param args Command line arguments (server/client mode, options)
     */
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║      SocketComm File Transfer Service       ║");
        System.out.println("║              Version " + VERSION + "                  ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
        
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        String mode = args[0].toLowerCase();
        
        switch (mode) {
            case "server":
                startServerMode(args);
                break;
                
            case "client":
                startClientMode(args);
                break;
                
            case "--help":
            case "-h":
            case "help":
                printUsage();
                break;
                
            case "--version":
            case "-v":
            case "version":
                System.out.println("SocketComm File Transfer Service v" + VERSION);
                break;
                
            default:
                System.err.println("Unknown mode: " + mode);
                printUsage();
                System.exit(1);
        }
    }
    
    /**
     * Start the application in server mode.
     * 
     * @param args Command line arguments including server configuration
     */
    private static void startServerMode(String[] args) {
        int port = DEFAULT_PORT;
        String fileName = "sample.txt";
        
        // Parse arguments
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--port":
                case "-p":
                    if (i + 1 < args.length) {
                        port = Integer.parseInt(args[++i]);
                    }
                    break;
                    
                case "--file":
                case "-f":
                    if (i + 1 < args.length) {
                        fileName = args[++i];
                    }
                    break;
                    
                default:
                    System.err.println("Unknown option: " + args[i]);
                    return;
            }
        }
        
        System.out.println("Starting in SERVER mode...");
        System.out.println("  Port: " + port);
        System.out.println("  File: " + fileName);
        System.out.println();
        
        // Create and start server
        com.socketcomm.filetransfer.server.FileServer server = 
            new com.socketcomm.filetransfer.server.FileServer(port, fileName);
        server.start();
    }
    
    /**
     * Start the application in client mode.
     * 
     * @param args Command line arguments including client configuration
     */
    private static void startClientMode(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        String outputFile = "received_file.bin";
        
        // Parse arguments
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--host":
                case "-H":
                    if (i + 1 < args.length) {
                        host = args[++i];
                    }
                    break;
                    
                case "--port":
                case "-p":
                    if (i + 1 < args.length) {
                        port = Integer.parseInt(args[++i]);
                    }
                    break;
                    
                case "--output":
                case "-o":
                    if (i + 1 < args.length) {
                        outputFile = args[++i];
                    }
                    break;
                    
                default:
                    System.err.println("Unknown option: " + args[i]);
                    return;
            }
        }
        
        System.out.println("Starting in CLIENT mode...");
        System.out.println("  Server: " + host + ":" + port);
        System.out.println("  Output: " + outputFile);
        System.out.println();
        
        // Create and start client
        com.socketcomm.filetransfer.client.FileClient client = 
            new com.socketcomm.filetransfer.client.FileClient(host, port, outputFile);
        client.connectAndReceive();
    }
    
    /**
     * Print usage information to standard output.
     */
    private static void printUsage() {
        System.out.println("USAGE:");
        System.out.println("  java -jar file-transfer-service.jar <MODE> [OPTIONS]");
        System.out.println();
        System.out.println("MODES:");
        System.out.println("  server    Start in server mode (host files)");
        System.out.println("  client    Start in client mode (download files)");
        System.out.println();
        System.out.println("SERVER OPTIONS:");
        System.out.println("  -p, --port <port>    Port to listen on (default: " + DEFAULT_PORT + ")");
        System.out.println("  -f, --file <file>    File to serve (default: sample.txt)");
        System.out.println();
        System.out.println("CLIENT OPTIONS:");
        System.out.println("  -H, --host <host>    Server address (default: " + DEFAULT_HOST + ")");
        System.out.println("  -p, --port <port>    Server port (default: " + DEFAULT_PORT + ")");
        System.out.println("  -o, --output <file>  Output filename (default: received_file.bin)");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  java -jar file-transfer-service.jar server --port 8080");
        System.out.println("  java -jar file-transfer-service.jar client --host 192.168.1.100");
        System.out.println();
        System.out.println("For more information, visit: https://docs.socketcomm.dev");
    }
}
