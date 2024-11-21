/*Paxos Member class*/
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public abstract class PaxosMember {
    final String memberId;// Unique identifier for the Paxos member
    protected final ServerSocket serverSocket;// Server socket for listening to incoming connections
    protected final Map<String, Socket> Connections = new HashMap<>();// Map of peer IDs to their respective sockets

    /**
     * Constructor to initialize a Paxos member.
     * Sets up a server socket for listening to incoming connections.
     */
    public PaxosMember(String memberId, int port) throws IOException {
        this.memberId = memberId;
        this.serverSocket = new ServerSocket(port);
        System.out.println(memberId + " is online on port " + port);

        // Start a thread to accept incoming connections
        new Thread(this::acceptConnections).start();
    }

    /**
     * Starts the thread to accept incoming connections. Used in testing, called explicitly
     */
    public void start() {
        new Thread(this::acceptConnections).start();
    }

    /**
     * Continuously accepts incoming connections
     * Spawns thread to handle client connections
     */
    private void acceptConnections() {
        while (!serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleMessage(clientSocket)).start();
            } catch (IOException e) {
                if (serverSocket.isClosed()) {
                    return;
                } else {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }
    /**
     * Connects to a peer using the specified host and port.
     * Establishes a socket connection and stores it in the peerConnections map.
     * Used in testing, explicitly called
     */
    public void connectToOthers(String peerId, String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            socket.setKeepAlive(true);
            Connections.put(peerId, socket);
        } catch (IOException e) {
            System.err.println(memberId + " failed to connect to " + peerId + " on port " + port);
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Broadcasts a message to all connected peers.
     */
    public void broadcastMessage(String message) {
        for (Map.Entry<String, Socket> entry : Connections.entrySet()) {
            sendMessage(message, entry.getValue());
        }
    }

    /**
     * Sends a message to a specific peer.
     */
    public void sendMessage(String message, Socket socket) {
        try {
            socket.getOutputStream().write((message + "\n").getBytes());
            System.out.println(memberId + " sent message: " + message);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Closes all peer connections and the server socket.
     * Used in testing, explicitly called
     */
    public void close() throws IOException {

        // Close all peer connections
        for (Socket socket : Connections.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        // Clear the peerConnections map
        Connections.clear();

        // Close the server socket
        if (!serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    /**
     * Abstract method to handle incoming messages.
     * Implemented by subclasses to define specific message handling logic.
     */
    protected abstract void handleMessage(Socket clientSocket);
}
