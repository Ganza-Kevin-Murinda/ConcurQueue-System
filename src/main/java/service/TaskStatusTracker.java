package service;

import model.ETaskStatus;
import model.TaskStatusInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TaskStatusTracker {
    private static final Logger logger = LoggerFactory.getLogger(TaskStatusTracker.class);
    private final ConcurrentHashMap<UUID, TaskStatusInfo> taskStatusMap;

    public TaskStatusTracker() {
        taskStatusMap = new ConcurrentHashMap<>();
    }

    public void updateTaskStatus(UUID taskId, ETaskStatus status, String threadName) {
        TaskStatusInfo statusInfo = taskStatusMap.get(taskId);
        if (statusInfo == null) {
            // Create new status info
            statusInfo = new TaskStatusInfo(taskId, status, threadName);
            taskStatusMap.put(taskId, statusInfo);
        } else {
            // Update existing status info
            statusInfo.setStatus(status);
            statusInfo.setProcessingThreadName(threadName);
            statusInfo.setStatusUpdatedAt(java.time.Instant.now());
        }

        logger.info("Task {} status updated to {} by thread {}",
                taskId.toString().substring(0, 8), status, threadName);
    }

    public void printAllTaskStatuses() {
        logger.info("=== Current Task Statuses ===");
        taskStatusMap.forEach((taskId, status) -> logger.info("Task {}: {} (Thread: {}, Updated: {})",
                taskId.toString().substring(0, 8),
                status.getStatus(),
                status.getProcessingThreadName(),
                status.getStatusUpdatedAt()));
    }

    public int getTaskCount() {
        return taskStatusMap.size();
    }

    public TaskStatusInfo getTaskStatus(UUID taskId) {
        return taskStatusMap.get(taskId);
    }
}
