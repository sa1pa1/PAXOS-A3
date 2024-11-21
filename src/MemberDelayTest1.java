
import java.io.IOException;
import java.net.Socket;

/**
 * Test 1a: One proposer, with delays
 **/

public class MemberDelayTest1 {
    public static void main(String[] args) {
        try {
            //TESTING COMMENCE
            System.out.println("#####################################################");
            System.out.println("TEST 2.A: ONE PROPOSER WITH SUGGESTED DELAY PROFILES");
            System.out.println("#####################################################");

            // Initialise proposers with unique and increasing proposal IDs
            Proposer proposerM1 = new Proposer("M1", 5001, 2) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                    DelayBehaviour.applyDelay("M1");
                    super.handleMessage(clientSocket);
                }
            };

            // Array to hold acceptors and their IDs
            Acceptor[] acceptors = new Acceptor[8];
            String[] acceptorIds = {"M2","M3", "M4", "M5", "M6", "M7", "M8", "M9"};
            int startingPort = 5002;

            // Create and start each acceptor, assigning them unique ports
            for (int i = 0; i < acceptors.length; i++) {
                final String acceptorId = acceptorIds[i]; // Capture the ID for use in lambda
                acceptors[i] = new Acceptor(acceptorId, startingPort + i) {
                    @Override
                    protected void handleMessage(Socket clientSocket) {
                        DelayBehaviour.applyDelay(acceptorId);
                        super.handleMessage(clientSocket);
                    }
                };
                new Thread(acceptors[i]::start).start();
            }

            // Start proposer
            new Thread(proposerM1::start).start();

            //********** ESTABLISHING CONNECTIONS ************//
            // Connect each acceptor to both proposers
            for (Acceptor acceptor : acceptors) {
                acceptor.connectToOthers("M1", "localhost", 5001); // Connect to Proposer M1
            }

            // Connect proposers to each acceptor
            for (int i = 0; i < acceptors.length; i++) {
                proposerM1.connectToOthers(acceptorIds[i], "localhost", startingPort + i);
            }
            //*************************************************//

            // Start proposing
            proposerM1.propose();

            // allow execution
            Thread.sleep(8000);

            //releasing ports
            System.out.println("RELEASING PORTS");
            proposerM1.close();
            for (Acceptor acceptor : acceptors) {
                acceptor.close();
            }

            //LOGGING COMPLETION
            System.out.println("##################################");
            System.out.println("TEST 2.A COMPLETED");
            System.out.println("##################################");

        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
