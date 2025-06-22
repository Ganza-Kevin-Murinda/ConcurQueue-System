package producer;

import prototype.MainApp;
import lombok.extern.slf4j.Slf4j;
import model.ETaskStatus;
import model.Task;

import java.util.Random;

/**
 * Producer for low-priority maintenance tasks
 */
@Slf4j
public class MaintenanceTaskProducer implements Runnable{
    private final Random random = new Random();
    private final String[] maintenanceTypes = {"backup", "cleanup", "report", "archive", "optimize"};
    private int taskCounter = 0;

    @Override
    public void run() {
        log.info("MaintenanceTaskProducer started");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Create low-priority maintenance tasks (priority 4-5)
                String maintenanceType = maintenanceTypes[random.nextInt(maintenanceTypes.length)];
                String taskName = "Maintenance-" + maintenanceType + "-" + (++taskCounter);
                int priority = random.nextInt(2) + 4; // Priority 4 or 5
                String payload = String.format("type=%s,target=system,scheduled=true", maintenanceType);

                Task task = new Task(taskName, priority, payload);
                MainApp.getTaskQueue().offer(task);
                MainApp.getStatusTracker().updateTaskStatus(
                        task.getId(), ETaskStatus.SUBMITTED, Thread.currentThread().getName());
                MainApp.getTotalTasksSubmitted().incrementAndGet();

                log.info("Created maintenance task: {} (Priority: {})", taskName, priority);

                // Submit maintenance tasks every 2-4 seconds (low frequency)
                Thread.sleep(2000 + random.nextInt(2000));
            }
        } catch (InterruptedException e) {
            log.info("MaintenanceTaskProducer interrupted, stopping...");
        }

        log.info("MaintenanceTaskProducer finished. Created {} tasks", taskCounter);
    }
}
