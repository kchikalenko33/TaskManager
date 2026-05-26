package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Epic extends BaseTask {
    private Map<Integer, Subtask> subtasks = new HashMap<>();
    private LocalDateTime endTime;

    public Epic(String name, String description, int id, Status status) {
        super(name, description, id, status);
    }

    public Epic(String name, String description, int id, Status status, LocalDateTime startTime, Duration duration) {
        super(name, description, id, status, startTime, duration);
    }

    public Epic(String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
    }

    public Duration getDuration() {
        Duration resDuration = Duration.ZERO;

        for (Subtask subtask : subtasks.values()) {
            resDuration = resDuration.plus(duration);
        }

        return resDuration;
    }

    public LocalDateTime getStartTime() {
        LocalDateTime resStartTime = LocalDateTime.MAX;

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getStartTime().isBefore(resStartTime)) {
                resStartTime = subtask.getStartTime();
            }
        }

        return resStartTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        if (endTime == null) endTime = LocalDateTime.MIN;

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEndTime().isAfter(endTime)) {
                endTime = subtask.getEndTime();
            }
        }

        return endTime;
    }

    public List<Subtask> getAllSubtask() {
        return new ArrayList<>(subtasks.values());
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtasks.keySet());
    }

    public void addSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    public void removeSubtask(int subtaskId) {
        subtasks.remove(subtaskId);
    }

}
