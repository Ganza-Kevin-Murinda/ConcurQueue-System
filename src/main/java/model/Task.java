package model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task implements Comparable<Task> {
    private UUID id;
    private String name;
    private int priority;
    private Instant createdTimestamp;
    private String payload;

    // Constructor for easy task creation (auto-generates ID and timestamp)
    public Task(String name, int priority, String payload) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.priority = priority;
        this.createdTimestamp = Instant.now();
        this.payload = payload;
    }

    @Override
    public int compareTo(Task other) {

        int priorityComparison = Integer.compare(this.priority, other.priority);
        if (priorityComparison != 0) {
            return priorityComparison;
        }

        return this.createdTimestamp.compareTo(other.createdTimestamp);
    }

    @Override
    public String toString() {
        return String.format("Task{id=%s, name='%s', priority=%d, created=%s, payload='%s'}",
                id.toString().substring(0, 8) + "...", name, priority, createdTimestamp, payload);
    }
}