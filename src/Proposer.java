import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Proposer extends PaxosMember {
    private final Set<String> promisesReceived = new HashSet<>();
    private final String proposalId;
    final Map<String, Set<String>> acceptedProposals = new HashMap<>();

    public Proposer(String memberId, int port, String proposalId) throws IOException {
        super(memberId, port);
        this.proposalId = proposalId;
    }

    public void propose() {
        System.out.println(memberId + " proposing " + proposalId);
        broadcastMessage("PREPARE " + proposalId);
    }

    @Override
    protected void handleMessage(Socket clientSocket) {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) { // Continuously read messages
                System.out.println(memberId + " received message: " + message);

                if (message.startsWith("PROMISE")) {
                    String[] parts = message.split(" ");
                    String senderId = parts[2];
                    handlePromise(senderId);
                }

                if (message.startsWith("ACCEPTED")) {


                    String[] parts = message.split(" ");
                    String proposalId = parts[1];
                    String acceptorId = parts[2];

                    acceptedProposals.putIfAbsent(proposalId, new HashSet<>());
                    acceptedProposals.get(proposalId).add(acceptorId);
                    System.out.println(memberId + " received ACCEPTED from " + acceptorId + " for proposal: " + proposalId);

                    // Check if a majority of ACCEPTED messages has been reached
                    if (acceptedProposals.get(proposalId).size() >= 4) {
                        System.out.println("Consensus reached on proposal " + proposalId + " - broadcasting FINALIZE.");
                        broadcastMessage("FINALISE " + proposalId);
                    }

                    System.out.println("Election winner is " + proposalId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void handlePromise(String senderId) {
        if (!promisesReceived.contains(senderId)) {
            promisesReceived.add(senderId);
            System.out.println(memberId + " recorded PROMISE from " + senderId);

            // Check if a majority has been reached
            if (promisesReceived.size() >= 4) {
                System.out.println(memberId + " received majority PROMISES, broadcasting ACCEPT.");
                broadcastMessage("ACCEPT " + proposalId);

            }
        }
    }
}
