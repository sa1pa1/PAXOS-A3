import java.io.IOException;

public class M2Offline {
    public static void main(String[] args) {
        try {
            // Initialize proposers
            Proposer proposerM1 = new Proposer("M1", 5001, 3);
            Proposer proposerM2 = new Proposer("M2", 5002, 5);

            // Array to hold acceptors and their IDs
            Acceptor[] acceptors = new Acceptor[8];
            String[] acceptorIds = {"M3", "M4", "M5", "M6", "M7", "M8", "M9", "M10"};
            int startingPort = 5003;

            // Create and start each acceptor, assigning them unique ports
            for (int i = 0; i < acceptors.length; i++) {
                acceptors[i] = new Acceptor(acceptorIds[i], startingPort + i);
                new Thread(acceptors[i]::start).start();
            }

            // Delay to allow connections to establish
            Thread.sleep(5000); // Wait 5 seconds

            // Start proposers
            new Thread(proposerM1::start).start();
            new Thread(proposerM2::start).start();

            // Connect each acceptor to both proposers
            for (Acceptor acceptor : acceptors) {
                acceptor.connectToPeer("M1", "localhost", 5001);
                acceptor.connectToPeer("M2", "localhost", 5002);
            }

            // Connect proposers to each acceptor
            for (int i = 0; i < acceptors.length; i++) {
                proposerM1.connectToPeer(acceptorIds[i], "localhost", startingPort + i);
                proposerM2.connectToPeer(acceptorIds[i], "localhost", startingPort + i);
            }

            // Simulate offline scenario: M2 goes offline after proposing
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Wait for M2 to propose
                    System.out.println("M2 going offline...");
                    System.exit(0); // Simulate M2 disconnect
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            // Start proposals
            proposerM1.propose();
            proposerM2.propose();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
