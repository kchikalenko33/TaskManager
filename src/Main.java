import taskManager.InMemoryTaskManager;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;
import taskManager.FileBackedTaskManager;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        FileBackedTaskManager taskManagerBack = new FileBackedTaskManager(new File("C:\\Users\\chika\\IdeaProjects\\TaskManager\\src\\datas.csv"));


        Task task1 = new Task("Задача1", "Описание1", Status.NEW, LocalDateTime.of(2026, 05, 19, 5, 12), Duration.ofHours(4));
        Task task2 = new Task("Задача2", "Описание2", Status.NEW, LocalDateTime.of(2026, 04, 29, 5, 12), Duration.ofHours(2));
        Epic epic1 = new Epic("Эпик1", "Описание1", Status.NEW, LocalDateTime.of(2026, 05, 9, 5, 12), Duration.ofHours(5));
        Epic epic2 = new Epic("Эпик2", "Описание2", Status.NEW, LocalDateTime.of(2026, 05, 13, 5, 12), Duration.ofHours(1));
        taskManagerBack.addEpic(epic1);
        taskManagerBack.addTask(task1);
        taskManagerBack.addTask(task2);
        taskManagerBack.addEpic(epic2);
        Subtask subtask1 = new Subtask("подзадача1", "Описание1", epic1.getId(), Status.NEW, LocalDateTime.of(2026, 5, 19, 5, 12), Duration.ofHours(4));
        Subtask subtask2 = new Subtask("подзадача2", "Описание2", epic1.getId(), Status.NEW, LocalDateTime.of(2026, 4, 19, 5, 12), Duration.ofHours(2));
        Subtask subtask3 = new Subtask("подзадача2", "Описание3", epic1.getId(), Status.NEW, LocalDateTime.of(2026, 5, 10, 5, 12), Duration.ofHours(1));
        taskManagerBack.addSubtask(subtask1);
        taskManagerBack.addSubtask(subtask2);
        taskManagerBack.addSubtask(subtask3);

        System.out.println(taskManagerBack.toString(task1));
        System.out.println(taskManagerBack.toString(subtask1));
        System.out.println(taskManagerBack.toString(epic1));

        taskManagerBack.getTask(task1.getId());
        taskManagerBack.getSubtask(subtask1.getId());
        taskManagerBack.getEpic(epic1.getId());
        taskManagerBack.getEpic(epic2.getId());
        taskManagerBack.getTask(task1.getId());

        taskManagerBack.save();

        FileBackedTaskManager taskManager1 = FileBackedTaskManager.loadFromFile
                (new File("C:\\Users\\chika\\IdeaProjects\\TaskManager\\src\\datas.csv"));

        System.out.println();
    }
}
