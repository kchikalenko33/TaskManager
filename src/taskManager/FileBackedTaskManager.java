package taskManager;

import exception.FileReadException;
import exception.FileSaveException;
import exception.TypeTaskNotFoundException;
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

    // превращение списка задач из истории в строку id1,id2,id3
    public static String historyToString(HistoryManager manager) {
        List<BaseTask> listTasks = manager.getHistory();

        if (listTasks.isEmpty()) {
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

    //превращение строки с id задач в историю со списком задач
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
        } catch (IOException e) {
            throw new FileReadException("Ошибка чтения файла");
        }

        if (lines.isEmpty()) {
            return taskManager;
        }

        int emptyLineIndex = lines.indexOf("");

        if (emptyLineIndex == -1) {
            emptyLineIndex = lines.size() - 2;
        }

        // Восстановление задач
        for (int i = 1; i < emptyLineIndex; i++) {
            BaseTask baseTask = taskManager.fromString(lines.get(i));
            if (baseTask == null) {
                continue;
            }
            if (baseTask instanceof Task task) {
                taskManager.tasks.put(baseTask.getId(), task);
                taskManager.priorityTask.add(task);
            } else if (baseTask instanceof Subtask subtask) {
                taskManager.subtasks.put(baseTask.getId(), subtask);
                taskManager.priorityTask.add(subtask);
            } else if (baseTask instanceof Epic epic) {
                taskManager.epics.put(baseTask.getId(), epic);
            }
        }

        // Восстановление связи эпик -> подзадачи
        for (Subtask subtask : taskManager.subtasks.values()) {
            Epic epic = taskManager.epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.addSubtask(subtask);
            }
        }

        // Пересчёт статусов эпиков
        for (Epic epic : taskManager.epics.values()) {
            taskManager.updateEpicStatus(epic);
        }

        // Восстановление истории
        if (emptyLineIndex + 1 < lines.size()) {
            List<Integer> historyIds = historyFromString(lines.get(emptyLineIndex + 1));

            for (Integer id : historyIds) {
                if (taskManager.tasks.containsKey(id)) {
                    taskManager.historyManager.add(taskManager.tasks.get(id));
                } else if (taskManager.subtasks.containsKey(id)) {
                    taskManager.historyManager.add(taskManager.subtasks.get(id));
                } else if (taskManager.epics.containsKey(id)) {
                    taskManager.historyManager.add(taskManager.epics.get(id));
                }
            }
        }

        // Восстановление генератора id
        if (!lines.isEmpty()) {
            String lastLine = lines.get(lines.size() - 1);
            if (lastLine != null && !lastLine.isBlank()) {
                taskManager.idGen = Integer.parseInt(lastLine);
            }
        }

        return taskManager;
    }

    public void save() {
        // сохраняет текущее состояние менеджера в файл
        // метод должен вызываться во всех других методах, которые изменяет состояния менеджера
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write("id,type,name,status,description,startTime,duration,epicId\n");

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
            throw new FileSaveException("Ошибка сохранения файла");
        }
    }

    // преобразование одной задачи в строку
    public String toString(BaseTask task) {
        if (task instanceof Task) {
            return task.getId() + "," +
                    TypeTask.TASK.name() + "," +
                    task.getName() + "," +
                    task.getStatus() + "," +
                    task.getDescription() + "," +
                    task.getStartTime() + "," +
                    task.getDuration();
        } else if (task instanceof Epic) {
            return task.getId() + "," +
                    TypeTask.EPIC.name() + "," +
                    task.getName() + "," +
                    task.getStatus() + "," +
                    task.getDescription() + "," +
                    task.getStartTime() + "," +
                    task.getDuration();
        } else if (task instanceof Subtask subtask) {
            return subtask.getId() + "," +
                    TypeTask.SUBTASK.name() + "," +
                    subtask.getName() + "," +
                    subtask.getStatus() + "," +
                    subtask.getDescription() + "," +
                    subtask.getStartTime() + "," +
                    subtask.getDuration() + "," +
                    subtask.getEpicId();
        }

        throw new TypeTaskNotFoundException("Неизвестный тип задачи");
    }

    //преобразование строки в задачу
    public BaseTask fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String[] parts = value.split(",", -1);

        if (parts.length < 7) {
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
                if (parts.length < 8) {
                    throw new IllegalArgumentException("Для подзадачи не указан epicId");
                }
                int epicId = Integer.parseInt(parts[7]);
                task = new Subtask(name, description, id, status, epicId, startTime, duration);
                break;
            default:
                throw new TypeTaskNotFoundException("Неизвестный тип задачи: " + type);
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
