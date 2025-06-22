package monitor;

import lombok.extern.slf4j.Slf4j;
import prototype.MainApp;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Simple Monitoring Thread
 * Logs system metrics every 5 seconds:
 * - Queue size
 * - Active thread count
 * - Processed task count
 * - Additional useful metrics
 */
@Slf4j
public class MonitorThread implements Runnable {

    private final ThreadPoolExecutor workerPool;
    private final int monitoringIntervalSeconds;

    public MonitorThread(ThreadPoolExecutor workerPool) {
        this.workerPool = workerPool;
        this.monitoringIntervalSeconds = 5; // Log every 5 seconds
    }

    @Override
    public void run() {
        log.info("MonitoringThread started - reporting every {} seconds", monitoringIntervalSeconds);

        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Sleep first, then report
                Thread.sleep(monitoringIntervalSeconds * 1000L);

                // Collect metrics
                logSystemMetrics();
            }
        } catch (InterruptedException e) {
            log.info("MonitoringThread interrupted, stopping...");
        }

        // Final report
        log.info("=== FINAL MONITORING REPORT ===");
        logSystemMetrics();
        log.info("MonitoringThread finished");
    }

    private void logSystemMetrics() {
        // Core metrics requested in Step 7
        int queueSize = MainApp.getTaskQueue().size();
        int activeThreadCount = workerPool.getActiveCount();
        int processedCount = MainApp.getTotalTasksProcessed().get();

        // Additional useful metrics
        int submittedCount = MainApp.getTotalTasksSubmitted().get();
        int retriedCount = MainApp.getTotalTasksRetried().get();
        int totalThreadPoolSize = workerPool.getPoolSize();
        long completedTaskCount = workerPool.getCompletedTaskCount();

        // Status breakdown from TaskStatusTracker
        String statusSummary = getTaskStatusBreakdown();

        // Log the monitoring report
        log.info("📊 === SYSTEM MONITORING REPORT ===");
        log.info("🔢 Queue Size: {} | Active Threads: {}/{}",
                queueSize, activeThreadCount, totalThreadPoolSize);
        log.info("✅ Tasks Processed: {} | Submitted: {} | Retried: {}",
                processedCount, submittedCount, retriedCount);
        log.info("🏭 ThreadPool Completed Tasks: {}", completedTaskCount);
        log.info("📈 Task Status Breakdown: {}", statusSummary);
        log.info("💡 Processing Rate: {:.1f}% | Retry Rate: {:.1f}%",
                calculateProcessingRate(processedCount, submittedCount),
                calculateRetryRate(retriedCount, submittedCount));
        log.info("📊 ===============================");
    }

    private String getTaskStatusBreakdown() {
        var statusTracker = MainApp.getStatusTracker();
        var taskStatusMap = statusTracker.getTaskStatusMap();

        if (taskStatusMap == null) {
            return "Status tracking unavailable";
        }

        long submitted = taskStatusMap.values().stream()
                .filter(status -> status.getStatus() == model.ETaskStatus.SUBMITTED).count();
        long processing = taskStatusMap.values().stream()
                .filter(status -> status.getStatus() == model.ETaskStatus.PROCESSING).count();
        long completed = taskStatusMap.values().stream()
                .filter(status -> status.getStatus() == model.ETaskStatus.COMPLETED).count();
        long retrying = taskStatusMap.values().stream()
                .filter(status -> status.getStatus() == model.ETaskStatus.RETRYING).count();
        long failed = taskStatusMap.values().stream()
                .filter(status -> status.getStatus() == model.ETaskStatus.FAILED).count();

        return String.format("S:%d P:%d C:%d R:%d F:%d",
                submitted, processing, completed, retrying, failed);
    }

    private double calculateProcessingRate(int processed, int submitted) {
        return submitted > 0 ? (processed * 100.0 / submitted) : 0.0;
    }

    private double calculateRetryRate(int retried, int submitted) {
        return submitted > 0 ? (retried * 100.0 / submitted) : 0.0;
    }
}
