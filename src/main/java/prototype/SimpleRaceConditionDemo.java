package prototype;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple Race Condition Demonstration
 * Shows the problem and the fix in just a few lines
 */
public class SimpleRaceConditionDemo {

    // UNSAFE - regular int (will lose updates)
    private static int unsafeCounter = 0;

    // SAFE - AtomicInteger (correct results)
    private static AtomicInteger safeCounter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Simple Race Condition Demo ===");

        // Create 5 threads, each will increment 1000 times
        Thread[] threads = new Thread[5];

        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    unsafeCounter++;                    // UNSAFE - race condition!
                    safeCounter.incrementAndGet();      // SAFE - atomic operation
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            thread.join();
        }

        // Show results
        System.out.println("Expected result: 5000");
        System.out.println("Unsafe counter:  " + unsafeCounter + " (❌ Wrong!)");
        System.out.println("Safe counter:    " + safeCounter.get() + " (✅ Correct!)");
        System.out.println("Lost updates:    " + (5000 - unsafeCounter));
    }
}