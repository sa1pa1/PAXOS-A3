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
    private final int proposalId; // Change from String to int
    final Map<String, Set<String>> acceptedProposals = new HashMap<>();
    private static final int TIMEOUT = 30000; // Timeout in milliseconds
    private static final int MAX_RETRIES = 2; // Maximum number of retries
    private int retryCount = 0; // Counter for retry attempts
    private boolean consensusReached = false; // Flag to check if consensus has already been reached

    public Proposer(String memberId, int port, int proposalId) throws IOException {
        super(memberId, port);
        this.proposalId = proposalId;
    }

    public void propose() {
        if (consensusReached || retryCount >= MAX_RETRIES) {
            if (retryCount >= MAX_RETRIES) {
                System.out.println(memberId + " reached max retry attempts without consensus.");
            }
            return; // Exit if consensus is already reached or max retries exceeded
        }

        retryCount++;
        System.out.println(memberId + " proposing " + proposalId + " (Attempt " + retryCount + ")");

        // Broadcast PREPARE message with proposal ID and proposer ID
        broadcastMessage("PREPARE " + proposalId + " " + memberId);

        // Start a timer for collecting promises
        new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT);
                if (!consensusReached && promisesReceived.size() < 5) { // Majority check for 8 acceptors
                    System.out.println(memberId + " did not receive a majority within timeout, retrying proposal.");
                    propose(); // Retry the proposal
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void handleMessage(Socket clientSocket) {
        if (consensusReached) return;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) { // Continuously read messages

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


                    // Check if a majority of ACCEPTED messages has been reached
                    synchronized (this) {  // Ensure FINALISE is broadcast only once
                        if (!consensusReached && acceptedProposals.get(proposalId).size() >= 5) { // Majority for 9 acceptors including proposer
                            consensusReached = true; // Set the flag to true once consensus is reached
                            broadcastMessage("FINALISE " + proposalId + " " + memberId);


                            // Print election winner as the last statement
                            System.out.println();
                            System.out.println("Election winner is " + memberId );

                        }

                    }
                }

                if (message.startsWith("FINALISE")) {
                    handleFinalise(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFinalise(String message) {
        String[] parts = message.split(" ");
        if (parts.length >= 3) {
            String finalizedProposalId = parts[1];
            String finalizedProposerId = parts[2];

            // Set the consensusReached flag to true
            consensusReached = true;

            // Print the finalization message
            System.out.println("FINALISE received: Proposal ID " + finalizedProposalId + " by proposer " + finalizedProposerId);
            System.out.println("Stopping further processing for proposer: " + memberId);
        } else {
            System.err.println("Received improperly formatted FINALISE message: " + message);
        }
    }



    private synchronized void handlePromise(String senderId) {
        if (consensusReached || promisesReceived.contains(senderId)) {
            return; // Skip if consensus is reached or promise already recorded
        }

        promisesReceived.add(senderId);
        System.out.println(memberId + " recorded PROMISE from " + senderId);

        // Check if a majority has been reached
        if (promisesReceived.size() >= 4) { // Majority for 8 acceptors
            System.out.println(memberId + " received majority PROMISES, broadcasting ACCEPT.");

            // Broadcast ACCEPT message with proposalId and proposerId
            broadcastMessage("ACCEPT " + proposalId + " " + memberId);
        }
    }

}
