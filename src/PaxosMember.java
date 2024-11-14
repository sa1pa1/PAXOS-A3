import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class PaxosMember {
    final String memberId;
    private final ServerSocket serverSocket;
    protected final Map<String, Socket> peerConnections = new HashMap<>();
    private final Random random = new Random();

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
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleMessage(clientSocket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void connectToPeer(String peerId, String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            socket.setKeepAlive(true);  // Enable keep-alive to prevent timeouts
            peerConnections.put(peerId, socket);
            System.out.println(memberId + " connected to " + peerId + " on port " + port);
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
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
            System.out.println(memberId + " sent message: " + message + " to " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void handleMessage(Socket clientSocket);
}
