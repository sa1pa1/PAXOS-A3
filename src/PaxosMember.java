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
                new Thread(() -> handleMessageWithBehavior(clientSocket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessageWithBehavior(Socket clientSocket) {
        try {
            // Apply behavior based on member ID
            if ("M1".equals(memberId)) {
                // M1: Immediate response, no delay needed
            } else if ("M2".equals(memberId)) {
                // M2: Simulate poor connectivity with occasional message drop
                if (shouldSkipResponse()) {
                    System.out.println(memberId + " skipped a message due to poor connectivity.");
                    return; // Skip processing this message
                }
                simulatePoorConnectivity(); // Delayed response
            } else if ("M3".equals(memberId)) {
                // M3: High chance of dropping messages due to intermittent connectivity
                if (shouldDropMessage()) {
                    System.out.println(memberId + " dropped a message due to intermittent connectivity.");
                    return; // Exit without processing the message
                }
            } else if (memberId.startsWith("M") && Integer.parseInt(memberId.substring(1)) >= 4) {
                // M4-M9: Simulate busy schedule with varied random delay
                simulateBusySchedule();
            }

            // Pass client socket to the specific message handler
            handleMessage(clientSocket);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void connectToPeer(String peerId, String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            socket.setKeepAlive(true);  // Enable keep-alive to prevent timeouts
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
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
            System.out.println(memberId + " sent message: " + message + " to " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void simulatePoorConnectivity() throws InterruptedException {
        int delay = 3000 + random.nextInt(5000); // Delay between 3 to 8 seconds
        System.out.println(memberId + " delaying response due to poor connectivity...");
        Thread.sleep(delay);
    }

    private boolean shouldSkipResponse() {
        // 30% chance that M2 will skip responding entirely
        return random.nextDouble() < 0.3;
    }

    private boolean shouldDropMessage() {
        // 50% chance that M3 will drop the message
        return random.nextDouble() < 0.5;
    }

    private void simulateBusySchedule() throws InterruptedException {
        int delay = 1000 + random.nextInt(2000); // Delay between 1 to 3 seconds
        System.out.println(memberId + " delaying response due to busy schedule...");
        Thread.sleep(delay);
    }
    protected abstract void handleMessage(Socket clientSocket);


}
