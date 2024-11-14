import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Acceptor extends PaxosMember {
    private String highestProposalId = null;
    private String proposerId; // Dynamic proposer ID

    public Acceptor(String memberId, int port, String proposerId) throws IOException {
        super(memberId, port);
        this.proposerId = proposerId; // Set proposer ID during instantiation
    }

    @Override
    protected void handleMessage(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {  // Continuously read messages

                if (message.startsWith("PREPARE")) {
                    String[] parts = message.split(" ");
                    if (parts.length >= 2) {  // Check if the message is properly formatted
                        String proposalId = parts[1];
                        if (highestProposalId == null || proposalId.compareTo(highestProposalId) > 0) {
                            highestProposalId = proposalId;
                            System.out.println(memberId + " sending PROMISE for proposal: " + proposalId);
                            sendMessage("PROMISE " + proposalId + " " + memberId, peerConnections.get(proposerId)); // Send to the specified proposer
                        }
                    } else {
                        System.err.println(memberId + " received improperly formatted PREPARE message.");
                    }
                }

                if (message.startsWith("ACCEPT")) {
                    System.out.println(memberId + " processing ACCEPT message.");
                    String[] parts = message.split(" ");
                    if (parts.length >= 2) {  // Check if the message is properly formatted
                        String proposalId = parts[1];
                        // Send the ACCEPTED message to proposer
                        Socket proposerSocket = peerConnections.get(proposerId);
                        if (proposerSocket != null) {
                            sendMessage("ACCEPTED " + proposalId + " " + memberId, proposerSocket);
                            System.out.println(memberId + " successfully sent ACCEPTED to proposer for proposal: " + proposalId);
                        } else {
                            System.err.println(memberId + " could not find connection to proposer for ACCEPTED message.");
                        }
                    } else {
                        System.err.println(memberId + " received improperly formatted ACCEPT message.");
                    }
                }

                // Handle FINALISE message
                if (message.startsWith("FINALISE")) {
                    String[] parts = message.split(" ");
                    if (parts.length >= 2) {  // Check if the message is properly formatted
                        String finalizedProposalId = parts[1];
                        System.out.println(memberId + " acknowledge election winner is " + finalizedProposalId);
                    } else {
                        System.err.println(memberId + " received improperly formatted FINALIZE message.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to update the proposer ID if it changes
    public void setProposerId(String proposerId) {
        this.proposerId = proposerId;
    }
}
