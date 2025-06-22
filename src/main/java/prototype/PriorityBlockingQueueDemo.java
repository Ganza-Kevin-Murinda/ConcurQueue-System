package prototype;

import lombok.extern.slf4j.Slf4j;
import model.ETaskStatus;
import model.Task;
import service.SimpleTaskProcessor;
import service.TaskStatusTracker;

import java.util.concurrent.PriorityBlockingQueue;

@Slf4j
public class PriorityBlockingQueueDemo {

    public static void main(String[] args) {
        log.info("Starting PriorityBlockingQueue Demo");

        TaskStatusTracker statusTracker = new TaskStatusTracker();
        SimpleTaskProcessor processor = new SimpleTaskProcessor(statusTracker);

        PriorityBlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>();

        // Phase 1: Create and submit tasks
        log.info("=== PHASE 1: Creating Tasks ===");
        createAndSubmitTasks(taskQueue, statusTracker);

        // Phase 2: Process tasks
        log.info("=== PHASE 2: Processing Tasks ===");
        processAllTasks(taskQueue, processor);

        // Phase 3: Show final results
        log.info("=== PHASE 3: Final Results ===");
        statusTracker.printAllTaskStatuses();

        log.info("PriorityBlockingQueue Demo completed successfully!");
    }

    private static void createAndSubmitTasks(PriorityBlockingQueue<Task> taskQueue,
                                             TaskStatusTracker statusTracker) {
        // Create tasks with mixed priorities to test priority ordering
        Task[] tasks = {
                new Task("CriticalPayment-1001", 1, "payment_id=1001,amount=5000.00"),
                new Task("SendEmail-welcome", 4, "user_id=5432,template=welcome"),
                new Task("GenerateReport-daily", 5, "report_type=daily,date=2024-06-19"),
                new Task("UrgentPayment-1002", 1, "payment_id=1002,amount=1500.00"),
                new Task("ProcessOrder-789", 2, "order_id=789,items=3"),
                new Task("BackupDatabase", 5, "database=main,type=incremental"),
                new Task("SendEmail-reminder", 4, "user_id=9876,template=reminder"),
                new Task("HighPriorityOrder-456", 2, "order_id=456,items=1"),
                new Task("CleanupTempFiles", 5, "directory=/tmp,age=7days")
        };

        // Submit tasks to blocking queue
        for (Task task : tasks) {
            taskQueue.offer(task); // offer() is thread-safe for PriorityBlockingQueue
            statusTracker.updateTaskStatus(task.getId(), ETaskStatus.SUBMITTED, "main");
            log.info("Submitted task: {} (Priority: {})", task.getName(), task.getPriority());
        }

        log.info("Total tasks submitted: {}", tasks.length);
        log.info("Queue size: {}", taskQueue.size());

        // Demonstrate priority ordering
        log.info("=== Priority Order Verification ===");
        log.info("Tasks will be processed in priority order (1=highest, 5=lowest)");
    }

    private static void processAllTasks(PriorityBlockingQueue<Task> taskQueue,
                                                         SimpleTaskProcessor processor) {
        int processedCount = 0;

        // Process until queue is empty
        while (!taskQueue.isEmpty()) {
            try {
                // Use poll() instead of take() since we know queue has items
                Task task = taskQueue.poll();
                if (task != null) {
                    log.info("Dequeued task: {} (Priority: {})", task.getName(), task.getPriority());
                    processor.processTask(task);
                    processedCount++;

                    // Small delay for readability
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("Processed {} tasks", processedCount);
    }

}
