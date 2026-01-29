package taskManager;

import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Subtask> subtasks = new HashMap<>();
    private Map<Integer, Epic> epics = new HashMap<>();
    private int idGen = 1;

    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public void removeAllTasks() {
        tasks.clear();
    }

    public void removeAllSubtasks() {
        subtasks.clear();
    }

    public void removeAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public List<Subtask> getSubtaskOfEpic(int id) {
        return //todo проверить если id не представлен в taskManager, то возвращать пустой список иначе возвращать список подзадач
    }

    public Task removeTask(int id) {
        return tasks.remove(id);
    }

    public Subtask removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = subtask.getEpic();
            if (epic != null) {
                epic.removeSubtask(id);
            }
        }
        return subtask;
    }

    public Epic removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                removeSubtask(subId);
            }
        }
        return epic;
    }

    private void updateEpicStatus(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        boolean allDone = true;
        boolean hasNew = true;

        for (Subtask sub : epic.getAllSubtask()) {
            if (sub == null || sub.getStatus() == Status.IN_PROGRESS) {
                allDone = false;
                hasNew = false;
            } else if (sub.getStatus() != Status.DONE) {
                allDone = false;
            } else if (sub.getStatus() != Status.NEW) {
                hasNew = false;
            }
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (hasNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    public boolean addTask(Task task) { // todo добавить проверку на null
        task.setId(idGen++);
        tasks.put(task.getId(), task);
        return true;
    }

    public boolean addSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpic().getId())) {
            return false;
        }
        Epic epic = subtask.getEpic();
        if (epic == null) {
            return false;
        }
        subtask.setId(idGen);
        subtasks.put(idGen++, subtask);
        epic.addSubtask(subtask);
        updateEpicStatus(epic);

        return true;
    }

    public boolean addEpic(Epic epic) {
        if (epic == null) {
            return false;
        }
        epic.setId(idGen);
        epics.put(idGen++, epic);
        return true;
    }

    public boolean updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return false;
        }
        tasks.put(task.getId(), task);
        return true;
    }

    public boolean updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())
                || !epics.containsKey(subtask.getEpic().getId())) {
            return false;
        }

        subtasks.put(subtask.getId(), subtask);
        return true;
    }

    public boolean updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) {
            return false;
        }

        epics.put(epic.getId(), epic);
        return true;
    }

}
