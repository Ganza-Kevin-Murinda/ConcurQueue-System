package producer;

import prototype.ExecutorServiceDemo;
import lombok.extern.slf4j.Slf4j;
import model.ETaskStatus;
import model.Task;
import java.util.Random;

/**
 * Producer for high-priority payment tasks
 */
@Slf4j
public class PaymentTaskProducer implements Runnable {

    private final Random random = new Random();
    private int taskCounter = 0;

    @Override
    public void run() {
        log.info("PaymentTaskProducer started");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Create high-priority payment tasks (priority 1-2)
                String taskName = "Payment-" + (++taskCounter);
                int priority = random.nextInt(2) + 1; // Priority 1 or 2
                String payload = String.format("payment_id=%d,amount=%.2f,type=credit_card",
                        1000 + taskCounter, 100.0 + random.nextDouble() * 900);

                Task task = new Task(taskName, priority, payload);
                ExecutorServiceDemo.getTaskQueue().offer(task);
                ExecutorServiceDemo.getStatusTracker().updateTaskStatus(
                        task.getId(), ETaskStatus.SUBMITTED, Thread.currentThread().getName());
                ExecutorServiceDemo.getTotalTasksSubmitted().incrementAndGet();

                log.info("Created payment task: {} (Priority: {})", taskName, priority);

                // Submit payment tasks every 1-2 seconds (high frequency)
                Thread.sleep(1000 + random.nextInt(1000));
            }
        } catch (InterruptedException e) {
            log.info("PaymentTaskProducer interrupted, stopping...");
        }

        log.info("PaymentTaskProducer finished. Created {} tasks", taskCounter);
    }
}
