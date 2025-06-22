package prototype;

import model.ETaskStatus;
import model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.SimpleTaskProcessor;
import service.TaskStatusTracker;

import java.util.PriorityQueue;
import java.util.Queue;

public class SingleThreadedDemo {
    public static final Logger logger = LoggerFactory.getLogger(SingleThreadedDemo.class);

    public static void main(String[] args) {
        logger.info("Starting Single -Threaded ConcurQueue Prototype");

        // Initialize components
        TaskStatusTracker statusTracker = new TaskStatusTracker();
        SimpleTaskProcessor processor = new SimpleTaskProcessor(statusTracker);
        Queue<Task> taskQueue = new PriorityQueue<>();

        // Phase 1: Create and submit tasks (Producer simulation)
        logger.info("=== PHASE 1: Creating Tasks ===");
        createAndSubmitTasks(taskQueue, statusTracker);

        // Phase 2: Process all tasks (Consumer simulation)
        logger.info("=== PHASE 2: Processing Tasks ===");
        processAllTasks(taskQueue, processor);

        // Phase 3: Show final results
        logger.info("=== PHASE 3: Final Results ===");
        statusTracker.printAllTaskStatuses();

        logger.info("Single-Threaded Prototype completed successfully!");

    }

    private static void createAndSubmitTasks(Queue<Task> taskQueue, TaskStatusTracker statusTracker) {
        // Create tasks with different priorities
        Task[] tasks = {
                new Task("ProcessPayment-1001", 1, "payment_id=1001,amount=250.00"),
                new Task("SendEmail-welcome", 3, "user_id=5432,template=welcome"),
                new Task("GenerateReport-daily", 5, "report_type=daily,date=2024-06-19"),
                new Task("ProcessPayment-1002", 1, "payment_id=1002,amount=150.00"),
                new Task("BackupDatabase", 4, "database=main,type=incremental"),
                new Task("SendEmail-reminder", 3, "user_id=9876,template=reminder"),
                new Task("ProcessOrder-789", 2, "order_id=789,items=3"),
                new Task("CleanupTempFiles", 5, "directory=/tmp,age=7days")
        };

        // Submit tasks to queue and update status
        for (Task task : tasks) {
            taskQueue.offer(task);
            statusTracker.updateTaskStatus(task.getId(), ETaskStatus.SUBMITTED, "main");
            logger.info("Submitted task: {}", task);
        }

        logger.info("Total tasks submitted: {}", tasks.length);
        logger.info("Queue size: {}", taskQueue.size());
    }

    private static void processAllTasks(Queue<Task> taskQueue, SimpleTaskProcessor processor) {
        int processedCount = 0;

        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.poll();
            logger.info("Dequeued task for processing: {}", task.getName());

            processor.processTask(task);
            processedCount++;

            // Small delay between tasks for readability
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.info("Processed {} tasks", processedCount);
    }
}
