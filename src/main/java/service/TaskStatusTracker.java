package service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.ETaskStatus;
import model.TaskStatusInfo;
import prototype.MainApp;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Task Status Tracker with retry logic support
 * Tracks all task statuses including RETRYING and FAILED states
 */
@Getter
@Slf4j
public class TaskStatusTracker {

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

        log.info("Task {} status updated to {} by thread {}",
                taskId.toString().substring(0, 8), status, threadName);
    }

    public void updateTaskStatusWithError(UUID taskId, ETaskStatus status, String threadName, String errorMessage) {
        TaskStatusInfo statusInfo = taskStatusMap.get(taskId);
        if (statusInfo == null) {
            statusInfo = new TaskStatusInfo(taskId, status, threadName);
            taskStatusMap.put(taskId, statusInfo);
        } else {
            statusInfo.setStatus(status);
            statusInfo.setProcessingThreadName(threadName);
            statusInfo.setStatusUpdatedAt(java.time.Instant.now());
        }

        statusInfo.setErrorMessage(errorMessage);

        log.error("Task {} status updated to {} by thread {} - Error: {}",
                taskId.toString().substring(0, 8), status, threadName, errorMessage);
    }

    public boolean canRetry(UUID taskId) {
        TaskStatusInfo statusInfo = taskStatusMap.get(taskId);
        return statusInfo != null && statusInfo.getRetryCount() < MainApp.getMaxRetryAttempts();
    }

    public void incrementRetryCount(UUID taskId, String threadName) {
        TaskStatusInfo statusInfo = taskStatusMap.get(taskId);
        if (statusInfo != null) {
            statusInfo.incrementRetryCount();
            statusInfo.setStatus(ETaskStatus.RETRYING);
            statusInfo.setProcessingThreadName(threadName);

            log.warn("Task {} retry count incremented to {} by thread {}",
                    taskId.toString().substring(0, 8), statusInfo.getRetryCount(), threadName);
        }
    }

    public void markTaskAsPermanentlyFailed(UUID taskId, String threadName, String finalError) {
        TaskStatusInfo statusInfo = taskStatusMap.get(taskId);
        if (statusInfo != null) {
            statusInfo.setStatus(ETaskStatus.FAILED);
            statusInfo.setProcessingThreadName(threadName);
            statusInfo.setStatusUpdatedAt(java.time.Instant.now());
            statusInfo.setErrorMessage(finalError);

            log.error("Task {} permanently FAILED after {} attempts by thread {} - Final Error: {}",
                    taskId.toString().substring(0, 8), statusInfo.getRetryCount(), threadName, finalError);
        }
    }

    public TaskStatusInfo getTaskStatus(UUID taskId) {
        return taskStatusMap.get(taskId);
    }

    public int getTaskCount() {
        return taskStatusMap.size();
    }

    public void printStatusSummary() {
        log.info("=== TASK STATUS SUMMARY ===");

        long submitted = taskStatusMap.values().stream()
                .filter(status -> status.getStatus() == ETaskStatus.SUBMITTED).count();
        long processing = taskStatusMap.values().stream()
                .filter(status -> status.getStatus() == ETaskStatus.PROCESSING).count();
        long completed = taskStatusMap.values().stream()
                .filter(status -> status.getStatus() == ETaskStatus.COMPLETED).count();
        long retrying = taskStatusMap.values().stream()
                .filter(status -> status.getStatus() == ETaskStatus.RETRYING).count();
        long failed = taskStatusMap.values().stream()
                .filter(status -> status.getStatus() == ETaskStatus.FAILED).count();

        log.info("SUBMITTED: {}, PROCESSING: {}, COMPLETED: {}, RETRYING: {}, FAILED: {}",
                submitted, processing, completed, retrying, failed);
        log.info("Total tasks tracked: {}", taskStatusMap.size());
    }

    public void printFailedTasks() {
        List<TaskStatusInfo> failedTasks = taskStatusMap.values().stream()
                .filter(status -> status.getStatus() == ETaskStatus.FAILED)
                .toList();

        if (!failedTasks.isEmpty()) {
            log.info("=== FAILED TASKS DETAILS ===");
            failedTasks.forEach(task -> {
                log.info("Task {}: Retries: {}, Last Error: {}, Last Thread: {}",
                        task.getTaskId().toString().substring(0, 8),
                        task.getRetryCount(),
                        task.getErrorMessage(),
                        task.getProcessingThreadName());
            });
        } else {
            log.info("=== NO PERMANENTLY FAILED TASKS ===");
        }
    }

    public void printAllTaskStatuses() {
        log.info("=== DETAILED TASK STATUSES ===");
        taskStatusMap.forEach((taskId, status) -> {
            String retryInfo = status.getRetryCount() > 0 ?
                    String.format(" (Retries: %d)", status.getRetryCount()) : "";
            String errorInfo = status.getErrorMessage() != null ?
                    String.format(" [Error: %s]", status.getErrorMessage()) : "";

            log.info("Task {}: {} (Thread: {}, Updated: {}){}{}",
                    taskId.toString().substring(0, 8),
                    status.getStatus(),
                    status.getProcessingThreadName(),
                    status.getStatusUpdatedAt(),
                    retryInfo,
                    errorInfo);
        });
    }
}
