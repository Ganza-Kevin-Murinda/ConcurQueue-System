package consumer;

import prototype.ExecutorServiceDemo;
import lombok.extern.slf4j.Slf4j;
import model.ETaskStatus;
import model.Task;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Worker class that processes tasks from the shared queue
 * Runs in ExecutorService thread pool
 */
@Slf4j
public class TaskWorker implements Runnable {
    private final String workerName;
    private final Random random;

    public TaskWorker(String workerName) {
        this.workerName = workerName;
        this.random = new Random();
    }

    @Override
    public void run() {
        log.info("TaskWorker {} started", workerName);

        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Try to get a task from the queue (with timeout)
                Task task = ExecutorServiceDemo.getTaskQueue().poll(2, TimeUnit.SECONDS);

                if (task != null) {
                    processTask(task);
                } else {
                    // No task available, check if we should continue
                    log.debug("Worker {} waiting for tasks...", workerName);
                }
            }
        } catch (InterruptedException e) {
            log.info("TaskWorker {} interrupted, stopping...", workerName);
        }

        log.info("TaskWorker {} finished", workerName);
    }

    private void processTask(Task task) {
        log.info("Worker {} picked up task: {} (Priority: {})",
                workerName, task.getName(), task.getPriority());

        // Update status to PROCESSING
        ExecutorServiceDemo.getStatusTracker().updateTaskStatus(
                task.getId(), ETaskStatus.PROCESSING, workerName);

        try {
            // Simulate processing time based on priority
            int processingTime = calculateProcessingTime(task.getPriority());
            log.info("Worker {} processing task {} for {} ms",
                    workerName, task.getName(), processingTime);

            Thread.sleep(processingTime);

            // Simulate occasional failures (5% chance)
            if (random.nextInt(100) < 5) {
                throw new RuntimeException("Simulated processing failure");
            }

            // Success!
            ExecutorServiceDemo.getStatusTracker().updateTaskStatus(
                    task.getId(), ETaskStatus.COMPLETED, workerName);
            ExecutorServiceDemo.getTotalTasksProcessed().incrementAndGet();

            log.info("Worker {} successfully completed task: {}",
                    workerName, task.getName());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ExecutorServiceDemo.getStatusTracker().updateTaskStatus(
                    task.getId(), ETaskStatus.FAILED, workerName);
            log.error("Worker {} interrupted while processing task: {}",
                    workerName, task.getName());
        } catch (Exception e) {
            ExecutorServiceDemo.getStatusTracker().updateTaskStatus(
                    task.getId(), ETaskStatus.FAILED, workerName);
            log.error("Worker {} failed to process task: {} - Error: {}",
                    workerName, task.getName(), e.getMessage());
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
