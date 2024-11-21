import java.io.IOException;

public class ImmediateResponseTest1 {
    public static void main(String[] args) {
        try {
            String proposerID = "M1";

            // Initialize proposers
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

            // Start proposers
            new Thread(proposerM1::start).start();


            // Connect each acceptor to both proposers
            for (Acceptor acceptor : acceptors) {
                acceptor.connectToOthers(proposerID, "localhost", 5001);
            }

            // Connect proposers to each acceptor
            for (int i = 0; i < acceptors.length; i++) {
                proposerM1.connectToOthers(acceptorIds[i], "localhost", startingPort + i);
            }

            // Start M2's proposal immediately
            proposerM1.propose();
            Thread.sleep(3000);
            proposerM1.close();
            for (Acceptor acceptor : acceptors) {
                acceptor.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {


        }
    }
}