import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public abstract class PaxosMember {
    final String memberId;
    protected final ServerSocket serverSocket;
    protected final Map<String, Socket> peerConnections = new HashMap<>();

    // Constructor
    public PaxosMember(String memberId, int port) throws IOException {
        this.memberId = memberId;
        this.serverSocket = new ServerSocket(port);
        System.out.println(memberId + " is online on port " + port);

        // Start a thread to accept incoming connections
        new Thread(this::acceptConnections).start();
    }

    public void start() {
        new Thread(this::acceptConnections).start();
    }

    private void acceptConnections() {
        while (!serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleMessage(clientSocket)).start();
            } catch (IOException e) {
                if (serverSocket.isClosed()) {
                } else {
                    e.printStackTrace();
                }
            }
        }
    }

    public void connectToPeer(String peerId, String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            socket.setKeepAlive(true);
            peerConnections.put(peerId, socket);
        } catch (IOException e) {
            System.err.println(memberId + " failed to connect to " + peerId + " on port " + port);
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String message) {
        for (Map.Entry<String, Socket> entry : peerConnections.entrySet()) {
            sendMessage(message, entry.getValue());
        }
    }

    public void sendMessage(String message, Socket socket) {
        try {
            socket.getOutputStream().write((message + "\n").getBytes());
            System.out.println(memberId + " sent message: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {

        // Close all peer connections
        for (Socket socket : peerConnections.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Clear the peerConnections map
        peerConnections.clear();

        // Close the server socket
        if (!serverSocket.isClosed()) {
            serverSocket.close();
        }

    }

    // Abstract method to handle incoming messages
    protected abstract void handleMessage(Socket clientSocket);
}
