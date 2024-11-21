/*Proposer class*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Proposer extends PaxosMember {
    //Initialisers
    //flag to denote consensus reached
    public static boolean consensusReached = false;
    //tracking recieved promises
    private final Set<String> promisesReceived = new HashSet<>();
    //tracking highest proposal ID
    private static int globalHighestProposalId = 0;
    //countdown latch to denote completion
    private static final CountDownLatch latch = new CountDownLatch(1);
    private int proposalId;
    //Track accepted proposals
    private final Map<String, Set<String>> acceptedProposals = new HashMap<>();
    private static final int TIMEOUT = 30000; // Timeout in milliseconds
    private int retryCount = 0; // Counter for retry attempts

    /**
     * Constructor to initialize a proposer.
     */
    public Proposer(String memberId, int port, int initialProposalId) throws IOException {
        super(memberId, port);
        this.proposalId = initialProposalId;
        synchronized (Proposer.class) {
            globalHighestProposalId = Math.max(globalHighestProposalId, initialProposalId);
        }
    }

    /**
     * Initiates the Paxos proposal process.
     * Broadcasts a PREPARE message to all acceptors and waits for PROMISE responses.
     * Retries if no majority is reached within the timeout period.
     */

    public void propose() {
        if (consensusReached ) {
            return; // Exit if consensus is already reached or max retries exceeded
        }

        retryCount++;
        //if retry count is more than 1, add proposalID to increment.
        if (retryCount > 1) {
            synchronized (Proposer.class) {
                proposalId = globalHighestProposalId + 1; // Increment based on highest known proposal ID
                globalHighestProposalId = proposalId;
            }
        }
        System.out.println(memberId + " proposing new proposalID " + proposalId + " (Attempt " + retryCount + ")");

        // Broadcast PREPARE message with proposal ID and proposer ID
        broadcastMessage("PREPARE " + proposalId + " " + memberId);

        // Start a timer for collecting promises
        new Thread(() -> {
            try {
                // Wait for timeout or latch signal
                if (!latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
                    if (!consensusReached && promisesReceived.size() < 5) {
                        System.out.println(memberId + " did not receive a majority within timeout, retrying proposal.");
                        propose(); // Retry the proposal
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Processes incoming messages and handles them based on type.
     * Handles PROMISE, ACCEPTED, and FINALISE messages.
     */
    @Override
    protected void handleMessage(Socket clientSocket) {
        if (consensusReached) return;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) { // Continuously read messages
                if (message.startsWith("PROMISE")) {
                    String[] parts = message.split(" ");
                    String senderId = parts[2];
                    //go to promise
                    handlePromise(senderId);
                }
                if (message.startsWith("PREPARE")) {
                    //go to prepare
                    TrackHighestProposalID(message);
                }
                if (message.startsWith("ACCEPTED")) {
                    String[] parts = message.split(" ");
                    String proposalId = parts[1];
                    String acceptorId = parts[2];

                    //add to the accepted proposal list to keep track
                    acceptedProposals.putIfAbsent(proposalId, new HashSet<>());
                    acceptedProposals.get(proposalId).add(acceptorId);

                    // Check if a majority of ACCEPTED messages has been reached
                    synchronized (this) {
                        if (!consensusReached && acceptedProposals.get(proposalId).size() >= 5) {
                            consensusReached = true; // Set the flag to true once consensus is reached
                            broadcastMessage("FINALISE " + proposalId + " " + memberId);

                            latch.countDown(); // Signal that consensus has been reached
                            // Print election winner as the last statement
                            System.out.println();
                            System.out.println("Election winner is " + memberId);
                            return; // Exit immediately
                        }
                    }
                }

                if (message.startsWith("FINALISE")) {
                    //go to finalise
                    handleFinalise(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a FINALISE message from another Proposer.
     * Marks consensus as reached and stops further processing.
     */
    private void handleFinalise(String message) {
        String[] parts = message.split(" ");
        if (parts.length >= 3) {
            String finalizedProposalId = parts[1];
            String finalizedProposerId = parts[2];

            // Set the consensusReached flag to true
            consensusReached = true;

            // Print the finalization message
            System.out.println("FINALISE received: Proposal ID " + finalizedProposalId + " by proposer " + finalizedProposerId);
        } else {
            System.err.println("Received improperly formatted FINALISE message: " + message);
        }
    }


    /**
     * Handles a PROMISE message from an acceptor.
     * Records the promise and checks if a majority has been reached.
     * If a majority is reached, broadcasts an ACCEPT message.
     */
    private synchronized void handlePromise(String acceptorId) {
        if (consensusReached || promisesReceived.contains(acceptorId)) {
            return; // Skip if consensus is reached or promise already recorded
        }
        //tracking recieved promises
        promisesReceived.add(acceptorId);
        System.out.println(memberId + " recorded PROMISE from " + acceptorId);

        // Check if a majority has been reached
        if (promisesReceived.size() >= 5) { // Majority for 9 acceptors
            System.out.println(memberId + " received majority PROMISES, broadcasting ACCEPT.");

            // Broadcast ACCEPT message with proposalId and proposerId
            broadcastMessage("ACCEPT " + proposalId + " " + memberId);
        }
    }

    /**
     * Handles a PREPARE message from another Proposer.
     * Updates the global highest proposal ID based on the received proposal.
     *
     * Track the global highest proposalID, upon retrying, will send a higher proposalID.
     */
    private void TrackHighestProposalID(String message) {
        String[] parts = message.split(" ");
        int receivedProposalId = Integer.parseInt(parts[1]);

        // Update the global highest proposal ID
        synchronized (Proposer.class) {
            globalHighestProposalId = Math.max(globalHighestProposalId, receivedProposalId);
        }
        System.out.println(memberId + " detected higher proposal ID: " + receivedProposalId);
    }

}
