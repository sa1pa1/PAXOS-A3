import java.io.IOException;
//handling changes to new proposer when its proposed after, using M1 as the latter proposal for no interruptions.
public class HigherProposalID {
    public static void main(String[] args) {
        try {
            // Initialize proposers with unique and increasing proposal IDs
            Proposer proposerM1 = new Proposer("M1", 5001, 2);
            Proposer proposerM2 = new Proposer("M2", 5002, 1);

            // Array to hold acceptors and their IDs
            Acceptor[] acceptors = new Acceptor[7];
            String[] acceptorIds = {"M3", "M4", "M5", "M6", "M7", "M8", "M9"};
            int startingPort = 5003;

            // Create and start each acceptor, assigning them unique ports
            for (int i = 0; i < acceptors.length; i++) {
                acceptors[i] = new Acceptor(acceptorIds[i], startingPort + i); // No specific proposer ID initially
                new Thread(acceptors[i]::start).start();
            }

            // Delay to allow connections to establish
            Thread.sleep(5000); // Wait 5 seconds before starting proposers

            // Start proposers
            new Thread(proposerM1::start).start();
            new Thread(proposerM2::start).start();

            // Connect each acceptor to both proposers
            for (Acceptor acceptor : acceptors) {
                acceptor.connectToPeer("M1", "localhost", 5001); // Connect to Proposer M1
                acceptor.connectToPeer("M2", "localhost", 5002); // Connect to Proposer M2
            }

            // Connect proposers to each acceptor
            for (int i = 0; i < acceptors.length; i++) {
                proposerM1.connectToPeer(acceptorIds[i], "localhost", startingPort + i);
                proposerM2.connectToPeer(acceptorIds[i], "localhost", startingPort + i);
            }

            // Start proposing
            proposerM2.propose();
            Thread.sleep(4000);
            proposerM1.propose();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
