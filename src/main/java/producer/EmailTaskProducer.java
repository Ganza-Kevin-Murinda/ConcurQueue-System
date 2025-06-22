package producer;

import prototype.ExecutorServiceDemo;
import lombok.extern.slf4j.Slf4j;
import model.ETaskStatus;
import model.Task;

import java.util.Random;

/**
 * Producer for medium-priority email tasks
 */
@Slf4j
public class EmailTaskProducer implements Runnable {
    private final Random random = new Random();
    private final String[] emailTypes = {"welcome", "reminder", "notification", "marketing", "alert"};
    private int taskCounter = 0;

    @Override
    public void run() {
        log.info("EmailTaskProducer started");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Create medium-priority email tasks (priority 3-4)
                String emailType = emailTypes[random.nextInt(emailTypes.length)];
                String taskName = "Email-" + emailType + "-" + (++taskCounter);
                int priority = random.nextInt(2) + 3; // Priority 3 or 4
                String payload = String.format("user_id=%d,template=%s,email=user%d@example.com",
                        5000 + taskCounter, emailType, taskCounter);

                Task task = new Task(taskName, priority, payload);
                ExecutorServiceDemo.getTaskQueue().offer(task);
                ExecutorServiceDemo.getStatusTracker().updateTaskStatus(
                        task.getId(), ETaskStatus.SUBMITTED, Thread.currentThread().getName());
                ExecutorServiceDemo.getTotalTasksSubmitted().incrementAndGet();

                log.info("Created email task: {} (Priority: {})", taskName, priority);

                // Submit email tasks every 1.5-3 seconds (medium frequency)
                Thread.sleep(1500 + random.nextInt(1500));
            }
        } catch (InterruptedException e) {
            log.info("EmailTaskProducer interrupted, stopping...");
        }

        log.info("EmailTaskProducer finished. Created {} tasks", taskCounter);
    }
}
