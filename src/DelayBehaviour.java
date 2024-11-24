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
                    return;
                }
                else {
                    simulateSmallDelay(memberId);
                }
            } else if (memberId.startsWith("M") && Integer.parseInt(memberId.substring(1)) >= 4) {
                // M4-M9: Simulate busy schedules
                simulateBusySchedule(memberId);
            }
        } catch (InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void simulateLargeDelay(String memberId) throws InterruptedException {
        int delay = 10000 + new Random().nextInt(5000); // Delay between 15 and 20 seconds
        System.out.println(memberId + " Very delayed, in the hills");
        Thread.sleep(delay);
    }


    private static void simulateSmallDelay(String memberId) throws InterruptedException {
        int delay = 2000 + new Random().nextInt(3000); // Delay between 2 and 5 seconds
        System.out.println(memberId + " delayed connection...");
        Thread.sleep(delay);
    }

    private static void simulateBusySchedule(String memberId) throws InterruptedException {
        int delay = 3000 + new Random().nextInt(3000); //delays between 3-6 seconds
        System.out.println(memberId + " is delayed due to busy schedule...");
        Thread.sleep(delay);
    }
}
