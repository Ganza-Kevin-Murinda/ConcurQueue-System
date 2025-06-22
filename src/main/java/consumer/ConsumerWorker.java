package consumer;

import lombok.extern.slf4j.Slf4j;
import model.ETaskStatus;
import model.Task;
import prototype.MainApp;
import service.TaskStatusTracker;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced worker that can handle task failures and retry logic
 * Automatically retries failed tasks up to MAX_RETRY_ATTEMPTS
 */
@Slf4j
public class ConsumerWorker implements Runnable {
    private final String workerName;
    private final Random random;

    public ConsumerWorker(String workerName) {
        this.workerName = workerName;
        this.random = new Random();
    }

    @Override
    public void run() {
        log.info("ConsumerWorker {} started", workerName);

        try{
            while (!Thread.currentThread().isInterrupted()) {
                // Try to get a task from the queue (with timeout)
                Task task = MainApp.getTaskQueue().poll(2, TimeUnit.SECONDS);

                if (task != null) {
                    processTaskWithRetry(task);
                } else {
                    // No task available, check if we should continue
                    log.debug("Worker {} waiting for tasks...", workerName);
                }
            }
        } catch(InterruptedException e){
            log.info("ConsumerWorker {} interrupted, stopping...",  workerName);
        }
        log.info("ConsumerWorker {} finished", workerName);
    }

    private void processTaskWithRetry(Task task) {
        log.info("Worker {} picked up task: {} (Priority: {})",
                workerName, task.getName(), task.getPriority());

        // Update status to PROCESSING
        MainApp.getStatusTracker().updateTaskStatus(
                task.getId(), ETaskStatus.PROCESSING, workerName);

        try {
            // Simulate processing time based on priority
            int processingTime = calculateProcessingTime(task.getPriority());
            log.info("Worker {} processing task {} for {} ms",
                    workerName, task.getName(), processingTime);

            Thread.sleep(processingTime);

            // Simulate failures with higher probability (15% of chance)
            if (random.nextInt(100) < 15) {
                throw new RuntimeException("Simulated processing failure - network timeout");
            }

            // Success!
            MainApp.getStatusTracker().updateTaskStatus(
                    task.getId(), ETaskStatus.COMPLETED, workerName);
            MainApp.getTotalTasksProcessed().incrementAndGet();

            log.info("Worker {} successfully completed task: {}",
                    workerName, task.getName());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handleTaskFailure(task, "Worker interrupted during processing");
        } catch (Exception e) {
            handleTaskFailure(task, e.getMessage());
        }
    }

    private void handleTaskFailure(Task task, String errorMessage) {
        TaskStatusTracker statusTracker = MainApp.getStatusTracker();

        if (statusTracker.canRetry(task.getId())) {
            // Task can be retried
            statusTracker.incrementRetryCount(task.getId(), workerName);
            statusTracker.updateTaskStatusWithError(
                    task.getId(), ETaskStatus.RETRYING, workerName, errorMessage);

            // Re-queue the task for retry (at the end to maintain some fairness)
            MainApp.getTaskQueue().offer(task);
            MainApp.getTotalTasksRetried().incrementAndGet();

            log.warn("Worker {} re-queued task {} for retry (attempt {})",
                    workerName, task.getName(),
                    statusTracker.getTaskStatus(task.getId()).getRetryCount());
        } else {
            // Max retries exceeded, mark as permanently failed
            statusTracker.markTaskAsPermanentlyFailed(task.getId(), workerName, errorMessage);

            log.error("Worker {} permanently failed task {} after {} attempts",
                    workerName, task.getName(),
                    MainApp.getMaxRetryAttempts());
        }
    }

    private int calculateProcessingTime(int priority) {
        // Higher priority (lower number) = faster processing
        // Priority 1: 200-500ms, Priority 5: 800-1200ms
        int baseTime = 200 + (priority * 150);
        int variance = 300;
        return baseTime + random.nextInt(variance);
    }
}
