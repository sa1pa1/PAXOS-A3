import java.io.IOException;
/**
 * Test 1.a: One proposer
 *
 **/
public class ImmediateResponseTest1 {
    public static void main(String[] args) {
        try {
            //LOGGING COMMENDMENT
            System.out.println("##################################");
            System.out.println("TEST 1.A: ONE PROPOSER, IMMEDIATE RESPONSE");
            System.out.println("##################################");
            String proposerID = "M1";

            // Initialise M1
            Proposer proposerM1 = new Proposer(proposerID, 5001, 1);

            // Array to hold acceptors and their IDs
            Acceptor[] acceptors = new Acceptor[8];
            String[] acceptorIds = {"M2","M3", "M4", "M5", "M6", "M7", "M8", "M9"};
            int startingPort = 5002;

            // Create and start each acceptor, assigning them unique ports
            for (int i = 0; i < acceptors.length; i++) {
                acceptors[i] = new Acceptor(acceptorIds[i], startingPort + i);
                new Thread(acceptors[i]::start).start();
            }

            // Start proposer
            new Thread(proposerM1::start).start();

            //********** ESTABLISHING CONNECTIONS ************//
            // Connect each acceptor to both proposers
            for (Acceptor acceptor : acceptors) {
                acceptor.connectToOthers(proposerID, "localhost", 5001);
            }

            // Connect proposers to each acceptor
            for (int i = 0; i < acceptors.length; i++) {
                proposerM1.connectToOthers(acceptorIds[i], "localhost", startingPort + i);
            }
            //*************************************************//

            // M1 proposes
            proposerM1.propose();
            Thread.sleep(3000);

            //releasing ports
            System.out.println("RELEASING PORTS");
            proposerM1.close();
            for (Acceptor acceptor : acceptors) {
                acceptor.close();
            }

            //LOGGING COMPLETION
            System.out.println("##################################");
            System.out.println("TEST 1.A COMPLETED");
            System.out.println("##################################");


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {


        }
    }
}