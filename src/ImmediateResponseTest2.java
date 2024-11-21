import java.io.IOException;

public class ImmediateResponseTest2 {
    public static void main(String[] args) {
        try {
            // Initialize three proposers with unique and increasing proposal IDs
            Proposer proposerM1 = new Proposer("M1", 5001, 2);
            Proposer proposerM2 = new Proposer("M2", 5002, 4);

            // Array to hold acceptors and their IDs
            Acceptor[] acceptors = new Acceptor[7];
            String[] acceptorIds = { "M3","M4", "M5", "M6", "M7", "M8", "M9" };
            int startingPort = 5004;

            // Create and start each acceptor, assigning them unique ports
            for (int i = 0; i < acceptors.length; i++) {
                acceptors[i] = new Acceptor(acceptorIds[i], startingPort + i);
                new Thread(acceptors[i]::start).start();
            }

            // Connect each acceptor to all proposers
            for (Acceptor acceptor : acceptors) {
                acceptor.connectToPeer("M1", "localhost", 5001);
                acceptor.connectToPeer("M2", "localhost", 5002);

            }

            // Connect all proposers to each acceptor
            for (int i = 0; i < acceptors.length; i++) {
                proposerM1.connectToPeer(acceptorIds[i], "localhost", startingPort + i);
                proposerM2.connectToPeer(acceptorIds[i], "localhost", startingPort + i);

            }

            // Create threads to propose concurrently
            Thread proposerThread1 = new Thread(() -> {
                proposerM1.propose();
            });

            Thread proposerThread2 = new Thread(() -> {
                proposerM2.propose();
            });



            // Start all proposer threads concurrently
            proposerThread1.start();
            proposerThread2.start();

            Thread.sleep(3000); // allow execution

            // Wait for all threads to finish
            proposerThread1.join();
            proposerThread2.join();

            proposerM1.close();
            proposerM2.close();
            // Close all acceptors
            for (Acceptor acceptor : acceptors) {
                acceptor.close();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}