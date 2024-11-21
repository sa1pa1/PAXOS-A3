
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *Test 3c: Three concurrent proposers, two shuts down
 *M2 and M3 shuts down concurrently.
 *M1 will retry with higher proposalID and win.
 **/

public class ShutdownTest3 {
    public static void main(String[] args) {
        try {
            //TESTING COMMENCE
            System.out.println("####################################################");
            System.out.println("TEST 3.C: THREE CONCURRENT PROPOSERS, TWO SHUTS DOWN");
            System.out.println("####################################################");

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

            Proposer proposerM3 = new Proposer("M3", 5003, 3) {
                @Override
                protected void handleMessage(Socket clientSocket) {
                    DelayBehaviour.applyDelay("M3");
                    super.handleMessage(clientSocket);
                }
            };

            // Array to hold acceptors and their IDs
            Acceptor[] acceptors = new Acceptor[6];
            String[] acceptorIds = {"M4", "M5", "M6", "M7", "M8", "M9"};
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

            // Delay to allow connections to establish
            Thread.sleep(500);

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


            // Schedule concurrent proposals
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

            scheduler.execute(proposerM1::propose);

            scheduler.execute(() -> {
                proposerM2.propose();

                // Shut down M3
                scheduler.schedule(() -> {
                    System.out.println("Shutting down M2...");
                    try {
                        proposerM2.close();
                    } catch (IOException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }, 500, TimeUnit.MILLISECONDS); // Shut down M2 after 0.5 second
            });

            scheduler.execute(() -> {
                proposerM3.propose();

                // Shut down M3 immediately after proposing
                scheduler.schedule(() -> {
                    System.out.println("Shutting down M3...");
                    try {
                        proposerM3.close();
                    } catch (IOException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }, 500, TimeUnit.MILLISECONDS); // Shut down M3 after 0.5 second
            });

            // allow execution
            scheduler.awaitTermination(18, TimeUnit.SECONDS);
            scheduler.shutdown();
            Thread.sleep(18000);

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
            System.out.println("TEST 3.C COMPLETED");
            System.out.println("##################################");


        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
