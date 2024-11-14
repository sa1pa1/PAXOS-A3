import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Acceptor extends PaxosMember {
    private String highestProposalId = null;
    private String proposerId = null;

    public Acceptor(String memberId, int port) throws IOException {
        super(memberId, port);
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
                            sendMessage("PROMISE " + proposalId + " " + memberId, peerConnections.get("M1")); // Send to M1 only
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
                        System.out.println(memberId + " received ACCEPT for proposal: " + proposalId + ", sending ACCEPTED to proposer");

                        // Send the ACCEPTED message to proposer
                        Socket learnerSocket = peerConnections.get("M1");
                        if (learnerSocket != null) {
                            sendMessage("ACCEPTED " + proposalId + " " + memberId, learnerSocket);
                            System.out.println(memberId + " successfully sent ACCEPTED to proposer for proposal: " + proposalId);
                        } else {
                            System.err.println(memberId + " could not find connection to proposer for ACCEPTED message.");
                        }
                    } else {
                        System.err.println(memberId + " received improperly formatted ACCEPT message.");
                    }
                }

                // Handle FINALIZE message
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
}
