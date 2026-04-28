package task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Epic extends BaseTask {
    private Map<Integer, Subtask> subtasks = new HashMap<>();

    public Epic(String name, String description, int id, Status status) {
        super(name, description, id, status);
    }

    public Epic(String name, String description, Status status) {
        super(name, description, status);
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
