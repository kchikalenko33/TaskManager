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

    @Override
    public Duration getDuration() {
        if (subtasks.isEmpty()) {
            return duration;
        }

        Duration resDuration = Duration.ZERO;

        for (Subtask subtask : subtasks.values()) {
            resDuration = resDuration.plus(subtask.getDuration());
        }

        return resDuration;
    }

    @Override
    public LocalDateTime getStartTime() {
        if (subtasks.isEmpty()) {
            return startTime;
        }

        LocalDateTime resStartTime = LocalDateTime.MAX;

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getStartTime() != null && subtask.getStartTime().isBefore(resStartTime)) {
                resStartTime = subtask.getStartTime();
            }
        }

        return resStartTime == LocalDateTime.MAX ? startTime : resStartTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        if (subtasks.isEmpty()) {
            return super.getEndTime();
        }

        LocalDateTime resEndTime = LocalDateTime.MIN;

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEndTime() != null && subtask.getEndTime().isAfter(resEndTime)) {
                resEndTime = subtask.getEndTime();
            }
        }

        return resEndTime == LocalDateTime.MIN ? super.getEndTime() : resEndTime;
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