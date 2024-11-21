/*This simulates when M1, M2 and M3 proposes, however their connectivity may differ for each election run
* In this test, we will simulate M1 with the lowest proposalID then M3 and M2 (highest)
* This test will show that M2 and M3 have differing chances of winning, that is if M2 is in the adelaide hills and
* M3 is not camping, then M3 will win.
* If M2 is at the cafe, M2 will win
* If both M2 is in the hills and M3 is camping, M1 will win despite having the smallest proposalID.


/*NOTE: this test is more important than testing with members M4-M9 as they are not interested in becoming council president.*/
/* Test this at least 5 times to obtain all three scenarios*/

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

public class MemberDelayTest3 {
    public static void main(String[] args) {
        try {
            // Initialize proposers with unique and increasing proposal IDs
            Proposer proposerM1 = new Proposer("M1", 5001, 1) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                    applyDelayBehavior("M1");
                    super.handleMessage(clientSocket);
                }
            };

            Proposer proposerM2 = new Proposer("M2", 5002, 3) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                    applyDelayBehavior("M2");
                    super.handleMessage(clientSocket);
                }
            };
            Proposer proposerM3 = new Proposer("M3", 5003, 2) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                    applyDelayBehavior("M3");
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
                        applyDelayBehavior(acceptorId);
                        super.handleMessage(clientSocket);
                    }
                };

                new Thread(acceptors[i]::start).start();
            }



            // Start proposers
            new Thread(proposerM1::start).start();
            new Thread(proposerM2::start).start();
            new Thread(proposerM3::start).start();

            // Connect each acceptor to both proposers
            for (Acceptor acceptor : acceptors) {
                acceptor.connectToPeer("M1", "localhost", 5001); // Connect to Proposer M1
                acceptor.connectToPeer("M2", "localhost", 5002); // Connect to Proposer M2
                acceptor.connectToPeer("M3", "localhost", 5003);
            }

            // Connect proposers to each acceptor
            for (int i = 0; i < acceptors.length; i++) {
                proposerM1.connectToPeer(acceptorIds[i], "localhost", startingPort + i);
                proposerM2.connectToPeer(acceptorIds[i], "localhost", startingPort + i);
                proposerM3.connectToPeer(acceptorIds[i], "localhost", startingPort + i);
            }

            // Start proposing
            proposerM1.propose();
            proposerM2.propose();
            proposerM3.propose();

            Thread.sleep(10000);

            proposerM1.close();
            proposerM2.close();
            proposerM3.close();
            for (Acceptor acceptor : acceptors) {
                acceptor.close();
            }


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void applyDelayBehavior(String memberId) {
        try {
            if ("M1".equals(memberId)) {
                // M1: Instant response
                System.out.println(memberId + " is responding immediately.");
            } else if ("M2".equals(memberId)) {
                // M2: Poor connectivity with occasional instant response
                if (Math.random() < 0.3) {
                    // Simulate being at the café with instant responses
                    System.out.println(memberId + " is responding instantly (at café).");
                } else {
                    // Simulate poor connectivity with delays
                    simulateLargeDelay(memberId);
                }

            } else if ("M3".equals(memberId)) {
                // M3: High chance of dropping messages
                if (NoResponse()) {
                    System.out.println(memberId + " is not responding. Maybe camping :))");
                    return;
                }
                else if (!NoResponse()) {
                    simulateSmallDelay(memberId);
                }
            } else if (memberId.startsWith("M") && Integer.parseInt(memberId.substring(1)) >= 4) {
                // M4-M9: Simulate busy schedules
                simulateSmallDelay(memberId);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void simulateLargeDelay(String memberId) throws InterruptedException {
        int delay = 4000 + new Random().nextInt(5000); // Delay between 4 to 9 seconds
        System.out.println(memberId + " Very delayed, in the hills");
        Thread.sleep(delay);
    }


    private static boolean NoResponse() {
        // 30% chance to drop the message
        return Math.random() < 0.3;
    }

    private static void simulateSmallDelay(String memberId) throws InterruptedException {
        int delay = 1000 + new Random().nextInt(2000); // Delay between 1 to 3 seconds
        System.out.println(memberId + " delaying response due to busy schedule...");
        Thread.sleep(delay);
    }
}

