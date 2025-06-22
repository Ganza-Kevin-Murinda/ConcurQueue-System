package service;

import model.ETaskStatus;
import model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class SimpleTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SimpleTaskProcessor.class);
    private final TaskStatusTracker statusTracker;
    private final Random random;

    public SimpleTaskProcessor(TaskStatusTracker statusTracker) {
        this.statusTracker = statusTracker;
        this.random = new Random();
    }

    public void processTask(Task task) {

        String currentThread = Thread.currentThread().getName();
        logger.info("Starting to process task: {}", task);

        // Update status to PROCESSING
        statusTracker.updateTaskStatus(task.getId(), ETaskStatus.PROCESSING, currentThread);

        try {
            // Simulate processing time based on priority
            int processingTime = calculateProcessingTime(task.getPriority());
            logger.info("Processing task {} for {} ms", task.getName(), processingTime);

            Thread.sleep(processingTime);

            // Simulate occasional failures (10% chance)
            if (random.nextInt(100) < 10) {
                throw new RuntimeException("Simulated processing failure");
            }

            // Success!
            statusTracker.updateTaskStatus(task.getId(), ETaskStatus.COMPLETED, currentThread);
            logger.info("Successfully completed task: {}", task.getName());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            statusTracker.updateTaskStatus(task.getId(), ETaskStatus.FAILED, currentThread);
            logger.error("Task processing interrupted: {}", task.getName());
        } catch (Exception e) {
            statusTracker.updateTaskStatus(task.getId(), ETaskStatus.FAILED, currentThread);
            logger.error("Task processing failed: {} - Error: {}", task.getName(), e.getMessage());
        }
    }

    private int calculateProcessingTime(int priority) {
        // Higher priority (lower number) = faster processing
        // Priority 1: 100-300ms, Priority 5: 500-1000ms
        int baseTime = 100 + (priority * 100);
        int variance = 200;
        return baseTime + random.nextInt(variance);
    }
}
