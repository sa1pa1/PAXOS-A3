/*Acceptor class*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Acceptor extends PaxosMember {
    private String highestProposalId = null;
    /**
     * Constructor to initialize an Acceptor.
     */
    public Acceptor(String memberId, int port) throws IOException {
        super(memberId, port);
    }

    /**
     * Handles incoming messages from Proposers.
     * Stops processing if consensus is already reached.
     */
    @Override
    protected void handleMessage(Socket clientSocket) {
        if (Proposer.consensusReached) return; // Stop processing if consensus is reached

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("PREPARE")) {
                    //Go to handle prepare
                    handlePrepare(message);
                } else if (message.startsWith("ACCEPT")) {
                    //Go to handle accept
                    handleAccept(message);
                } else if (message.startsWith("FINALISE")) {
                    //Go to handle finalise
                    handleFinalise(message);
                } else {
                    //error handling
                    System.err.println(memberId + ": Unknown message type: " + message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a PREPARE message from a Proposer.
     * If the proposal ID is higher than the currently highest proposal ID,
     * it sends a PROMISE message to the Proposer and updates the highest proposal ID.
     */

    private void handlePrepare(String message) {
        String[] parts = message.split(" ");
        if (parts.length >= 3) {
            //Get proposalID
            String proposalId = parts[1];
            //Get proposerID
            String proposerId = parts[2];

            //checking and sending promise to highest proposal
            if (highestProposalId == null || proposalId.compareTo(highestProposalId) > 0) {
                highestProposalId = proposalId;
                System.out.println(memberId + " sending PROMISE for proposal: " + proposalId + " to proposer: " + proposerId);

                Socket proposerSocket = Connections.get(proposerId);
                if (proposerSocket != null) {
                    sendMessage("PROMISE " + proposalId + " " + memberId, proposerSocket);
                } else {
                    //error logging
                    System.err.println(memberId + " cannot connect to " + proposerId);
                }
            }
        } else {
            //error logging
            System.err.println(memberId + " : Improper PREPARE message: " + message);
        }
    }

    /**
     * Handles an ACCEPT message from a Proposer.
     * Responds by sending an ACCEPTED message to the Proposer.
     */
    private void handleAccept(String message) {
        String[] parts = message.split(" ");
        if (parts.length >= 3) {
            String proposalId = parts[1];
            String proposerIdFromMessage = parts[2];

            System.out.println(memberId + " received ACCEPT for proposal: " + proposalId + ", sending ACCEPTED to proposer: " + proposerIdFromMessage);

            Socket proposerSocket = Connections.get(proposerIdFromMessage);
            if (proposerSocket != null) {
                sendMessage("ACCEPTED " + proposalId + " " + memberId, proposerSocket);
            } else {
                System.err.println(memberId + " cannot connect to " + proposerIdFromMessage);
            }
        } else {
            System.err.println(memberId + " : Improper PREPARE message: " + message);
        }
    }

    /**
     * Handles a FINALISE message from a Proposer.
     * Acknowledges the election winner and stops further processing.
     */
    private void handleFinalise(String message) {
        String[] parts = message.split(" ");
        if (parts.length >= 2) {
            String finalizedProposalId = parts[1];
            System.out.println(memberId + " acknowledged election winner is: " + finalizedProposalId);
            Proposer.consensusReached = true; // Stop further processing
        } else {
            System.err.println(memberId + " : Improper PREPARE message: " + message);
        }
    }

}
