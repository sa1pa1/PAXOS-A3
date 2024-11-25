

import java.io.IOException;
import java.net.Socket;

/**
 * Test 2.c: Concurrent: Two proposers (M2 and M3)
 * In this test, M2 and M3 are proposers. This test not only studies the behaviour of paxos protocol favouring higher proposal ID but also the effects of delays on which member will win.
 * M2 has a higher proposal ID than M3 in this test. However, due to its delay M3 can win.
 *
 **/

public class MemberDelayTest3 {
    public static void main(String[] args) {
        try {
            //TESTING COMMENCE
            System.out.println("##############################################################");
            System.out.println("TEST 2.C: TWO CONCURRENT PROPOSERS SUGGESTED DELAY PROFILES");
            System.out.println("##############################################################");

            // Initialise proposers with unique proposal IDs

            Proposer proposerM2 = new Proposer("M2", 5001, 4) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                        DelayBehaviour.applyDelay("M2");
                    super.handleMessage(clientSocket);
                }
            };
            Proposer proposerM3 = new Proposer("M3", 5002, 2) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                    DelayBehaviour.applyDelay("M3");
                    super.handleMessage(clientSocket);
                }
            };


            // Array to hold acceptors and their IDs
            Acceptor[] acceptors = new Acceptor[7];
            String[] acceptorIds = { "M1","M4", "M5", "M6", "M7", "M8", "M9"};
            int startingPort = 5003;

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

            // Start proposers

            new Thread(proposerM2::start).start();
            new Thread(proposerM3::start).start();

            //********** ESTABLISHING CONNECTIONS ************//
            // Connect each acceptor to both proposers
            for (Acceptor acceptor : acceptors) {

                acceptor.connectToOthers("M2", "localhost", 5001); // Connect to Proposer M2
                acceptor.connectToOthers("M3", "localhost", 5002);
            }

            // Connect proposers to each acceptor
            for (int i = 0; i < acceptors.length; i++) {

                proposerM2.connectToOthers(acceptorIds[i], "localhost", startingPort + i);
                proposerM3.connectToOthers(acceptorIds[i], "localhost", startingPort + i);
            }
            //*************************************************//

            // Start proposing
            proposerM2.propose();
            proposerM3.propose();

            // allow execution
            Thread.sleep(15000);

            //releasing ports
            System.out.println("RELEASING PORTS");
            proposerM2.close();
            proposerM3.close();
            for (Acceptor acceptor : acceptors) {
                acceptor.close();
            }

            //LOGGING COMPLETION
            System.out.println("##################################");
            System.out.println("TEST 2.C COMPLETED");
            System.out.println("##################################");


        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}

