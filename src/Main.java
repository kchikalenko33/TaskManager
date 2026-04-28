import history.InMemoryTaskManager;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;
import taskManager.FileBackedTaskManager;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        FileBackedTaskManager taskManagerBack = new FileBackedTaskManager(new File("C:/data.csv"));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(1).append(",").append("2").append(",");
        int start = stringBuilder.length();

        System.out.println(stringBuilder.delete(start -1, start));

        Task task1 = new Task("Задача1", "Описание1", Status.NEW);
        Task task2 = new Task("Задача2", "Описание2", Status.NEW);
        Epic epic1 = new Epic("Эпик1", "Описание1", Status.NEW);
        Epic epic2 = new Epic("Эпик2", "Описание2", Status.NEW);
        Subtask subtask1 = new Subtask("подзадача1", "Описание1", epic1.getId(), Status.NEW);
        Subtask subtask2 = new Subtask("подзадача2", "Описание2", epic1.getId(), Status.NEW);
        Subtask subtask3 = new Subtask("подзадача2", "Описание3", epic1.getId(), Status.NEW);

        System.out.println(taskManagerBack.toString(task1));
        System.out.println(taskManagerBack.toString(subtask1));
        System.out.println(taskManagerBack.toString(epic1));


        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        taskManager.getTask(task1.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getTask(task2.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubtask(subtask1.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubtask(subtask2.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubtask(subtask3.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getEpic(epic1.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getEpic(epic2.getId());
        System.out.println(taskManager.getHistory());

        taskManager.removeTask(task2.getId());
        System.out.println(taskManager.getHistory());

        taskManager.removeEpic(1);
        System.out.println(taskManager.getHistory());
    }
}
