package task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Task extends BaseTask{
    public Task(String name, String description, int id, Status status) {
        super(name, description, id, status);
    }

    public Task(String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
    }

    public Task(String name, String description, int id, Status status, LocalDateTime startTime, Duration duration) {
        super(name, description, id, status, startTime, duration);
    }



}
