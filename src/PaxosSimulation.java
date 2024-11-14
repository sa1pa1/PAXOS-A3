import java.io.IOException;

public class PaxosSimulation {
    public static void main(String[] args) {
        try {
            Proposer proposer = new Proposer("M1", 5001, "M1");
            Acceptor acceptor1 = new Acceptor("M2", 5002);
            Acceptor acceptor2 = new Acceptor("M3", 5003);
            Acceptor acceptor3 = new Acceptor("M4", 5004);
            Acceptor acceptor4 = new Acceptor("M5", 5005);


            // Start each member in its own thread
            new Thread(proposer::start).start();
            new Thread(acceptor1::start).start();
            new Thread(acceptor2::start).start();
            new Thread(acceptor3::start).start();
            new Thread(acceptor4::start).start();


            // Connect proposer to acceptors and learner
            proposer.connectToPeer("M2", "localhost", 5002);
            proposer.connectToPeer("M3", "localhost", 5003);
            proposer.connectToPeer("M4", "localhost", 5004);
            proposer.connectToPeer("M5", "localhost", 5005);


            // Connect acceptors back to the proposer for PROMISE messages
            acceptor1.connectToPeer("M1", "localhost", 5001);
            acceptor2.connectToPeer("M1", "localhost", 5001);
            acceptor3.connectToPeer("M1", "localhost", 5001);
            acceptor4.connectToPeer("M1", "localhost", 5001);


            // Start the proposal process
            proposer.propose();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
