package taskManager;

import history.HistoryManager;
import history.InMemoryTaskManager;
import task.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static String historyToString(HistoryManager manager) {
        List<BaseTask> listTasks = manager.getHistory();
        StringBuilder sb = new StringBuilder();

        for (BaseTask task : listTasks) {
            sb.append(task.getId()).append(",");
        }

        int start = sb.length();
        sb.delete(start - 1, start);

        return sb.toString();
    }

    public void save() {
        // сохраняет текущее состояние менеджера в файл
        // метод должен вызываться во всех других методах, которые изменяет состояния менеджера
    }

    public String toString(BaseTask task) {
        StringBuilder sb = new StringBuilder();

        if (task instanceof Task) {
            sb.append(task.getId()).append(",").append(TypeTask.TASK.name()).append(",").append(task.getName())
                    .append(",").append(task.getStatus()).append(",").append(task.getDescription()).append(",");
        } else if (task instanceof Epic) {
            sb.append(task.getId()).append(",").append(TypeTask.EPIC.name()).append(",").append(task.getName())
                    .append(",").append(task.getStatus()).append(",").append(task.getDescription()).append(",");
        } else if (task instanceof Subtask) {
            sb.append(task.getId()).append(",").append(TypeTask.SUBTASK.name()).append(",").append(task.getName())
                    .append(",").append(task.getStatus()).append(",").append(task.getDescription()).append(",")
                    .append(((Subtask) task).getEpicId());
        }

        return sb.toString();
    }

    public BaseTask fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String[] parts = value.split(",", -1);

        if (parts.length < 5) {
            throw new IllegalArgumentException("Невалидный формат строки");
        }

        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        BaseTask task = null;

        switch (type) {
            case "TASK":
                task = new Task(name, description, id, status);
                break;
            case "EPIC":
                task = new Epic(name, description, id, status);
                break;
            case "SUBTASK":
                int epicId = Integer.parseInt(parts[5]);
                task = new Subtask(name,description,id,status, epicId);
                break;
        }

        return task;
    }

}
