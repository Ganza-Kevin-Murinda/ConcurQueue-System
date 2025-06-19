package model;

public enum ETaskStatus {
    SUBMITTED,    // Task created and submitted to queue
    PROCESSING,   // Task picked up by worker thread
    COMPLETED,    // Task processed successfully
    FAILED,       // Task failed permanently (after all retries)
    RETRYING      // Task failed but will be retried
}
