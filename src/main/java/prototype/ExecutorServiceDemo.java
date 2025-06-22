package prototype;

import consumer.TaskWorker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.Task;
import producer.EmailTaskProducer;
import producer.MaintenanceTaskProducer;
import producer.PaymentTaskProducer;
import service.TaskStatusTracker;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Replaces single consumer with a fixed thread pool of workers
 * Demonstrates parallel task processing with thread pool management
 */
@Slf4j
public class ExecutorServiceDemo {

    // Configuration
    private static final int WORKER_POOL_SIZE = 4;
    private static final int PRODUCER_RUN_TIME_SECONDS = 10;

    // Shared resources
    @Getter
    private static PriorityBlockingQueue<Task> taskQueue;
    @Getter
    private static TaskStatusTracker statusTracker;
    @Getter
    private static final AtomicInteger totalTasksSubmitted = new AtomicInteger(0);
    @Getter
    private static final AtomicInteger totalTasksProcessed = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        log.info("Starting ExecutorService Worker Pool Demo");
        log.info("Worker pool size: {}", WORKER_POOL_SIZE);

        // Initialize shared components
        taskQueue = new PriorityBlockingQueue<>();
        statusTracker = new TaskStatusTracker();

        // Phase 1: Start producer threads
        log.info("=== PHASE 1: Starting Producer Threads ===");
        Thread paymentProducer = new Thread(new PaymentTaskProducer(), "PaymentProducer");
        Thread emailProducer = new Thread(new EmailTaskProducer(), "EmailProducer");
        Thread maintenanceProducer = new Thread(new MaintenanceTaskProducer(), "MaintenanceProducer");

        paymentProducer.start();
        emailProducer.start();
        maintenanceProducer.start();

        // Phase 2: Start worker pool
        log.info("=== PHASE 2: Starting Worker Pool ===");
        ExecutorService workerPool = Executors.newFixedThreadPool(WORKER_POOL_SIZE);

        // Submit worker tasks to the pool
        for (int i = 0; i < WORKER_POOL_SIZE; i++) {
            workerPool.submit(new TaskWorker("Worker-" + (i + 1)));
        }

        // Let the system run
        Thread.sleep(PRODUCER_RUN_TIME_SECONDS * 1000);

        // Phase 3: Stop producers
        log.info("=== PHASE 3: Stopping Producers ===");
        paymentProducer.interrupt();
        emailProducer.interrupt();
        maintenanceProducer.interrupt();

        // Wait for producers to finish
        paymentProducer.join();
        emailProducer.join();
        maintenanceProducer.join();

        log.info("All producers stopped. Total tasks submitted: {}", totalTasksSubmitted.get());

        // Phase 4: Let workers finish remaining tasks
        log.info("=== PHASE 4: Processing Remaining Tasks ===");
        Thread.sleep(3000); // Give workers time to finish remaining tasks

        // Phase 5: Shutdown worker pool
        log.info("=== PHASE 5: Shutting Down Worker Pool ===");
        workerPool.shutdown();

        try {
            if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Worker pool didn't terminate gracefully, forcing shutdown...");
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
        }

        // Final results
        log.info("=== FINAL RESULTS ===");
        log.info("Tasks submitted: {}", totalTasksSubmitted.get());
        log.info("Tasks processed: {}", totalTasksProcessed.get());
        log.info("Remaining in queue: {}", taskQueue.size());

        statusTracker.printAllTaskStatuses();
        log.info("ExecutorService Demo completed successfully!");
    }

}
