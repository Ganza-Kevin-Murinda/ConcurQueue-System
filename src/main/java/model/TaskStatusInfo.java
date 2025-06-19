package model;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatusInfo {
    private UUID taskId;
    private ETaskStatus status;
    private String processingThreadName;
    private Instant statusUpdatedAt;
    private int retryCount;
    private String errorMessage;

    public TaskStatusInfo(UUID taskId, ETaskStatus status, String threadName) {
        this.taskId = taskId;
        this.status = status;
        this.processingThreadName = threadName;
        this.statusUpdatedAt = Instant.now();
        this.retryCount = 0;
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.statusUpdatedAt = Instant.now();
    }
}
