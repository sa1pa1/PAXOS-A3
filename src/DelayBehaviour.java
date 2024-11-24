import java.util.Random;

public class DelayBehaviour {
    public static void applyDelay(String memberId) {
        try {
            if ("M1".equals(memberId)) {
                // M1: Instant response
                System.out.println(memberId + " is responding immediately.");
            } else if ("M2".equals(memberId)) {
                // M2: Poor connectivity with occasional instant response
                double randomValue = Math.random();
                if (randomValue < 0.3) {
                    // Simulate being at the café with instant responses
                    System.out.println(memberId + " is responding instantly (at café).");
                } else {
                    // Simulate poor connectivity with delays
                    simulateLargeDelay(memberId);
                }

            } else if ("M3".equals(memberId)) {
                // M3: High chance of dropping messages
                double randomValue = Math.random();
                if (randomValue < 0.3) {
                    System.out.println(memberId + " is not responding. Maybe camping :))");
                }
                else {
                    simulateSmallDelay(memberId);
                }
            } else if (memberId.startsWith("M") && Integer.parseInt(memberId.substring(1)) >= 4) {
                // M4-M9: Simulate busy schedules
                simulateSmallDelay(memberId);
            }
        } catch (InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void simulateLargeDelay(String memberId) throws InterruptedException {
        int delay = 7000 + new Random().nextInt(2000); // Delay between 4 and 9 seconds
        System.out.println(memberId + " Very delayed, in the hills");
        Thread.sleep(delay);
    }


    private static void simulateSmallDelay(String memberId) throws InterruptedException {
        int delay = 1000 + new Random().nextInt(2000); // Delay between 1 and 3 seconds
        System.out.println(memberId + " delaying response due to busy schedule...");
        Thread.sleep(delay);
    }
}
