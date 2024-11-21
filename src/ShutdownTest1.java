import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Test 3a: Two concurrent proposers, one shuts down
 *In this test, we have two proposers, M1 and M2. Where M2 has the higher proposalID.
 *This test simulate that when M2 proposes then shuts down. Acceptors no longer able to make further acceptances as M1 has a lower proposal ID.
 *
 **/
public class ShutdownTest1 {
    public static void main(String[] args) {
        try {

            //TESTING COMMENCE
            System.out.println("###################################################");
            System.out.println("TEST 3.A: TWO CONCURRENT PROPOSERS, ONE SHUTS DOWN");
            System.out.println("###################################################");

            // Initialise proposers with unique and increasing proposal IDs
            Proposer proposerM1 = new Proposer("M1", 5001, 1) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                    DelayBehaviour.applyDelay("M1");
                    super.handleMessage(clientSocket);
                }
            };

            Proposer proposerM2 = new Proposer("M2", 5002, 2) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                    DelayBehaviour.applyDelay("M2");
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
                        DelayBehaviour.applyDelay(acceptorId);
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

            // Schedule concurrent proposals
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

            scheduler.execute(proposerM1::propose);
            scheduler.execute(() -> {
                proposerM2.propose();

                // Shut down M2
                scheduler.schedule(() -> {
                    System.out.println("Shutting down M2...");
                    try {
                        proposerM2.close();
                    } catch (IOException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }, 500, TimeUnit.MILLISECONDS); // Shut down M2 after 0.8 second
            });

            // allow execution
            scheduler.awaitTermination(20, TimeUnit.SECONDS);
            scheduler.shutdown();
            Thread.sleep(20000);

            //releasing ports
            System.out.println("RELEASING PORTS");
            proposerM1.close();
            proposerM2.close();
            for (Acceptor acceptor : acceptors) {
                acceptor.close();
            }

            //LOGGING COMPLETION
            System.out.println("##################################");
            System.out.println("TEST 3.A COMPLETED");
            System.out.println("##################################");


        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


}
