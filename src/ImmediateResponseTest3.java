import java.io.IOException;
/**
 * Test 1.c: Concurrent: Three proposers
 *
 **/
public class ImmediateResponseTest3 {
    public static void main(String[] args) {
        try {
            //TESTING COMMENCE
            System.out.println("#######################################################");
            System.out.println("TEST 1.C: THREE CONCURRENT PROPOSERS, IMMEDIATE RESPONSE");
            System.out.println("#######################################################");

            // Initialise three proposers with unique and increasing proposal IDs
            Proposer proposerM1 = new Proposer("M1", 5001, 1);
            Proposer proposerM2 = new Proposer("M2", 5002, 2);
            Proposer proposerM3 = new Proposer("M3", 5003, 3);

            // Array to hold acceptors and their IDs
            Acceptor[] acceptors = new Acceptor[6];
            String[] acceptorIds = { "M4", "M5", "M6", "M7", "M8", "M9" };
            int startingPort = 5004;

            // Create and start each acceptor, assigning them unique ports
            for (int i = 0; i < acceptors.length; i++) {
                acceptors[i] = new Acceptor(acceptorIds[i], startingPort + i);
                new Thread(acceptors[i]::start).start();
            }

            //********** ESTABLISHING CONNECTIONS ************//
            // Connect each acceptor to all proposers
            for (Acceptor acceptor : acceptors) {
                acceptor.connectToOthers("M1", "localhost", 5001);
                acceptor.connectToOthers("M2", "localhost", 5002);
                acceptor.connectToOthers("M3", "localhost", 5003);
            }

            // Connect all proposers to each acceptor
            for (int i = 0; i < acceptors.length; i++) {
                proposerM1.connectToOthers(acceptorIds[i], "localhost", startingPort + i);
                proposerM2.connectToOthers(acceptorIds[i], "localhost", startingPort + i);
                proposerM3.connectToOthers(acceptorIds[i], "localhost", startingPort + i);
            }
            //*************************************************//

            // Create threads to propose concurrently
            Thread proposerThread1 = new Thread(proposerM1::propose);

            Thread proposerThread2 = new Thread(proposerM2::propose);

            Thread proposerThread3 = new Thread(proposerM3::propose);

            // Start all proposer threads concurrently
            proposerThread1.start();
            proposerThread2.start();
            proposerThread3.start();

            // allow execution
            Thread.sleep(3000);

            // Wait for all threads to finish
            proposerThread1.join();
            proposerThread2.join();
            proposerThread3.join();

            //releasing ports
            System.out.println("RELEASING PORTS");
            proposerM1.close();
            proposerM2.close();
            proposerM3.close();
            // Close all acceptors
            for (Acceptor acceptor : acceptors) {
                acceptor.close();
            }

            //LOGGING COMPLETION
            System.out.println("##################################");
            System.out.println("TEST 1.C COMPLETED");
            System.out.println("##################################");


        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}