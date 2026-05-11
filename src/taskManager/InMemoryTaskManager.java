package taskManager;

import history.HistoryManager;
import task.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Subtask> subtasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected HistoryManager historyManager = Managers.getDefaultHistory();
    protected int idGen = 1;

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
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
        }

        tasks.clear();
    }

    public void removeAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }

        subtasks.clear();

        for (Epic epic : epics.values()) {
            updateEpicStatus(epic);
        }
    }

    public void removeAllEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }

        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }

        epics.clear();
        subtasks.clear();
    }

    public Task getTask(int id) {
        Task task = tasks.get(id);

        if (tasks.containsKey(id)) {
            historyManager.add(task);
        }

        return task;
    }

    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);

        if (subtasks.containsKey(id)) {
            historyManager.add(subtask);
        }

        return subtask;
    }

    public Epic getEpic(int id) {
        Epic epic = epics.get(id);

        if (epics.containsKey(id)) {
            historyManager.add(epic);
        }

        return epic;
    }

    public List<Subtask> getSubtaskOfEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(epic.getAllSubtask());
    }

    public Task removeTask(int id) {
        historyManager.remove(id);
        return tasks.remove(id);
    }

    public Subtask removeSubtask(int id) {
        historyManager.remove(id);
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            int epic = subtask.getEpicId();
            epics.get(epic).removeSubtask(id);
                //epic.removeSubtask(id);

        }
        return subtask;
    }

    public Epic removeEpic(int id) {
        historyManager.remove(id);
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                removeSubtask(subId);
            }
        }
        return epic;
    }

    public void updateEpicStatus(Epic epic) {
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

    public boolean addTask(Task task) {
        if (task == null) {
            return false;
        }
        task.setId(idGen++);
        tasks.put(task.getId(), task);

        return true;
    }

    public boolean addSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            return false;
        }

        Epic epic = epics.get(subtask.getEpicId());
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
                || !epics.containsKey(subtask.getEpicId())) {
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

    @Override
    public List<BaseTask> getHistory() {
        return historyManager.getHistory();
    }
}
