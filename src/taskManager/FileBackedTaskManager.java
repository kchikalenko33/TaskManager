package taskManager;

import history.HistoryManager;
import task.*;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static String historyToString(HistoryManager manager) {
        List<BaseTask> listTasks = manager.getHistory();

        if (manager.getHistory().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (BaseTask task : listTasks) {
            sb.append(task.getId()).append(",");
        }

        int start = sb.length();
        sb.delete(start - 1, start);

        return sb.toString();
    }

    public static List<Integer> historyFromString(String value) {
        List<Integer> historyIds = new ArrayList<>();

        if (value == null || value.isBlank()) {
            return historyIds;
        }

        for (String string : value.split(",")) {
            historyIds.add(Integer.parseInt(string));
        }

        return historyIds;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(file);
        List<String> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            while (bufferedReader.ready()) {
                lines.add(bufferedReader.readLine());
            }

            for (int i = 1; i < lines.size() - 3; i++) {
                BaseTask task = taskManager.fromString(lines.get(i));
                if (task instanceof Task) {
                    taskManager.tasks.put(task.getId(),(Task) task);
                } else if (task instanceof Subtask) {
                    taskManager.subtasks.put(task.getId(), (Subtask) task);
                } else if (task instanceof  Epic) {
                    taskManager.epics.put(task.getId(), (Epic) task);
                }
            }

            List<Integer> historyIds = historyFromString(lines.get(lines.size() - 2));

            for (Integer id : historyIds) {
                if (taskManager.tasks.containsKey(id)) {
                    taskManager.historyManager.add(taskManager.tasks.get(id));
                } else if (taskManager.subtasks.containsKey(id)) {
                    taskManager.historyManager.add(taskManager.subtasks.get(id));
                } else if (taskManager.epics.containsKey(id)) {
                    taskManager.historyManager.add(taskManager.epics.get(id));
                }
            }

            taskManager.idGen = Integer.parseInt(lines.getLast());
            return taskManager;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка");
        }

    }

    public void save() {
        // сохраняет текущее состояние менеджера в файл
        // метод должен вызываться во всех других методах, которые изменяет состояния менеджера
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write("id,type,name,status,description,epic\n");

            for (Task task : tasks.values()) {
                bufferedWriter.write(toString(task) + "\n");
            }

            for (Epic epic : epics.values()) {
                bufferedWriter.write(toString(epic) + "\n");
            }

            for (Subtask subtask : subtasks.values()) {
                bufferedWriter.write(toString(subtask) + "\n");
            }

            bufferedWriter.write("\n");

            bufferedWriter.write(historyToString(historyManager));

            bufferedWriter.write("\n" + idGen);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString(BaseTask task) {
        StringBuilder sb = new StringBuilder();
       // DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-DD")

        if (task instanceof Task) {
            sb.append(task.getId()).append(",").append(TypeTask.TASK.name()).append(",").append(task.getName())
                    .append(",").append(task.getStatus()).append(",").append(task.getDescription()).append(",")
                    .append(task.getStartTime().toString()).append(",").append(task.getDuration().toString());
        } else if (task instanceof Epic) {
            sb.append(task.getId()).append(",").append(TypeTask.EPIC.name()).append(",").append(task.getName())
                    .append(",").append(task.getStatus()).append(",").append(task.getDescription()).append(",")
                    .append(task.getStartTime().toString()).append(",").append(task.getDuration().toString());
        } else if (task instanceof Subtask) {
            sb.append(task.getId()).append(",").append(TypeTask.SUBTASK.name()).append(",").append(task.getName())
                    .append(",").append(task.getStatus()).append(",").append(task.getDescription()).append(",")
                    .append(task.getStartTime().toString()).append(",")
                    .append(task.getDuration().toString()).append(",").append(((Subtask) task).getEpicId());
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
        LocalDateTime startTime = LocalDateTime.parse(parts[5]); //todo распарсить по форматеру вида 2026-05-19T05:12PT4H
        Duration duration = Duration.parse(parts[6]);

        BaseTask task = null;

        switch (type) {
            case "TASK":
                task = new Task(name, description, id, status, startTime, duration);
                break;
            case "EPIC":
                task = new Epic(name, description, id, status, startTime, duration);
                break;
            case "SUBTASK":
                int epicId = Integer.parseInt(parts[7]);
                task = new Subtask(name, description, id, status, epicId, startTime,duration);
                break;
        }

        return task;
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public Task removeTask(int id) {
        Task task = super.removeTask(id);
        save();

        return task;
    }

    @Override
    public Subtask removeSubtask(int id) {
        Subtask subtask = super.removeSubtask(id);
        save();

        return subtask;
    }

    @Override
    public Epic removeEpic(int id) {
        Epic epic = super.removeEpic(id);
        save();

        return epic;
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        super.updateEpicStatus(epic);
        save();
    }

    @Override
    public boolean addTask(Task task) {
        boolean temp = super.addTask(task);
        save();

        return temp;
    }

    @Override
    public boolean addSubtask(Subtask subtask) {
        boolean temp = super.addSubtask(subtask);
        save();

        return temp;
    }

    @Override
    public boolean addEpic(Epic epic) {
        boolean temp = super.addEpic(epic);
        save();

        return temp;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean temp = super.updateTask(task);
        save();

        return temp;
    }

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();

        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        save();

        return subtask;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();

        return epic;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        boolean temp = super.updateSubtask(subtask);
        save();

        return temp;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean temp = super.updateEpic(epic);
        save();

        return temp;
    }
}
