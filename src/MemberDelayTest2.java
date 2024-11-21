
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
/**
 * Test 2b: Two concurrent proposers with suggested delay profiles
 * Conccurent proposal of M1 and M2, where M2 have higher proposal ID. This simulates the differences that M2 will win
 * when they are at the cafe, though is only 30% of time.
 *
 **/

public class MemberDelayTest2 {
    public static void main(String[] args) {
        try {
            //LOGGING COMMENDMENT
            System.out.println("############################################################");
            System.out.println("TEST 2.B: TWO CONCURRENT PROPOSERS SUGGESTED DELAY PROFILES");
            System.out.println("############################################################");

            // Initialise proposers with unique and increasing proposal IDs
            Proposer proposerM1 = new Proposer("M1", 5001, 1) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                    applyDelayBehavior("M1");
                    super.handleMessage(clientSocket);
                }
            };

            Proposer proposerM2 = new Proposer("M2", 5002, 2) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                    applyDelayBehavior("M2");
                    super.handleMessage(clientSocket);
                }
            };

            // Array to hold acceptors and their IDs
            Acceptor[] acceptors = new Acceptor[7];
            String[] acceptorIds = {"M3", "M4", "M5", "M6", "M7", "M8", "M9"};
            int startingPort = 5003;

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

            //********** ESTABLISHING CONNECTIONS ************//
            // Connect each acceptor to both proposers
            for (Acceptor acceptor : acceptors) {
                acceptor.connectToOthers("M1", "localhost", 5001); // Connect to Proposer M1
                acceptor.connectToOthers("M2", "localhost", 5002); // Connect to Proposer M2
            }

            // Connect proposers to each acceptor
            for (int i = 0; i < acceptors.length; i++) {
                proposerM1.connectToOthers(acceptorIds[i], "localhost", startingPort + i);
                proposerM2.connectToOthers(acceptorIds[i], "localhost", startingPort + i);
            }
            //*************************************************//

            // Start proposing
            proposerM1.propose();
            proposerM2.propose();

            // allow execution
            Thread.sleep(8000);

            //releasing ports
            System.out.println("RELEASING PORTS");
            proposerM1.close();
            proposerM2.close();
            for (Acceptor acceptor : acceptors) {
                acceptor.close();
            }

            //LOGGING COMPLETION
            System.out.println("##################################");
            System.out.println("TEST 2.B COMPLETED");
            System.out.println("##################################");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*----------------------------------Behavoiurs methods -------------------------------------------*/
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
    /*----------------------------------------------------------------------------------------------*/

}
