package taskManager;

import exception.EpicNotFoundException;
import exception.IntersectionException;
import exception.SubtaskNotFoundException;
import exception.TaskNotFoundException;
import history.HistoryManager;
import task.*;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Subtask> subtasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected HistoryManager historyManager = Managers.getDefaultHistory();
    protected TreeSet<BaseTask> priorityTask = new TreeSet<>(Comparator.nullsLast(
            Comparator.comparing(BaseTask::getStartTime)));
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

    @Override
    public List<BaseTask> getHistory() {
        return historyManager.getHistory();
    }

    public List<BaseTask> getPrioritizedTasks() {
        return new ArrayList<>(priorityTask);
    }

    public boolean hasIntersection(BaseTask task) {
        LocalDateTime start = task.getStartTime();
        LocalDateTime end = task.getEndTime();

        if (start == null || end == null) {
            return false;
        }

        for (BaseTask existingTask : priorityTask) {
            if (existingTask.getId() == task.getId()) {
                // пропускаем саму себя при обновлении
                continue;
            }

            LocalDateTime existingStart = existingTask.getStartTime();
            LocalDateTime existingEnd = existingTask.getEndTime();

            if (existingStart == null || existingEnd == null) {
                continue;
            }


            boolean isStartInside = !start.isBefore(existingStart) && !start.isAfter(existingEnd);
            boolean isEndInside = !end.isBefore(existingStart) && !end.isAfter(existingEnd);
            boolean isSurrounding = start.isBefore(existingStart) && end.isAfter(existingEnd);

            if (isStartInside || isEndInside || isSurrounding) {
                return true;
            }
        }

        return false;
    }

    public void removeAllTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
            priorityTask.remove(task);
        }

        tasks.clear();
    }

    public void removeAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
            priorityTask.remove(subtask);
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

        if (task == null) throw new TaskNotFoundException(String.format("Задача с id '%d' не найдена", id));

        if (tasks.containsKey(id)) {
            historyManager.add(task);
        }

        return task;
    }

    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);

        if (subtask == null) throw new SubtaskNotFoundException(String.format("Подзадача с id '%d' не найдена", id));

        if (subtasks.containsKey(id)) {
            historyManager.add(subtask);
        }

        return subtask;
    }

    public Epic getEpic(int id) {
        Epic epic = epics.get(id);

        if (epic == null) throw new EpicNotFoundException(String.format("Эпик с id '%d' не найден", id));

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
        if (!tasks.containsKey(id)) throw new TaskNotFoundException("Нет задачи");

        historyManager.remove(id);
        Task task = tasks.get(id);
        if (task != null) priorityTask.remove(task);
        return tasks.remove(id);
    }

    public Subtask removeSubtask(int id) {
        historyManager.remove(id);
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);

            if (epic != null) epics.get(epicId).removeSubtask(id);

            priorityTask.remove(subtask);
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

        if (hasIntersection(task)) {
            throw new IntersectionException("Даты добавляемой задачи пересекаются с имеющеммся");
        }

        task.setId(idGen++);
        tasks.put(task.getId(), task);
        priorityTask.add(task);

        return true;
    }

    public boolean addSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            return false;
        }

        if (hasIntersection(subtask)) {
            throw new IntersectionException("Даты добавляемой задачи пересекаются с имеющеммся");
        }

        Epic epic = epics.get(subtask.getEpicId());

        if (epic == null) {
            return false;
        }

        subtask.setId(idGen);
        subtasks.put(idGen++, subtask);
        epic.addSubtask(subtask);
        updateEpicStatus(epic);
        priorityTask.add(subtask);

        return true;
    }

    public boolean addEpic(Epic epic) {
        if (epic == null) {
            return false;
        }

        if (hasIntersection(epic)) {
            throw new IntersectionException("Даты добавляемой задачи пересекаются с имеющеммся");
        }

        epic.setId(idGen);
        epics.put(idGen++, epic);
        return true;
    }

    public boolean patchTask(BaseTask task) {
        if (task instanceof Task) {
            Task task1 = tasks.get(task.getId());
            if (task1 == null) {
                return false;
            }
            task1.setName(task.getName());
            task1.setStatus(task.getStatus());
            task1.setDescription(task.getDescription());
            task1.setDuration(task.getDuration());
            task1.setStartTime(task.getStartTime());
            return true;
        } else if (task instanceof Subtask) {
            Subtask subtask = subtasks.get(task.getId());
            if (subtask == null) {
                return false;
            }
            subtask.setName(task.getName());
            subtask.setStatus(task.getStatus());
            subtask.setDescription(task.getDescription());
            subtask.setDuration(task.getDuration());
            subtask.setStartTime(task.getStartTime());
            return true;
        }
        return false;
    }

    public boolean updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return false;
        }

        if (hasIntersection(task)) {
            throw new IntersectionException("Даты добавляемой задачи пересекаются с имеющеммся");
        }

        // tasks.put(task.getId(), task);
        return patchTask(task);
    }

    public boolean updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())
                || !epics.containsKey(subtask.getEpicId())) {
            return false;
        }

        if (hasIntersection(subtask)) {
            throw new IntersectionException("Даты добавляемой задачи пересекаются с имеющеммся");
        }

        // subtasks.put(subtask.getId(), subtask);
        return patchTask(subtask);
    }

    public boolean updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) {
            return false;
        }

//        if (hasIntersection(epic)) {
//            throw new IntersectionException("Даты добавляемой задачи пересекаются с имеющеммся");
//        }

        epics.put(epic.getId(), epic);
        return true;
    }

    HistoryManager getHistoryManager() {
        return historyManager;
    }
}
