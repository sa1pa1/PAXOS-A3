import java.io.IOException;

public class OneProposer {
    public static void main(String[] args) {
        try {
            String proposerId = "M2"; // First proposer

            // Initialize proposers
            Proposer proposerM2 = new Proposer(proposerId, 5001, proposerId);

            // Array to hold acceptors and their IDs
            Acceptor[] acceptors = new Acceptor[8];
            String[] acceptorIds = {"M1","M3", "M4", "M5", "M6", "M7", "M8", "M9"};
            int startingPort = 5002;

            // Create and start each acceptor, assigning them unique ports
            for (int i = 0; i < acceptors.length; i++) {
                acceptors[i] = new Acceptor(acceptorIds[i], startingPort + i, proposerId);
                new Thread(acceptors[i]::start).start();
            }

            // Start proposers
            new Thread(proposerM2::start).start();


            // Connect each acceptor to both proposers
            for (Acceptor acceptor : acceptors) {
                acceptor.connectToPeer(proposerId, "localhost", 5001);
            }

            // Connect proposers to each acceptor
            for (int i = 0; i < acceptors.length; i++) {
                proposerM2.connectToPeer(acceptorIds[i], "localhost", startingPort + i);
            }

            // Start M2's proposal immediately
            proposerM2.propose();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
