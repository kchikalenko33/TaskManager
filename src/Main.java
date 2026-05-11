import taskManager.InMemoryTaskManager;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;
import taskManager.FileBackedTaskManager;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        FileBackedTaskManager taskManagerBack = new FileBackedTaskManager(new File("C:\\Users\\chika\\IdeaProjects\\TaskManager\\src\\datas.csv"));


        Task task1 = new Task("Задача1", "Описание1", Status.NEW);
        Task task2 = new Task("Задача2", "Описание2", Status.NEW);
        Epic epic1 = new Epic("Эпик1", "Описание1", Status.NEW);
        Epic epic2 = new Epic("Эпик2", "Описание2", Status.NEW);
        taskManagerBack.addEpic(epic1);
        taskManagerBack.addTask(task1);
        taskManagerBack.addTask(task2);
        taskManagerBack.addEpic(epic2);
        Subtask subtask1 = new Subtask("подзадача1", "Описание1", epic1.getId(), Status.NEW);
        Subtask subtask2 = new Subtask("подзадача2", "Описание2", epic1.getId(), Status.NEW);
        Subtask subtask3 = new Subtask("подзадача2", "Описание3", epic1.getId(), Status.NEW);
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
