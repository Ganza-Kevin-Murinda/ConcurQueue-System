package prototype;

import lombok.extern.slf4j.Slf4j;
import model.ETaskStatus;
import model.Task;
import service.SimpleTaskProcessor;
import service.TaskStatusTracker;

import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This version demonstrates multiple producer threads submitting tasks concurrently
 * with different priority strategies and submission patterns
 */
@Slf4j
public class MultipleProducersDemo {

    static PriorityBlockingQueue<Task> taskQueue;
    static TaskStatusTracker statusTracker;
    static AtomicInteger totalTasksSubmitted = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        log.info("Starting Multiple Producers Demo");

        // Initialize shared components
        taskQueue = new PriorityBlockingQueue<>();
        statusTracker = new TaskStatusTracker();
        SimpleTaskProcessor processor = new SimpleTaskProcessor(statusTracker);

        // Create and start producer threads
        log.info("=== PHASE 1: Starting Producer Threads ===");
        Thread paymentProducer = new Thread(new PaymentProducer(), "PaymentProducer");
        Thread emailProducer = new Thread(new EmailProducer(), "EmailProducer");
        Thread maintenanceProducer = new Thread(new MaintenanceProducer(), "MaintenanceProducer");

        // Start all producers
        paymentProducer.start();
        emailProducer.start();
        maintenanceProducer.start();

        Thread.sleep(8000); // Let producers run for a while

        // stop producers
        log.info("=== PHASE 2: Stopping Producers ===");
        paymentProducer.interrupt();
        emailProducer.interrupt();
        maintenanceProducer.interrupt();

        // Wait for producers to finish
        paymentProducer.join();
        emailProducer.join();
        maintenanceProducer.join();

        log.info("All producers stopped. Total tasks submitted: {}", totalTasksSubmitted.get());
        log.info("Queue size: {}", taskQueue.size());

        // Process all tasks (still single consumer for now)
        log.info("=== PHASE 3: Processing Tasks ===");
        processAllTasks(processor);

        // Show final results
        log.info("=== PHASE 4: Final Results ===");
        statusTracker.printAllTaskStatuses();

        log.info("Multiple Producers Demo completed successfully!");
    }

    private static void processAllTasks(SimpleTaskProcessor processor) {
        int processedCount = 0;

        while (!taskQueue.isEmpty()) {
            try {
                Task task = taskQueue.poll();
                if (task != null) {
                    log.info("Processing task: {} (Priority: {}, Submitted by: {})",
                            task.getName(), task.getPriority(),
                            statusTracker.getTaskStatus(task.getId()).getProcessingThreadName());
                    processor.processTask(task);
                    processedCount++;
                    Thread.sleep(50); // Faster processing for demo
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("Processed {} tasks", processedCount);
    }
}

/**
 * Producer 1: Generates high-priority payment tasks
 */
@Slf4j
class PaymentProducer implements Runnable {
    private final Random random = new Random();
    private int taskCounter = 0;

    @Override
    public void run() {
        log.info("PaymentProducer started");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Create high-priority payment tasks (priority 1-2)
                String taskName = "Payment-" + (++taskCounter);
                int priority = random.nextInt(2) + 1; // Priority 1 or 2
                String payload = String.format("payment_id=%d,amount=%.2f,type=credit_card",
                        1000 + taskCounter, 100.0 + random.nextDouble() * 900);

                Task task = new Task(taskName, priority, payload);
                MultipleProducersDemo.taskQueue.offer(task);
                MultipleProducersDemo.statusTracker.updateTaskStatus(
                        task.getId(), ETaskStatus.SUBMITTED, Thread.currentThread().getName());
                MultipleProducersDemo.totalTasksSubmitted.incrementAndGet();

                log.info("Created payment task: {} (Priority: {})", taskName, priority);

                // Submit payment tasks every 1-2 seconds
                Thread.sleep(1000 + random.nextInt(1000));
            }
        } catch (InterruptedException e) {
            log.info("PaymentProducer interrupted, stopping...");
        }

        log.info("PaymentProducer finished. Created {} tasks", taskCounter);
    }
}

/**
 * Producer 2: Generates medium-priority email tasks
 */
@Slf4j
class EmailProducer implements Runnable {
    private final Random random = new Random();
    private final String[] emailTypes = {"welcome", "reminder", "notification", "marketing", "alert"};
    private int taskCounter = 0;

    @Override
    public void run() {
        log.info("EmailProducer started");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Create medium-priority email tasks (priority 3-4)
                String emailType = emailTypes[random.nextInt(emailTypes.length)];
                String taskName = "Email-" + emailType + "-" + (++taskCounter);
                int priority = random.nextInt(2) + 3; // Priority 3 or 4
                String payload = String.format("user_id=%d,template=%s,email=user%d@example.com",
                        5000 + taskCounter, emailType, taskCounter);

                Task task = new Task(taskName, priority, payload);
                MultipleProducersDemo.taskQueue.offer(task);
                MultipleProducersDemo.statusTracker.updateTaskStatus(
                        task.getId(), ETaskStatus.SUBMITTED, Thread.currentThread().getName());
                MultipleProducersDemo.totalTasksSubmitted.incrementAndGet();

                log.info("Created email task: {} (Priority: {})", taskName, priority);

                // Submit email tasks every 1.5-3 seconds
                Thread.sleep(1500 + random.nextInt(1500));
            }
        } catch (InterruptedException e) {
            log.info("EmailProducer interrupted, stopping...");
        }

        log.info("EmailProducer finished. Created {} tasks", taskCounter);
    }
}

/**
 * Producer 3: Generates low-priority maintenance tasks
 */
@Slf4j
class MaintenanceProducer implements Runnable {
    private final Random random = new Random();
    private final String[] maintenanceTypes = {"backup", "cleanup", "report", "archive", "optimize"};
    private int taskCounter = 0;

    @Override
    public void run() {
        log.info("MaintenanceProducer started");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Create low-priority maintenance tasks (priority 4-5)
                String maintenanceType = maintenanceTypes[random.nextInt(maintenanceTypes.length)];
                String taskName = "Maintenance-" + maintenanceType + "-" + (++taskCounter);
                int priority = random.nextInt(2) + 4; // Priority 4 or 5
                String payload = String.format("type=%s,target=system,scheduled=true", maintenanceType);

                Task task = new Task(taskName, priority, payload);
                MultipleProducersDemo.taskQueue.offer(task);
                MultipleProducersDemo.statusTracker.updateTaskStatus(
                        task.getId(), ETaskStatus.SUBMITTED, Thread.currentThread().getName());
                MultipleProducersDemo.totalTasksSubmitted.incrementAndGet();

                log.info("Created maintenance task: {} (Priority: {})", taskName, priority);

                // Submit maintenance tasks every 2-4 seconds
                Thread.sleep(2000 + random.nextInt(2000));
            }
        } catch (InterruptedException e) {
            log.info("MaintenanceProducer interrupted, stopping...");
        }

        log.info("MaintenanceProducer finished. Created {} tasks", taskCounter);
    }
}