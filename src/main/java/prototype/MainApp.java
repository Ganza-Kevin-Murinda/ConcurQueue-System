package prototype;

import consumer.ConsumerWorker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.Task;
import producer.EmailTaskProducer;
import producer.MaintenanceTaskProducer;
import producer.PaymentTaskProducer;
import service.TaskStatusTracker;
import monitor.MonitorThread;
import java.util.concurrent.ThreadPoolExecutor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhanced Task Status Tracking with Retry Logic
 * Demonstrates FAILED status and retry mechanism for failed tasks
 */
@Slf4j
public class MainApp {

    // Configuration
    private static final int WORKER_POOL_SIZE = 4;
    private static final int PRODUCER_RUN_TIME_SECONDS = 8;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    // Shared resources
    @Getter
    private static PriorityBlockingQueue<Task> taskQueue;
    @Getter
    private static TaskStatusTracker statusTracker;
    @Getter
    private final static AtomicInteger totalTasksSubmitted = new AtomicInteger(0);
    @Getter
    private final static AtomicInteger totalTasksProcessed = new AtomicInteger(0);
    @Getter
    private final static AtomicInteger totalTasksRetried = new AtomicInteger(0);

    public static int getMaxRetryAttempts() { return MAX_RETRY_ATTEMPTS; }

    public static void main(String[] args) throws InterruptedException {
        log.info("Starting Enhanced Task Status Tracking with Retry Logic Demo");
        log.info("Worker pool size: {}, Max retry attempts: {}", WORKER_POOL_SIZE, MAX_RETRY_ATTEMPTS);

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
        ThreadPoolExecutor workerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(WORKER_POOL_SIZE);

        // Submit worker tasks to the pool
        for (int i = 0; i < WORKER_POOL_SIZE; i++) {
            workerPool.submit(new ConsumerWorker("Worker-" + (i + 1)));
        }

        // Phase 3: Start monitoring thread (NEW FOR STEP 7)
        log.info("=== PHASE 3: Starting Monitoring Thread ===");
        Thread monitoringThread = new Thread(new MonitorThread(workerPool), "MonitoringThread");
        monitoringThread.start();

        // Let the system run
        Thread.sleep(PRODUCER_RUN_TIME_SECONDS * 1000);

        // Phase 4: Stop producers
        log.info("=== PHASE 4: Stopping Producers ===");
        paymentProducer.interrupt();
        emailProducer.interrupt();
        maintenanceProducer.interrupt();

        paymentProducer.join();
        emailProducer.join();
        maintenanceProducer.join();

        log.info("All producers stopped. Total tasks submitted: {}", totalTasksSubmitted.get());

        // Phase 5: Let workers finish remaining tasks
        log.info("=== PHASE 5: Processing Remaining Tasks ===");
        Thread.sleep(5000); // Give workers time to finish and retry failed tasks

        // Phase 6: Shutdown worker pool
        log.info("==== PHASE 6: Shutting Down Worker Pool ====");

        // Stop monitoring thread first
        log.info("Stopping monitoring thread...");
        monitoringThread.interrupt();
        try {
            monitoringThread.join(2000); // Wait up to 2 seconds
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for monitoring thread to stop");
        }

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
        log.info("Tasks retried: {}", totalTasksRetried.get());
        log.info("Remaining in queue: {}", taskQueue.size());

        statusTracker.printStatusSummary();
        statusTracker.printFailedTasks();

        log.info("Retry Logic Demo completed successfully!");
    }

}
