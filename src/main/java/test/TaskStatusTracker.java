package test;

import model.ETaskStatus;
import model.TaskStatusInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TaskStatusTracker {
    private static final Logger logger = LoggerFactory.getLogger(TaskStatusTracker.class);
    private final Map<UUID, TaskStatusInfo> taskStatusMap;

    public TaskStatusTracker() {
        taskStatusMap = new HashMap<>();
    }

    public void updateTaskStatus(UUID taskId, ETaskStatus status, String threadName) {
        TaskStatusInfo statusInfo = taskStatusMap.get(taskId);
        if (statusInfo == null) { // create task
            statusInfo = new TaskStatusInfo(taskId, status, threadName);
            taskStatusMap.put(taskId, statusInfo);
        } else { // update task
            statusInfo.setStatus(status);
            statusInfo.setProcessingThreadName(threadName);
            statusInfo.setStatusUpdatedAt(java.time.Instant.now());
        }

        logger.info("Task {} status updated to {} by thread {}",
                taskId.toString().substring(0, 8), status, threadName);
    }

    public void printAllTaskStatuses() {
        logger.info("=== Current Task Statuses ===");
        taskStatusMap.forEach((taskId, status) -> {
            logger.info("Task {}: {} (Thread: {}, Updated: {})",
                    taskId.toString().substring(0, 8),
                    status.getStatus(),
                    status.getProcessingThreadName(),
                    status.getStatusUpdatedAt());
        });
    }

    public int getTaskCount() {
        return taskStatusMap.size();
    }
}
