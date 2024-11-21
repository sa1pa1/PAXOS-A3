

import java.io.IOException;
import java.net.Socket;

/**
 * Test 2.c: Three concurrent Proposers, with suggested delay profiles
 * This simulates when M1, M2 and M3 proposes, however their connectivity may differ for each election run
* In this test, we will simulate M1 with the lowest proposalID then M3 and M2 (highest)
* This test will show that M2 and M3 have differing chances of winning, that is if M2 is in the adelaide hills and
* M3 is not camping, then M3 will win.
* If M2 is at the cafe, M2 will win
* If both M2 is in the hills and M3 is camping, M1 will win despite having the smallest proposalID.
 **/

public class MemberDelayTest3 {
    public static void main(String[] args) {
        try {
            //TESTING COMMENCE
            System.out.println("##############################################################");
            System.out.println("TEST 2.C: THREE CONCURRENT PROPOSERS SUGGESTED DELAY PROFILES");
            System.out.println("##############################################################");

            // Initialise proposers with unique proposal IDs
            Proposer proposerM1 = new Proposer("M1", 5001, 1) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                    DelayBehaviour.applyDelay("M1");
                    super.handleMessage(clientSocket);
                }
            };

            Proposer proposerM2 = new Proposer("M2", 5002, 3) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                        DelayBehaviour.applyDelay("M2");
                    super.handleMessage(clientSocket);
                }
            };
            Proposer proposerM3 = new Proposer("M3", 5003, 2) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                    DelayBehaviour.applyDelay("M3");
                    super.handleMessage(clientSocket);
                }
            };


            // Array to hold acceptors and their IDs
            Acceptor[] acceptors = new Acceptor[6];
            String[] acceptorIds = { "M4", "M5", "M6", "M7", "M8", "M9"};
            int startingPort = 5004;

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
            new Thread(proposerM1::start).start();
            new Thread(proposerM2::start).start();
            new Thread(proposerM3::start).start();

            //********** ESTABLISHING CONNECTIONS ************//
            // Connect each acceptor to both proposers
            for (Acceptor acceptor : acceptors) {
                acceptor.connectToOthers("M1", "localhost", 5001); // Connect to Proposer M1
                acceptor.connectToOthers("M2", "localhost", 5002); // Connect to Proposer M2
                acceptor.connectToOthers("M3", "localhost", 5003);
            }

            // Connect proposers to each acceptor
            for (int i = 0; i < acceptors.length; i++) {
                proposerM1.connectToOthers(acceptorIds[i], "localhost", startingPort + i);
                proposerM2.connectToOthers(acceptorIds[i], "localhost", startingPort + i);
                proposerM3.connectToOthers(acceptorIds[i], "localhost", startingPort + i);
            }
            //*************************************************//

            // Start proposing
            proposerM1.propose();
            proposerM2.propose();
            proposerM3.propose();

            // allow execution
            Thread.sleep(10000);

            //releasing ports
            System.out.println("RELEASING PORTS");
            proposerM1.close();
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

