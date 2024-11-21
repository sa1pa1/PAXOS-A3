

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Acceptor extends PaxosMember {
    private String highestProposalId = null;

    public Acceptor(String memberId, int port) throws IOException {
        super(memberId, port);
    }

    @Override
    protected void handleMessage(Socket clientSocket) {
        if (Proposer.consensusReached) return; // Stop processing if consensus is reached

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("PREPARE")) {
                    handlePrepare(message);
                } else if (message.startsWith("ACCEPT")) {
                    handleAccept(message);
                } else if (message.startsWith("FINALISE")) {
                    handleFinalise(message);
                } else {
                    System.err.println(memberId + " received unknown message type: " + message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePrepare(String message) {
        String[] parts = message.split(" ");
        if (parts.length >= 3) {
            String proposalId = parts[1];
            String proposerIdFromMessage = parts[2];

            if (highestProposalId == null || proposalId.compareTo(highestProposalId) > 0) {
                highestProposalId = proposalId;
                System.out.println(memberId + " sending PROMISE for proposal: " + proposalId + " to proposer: " + proposerIdFromMessage);

                Socket proposerSocket = peerConnections.get(proposerIdFromMessage);
                if (proposerSocket != null) {
                    sendMessage("PROMISE " + proposalId + " " + memberId, proposerSocket);
                } else {
                    System.err.println(memberId + " could not find connection to proposer: " + proposerIdFromMessage);
                }
            }
        } else {
            System.err.println(memberId + " received improperly formatted PREPARE message: " + message);
        }
    }

    private void handleAccept(String message) {
        String[] parts = message.split(" ");
        if (parts.length >= 3) {
            String proposalId = parts[1];
            String proposerIdFromMessage = parts[2];

            System.out.println(memberId + " received ACCEPT for proposal: " + proposalId + ", sending ACCEPTED to proposer: " + proposerIdFromMessage);

            Socket proposerSocket = peerConnections.get(proposerIdFromMessage);
            if (proposerSocket != null) {
                sendMessage("ACCEPTED " + proposalId + " " + memberId, proposerSocket);
            } else {
                System.err.println(memberId + " could not find connection to proposer: " + proposerIdFromMessage);
            }
        } else {
            System.err.println(memberId + " received improperly formatted ACCEPT message: " + message);
        }
    }

    private void handleFinalise(String message) {
        String[] parts = message.split(" ");
        if (parts.length >= 2) {
            String finalizedProposalId = parts[1];
            System.out.println(memberId + " acknowledged election winner is: " + finalizedProposalId);
            Proposer.consensusReached = true; // Stop further processing
        } else {
            System.err.println(memberId + " received improperly formatted FINALISE message: " + message);
        }
    }

}
