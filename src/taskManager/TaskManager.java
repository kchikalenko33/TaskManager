package taskManager;

import task.*;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    List<Task> getTasks();

    List<Subtask> getSubtasks();

    List<Epic> getEpics();

    void removeAllTasks();

    void removeAllSubtasks();

    void removeAllEpics();

    Task getTask(int id);

    Subtask getSubtask(int id);
    Epic getEpic(int id);

    List<Subtask> getSubtaskOfEpic(int id);

    Task removeTask(int id);

    Subtask removeSubtask(int id);

    Epic removeEpic(int id);
    void updateEpicStatus(Epic epic);

    boolean addTask(Task task);

    boolean addSubtask(Subtask subtask);

    boolean addEpic(Epic epic);

    boolean updateTask(Task task);

    boolean updateSubtask(Subtask subtask);

    boolean updateEpic(Epic epic);

    List<BaseTask> getHistory();
}
