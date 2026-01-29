package task;

import taskManager.TaskManager;

public class MainTest {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        System.out.println("=== Тестирование Task ===");
        Task task1 = new Task("Task1", "Описание Task1", 0, Status.NEW);
        manager.addTask(task1);
        Task task2 = new Task("Task2", "Описание Task2", 0, Status.IN_PROGRESS);
        manager.addTask(task2);

        System.out.println("Все задачи: " + manager.getTasks());
        System.out.println("Task1: " + manager.getTask(task1.getId()));
        manager.removeTask(task1.getId());
        System.out.println("После удаления Task1: " + manager.getTasks());

        System.out.println("\n=== Тестирование Epic и Subtask ===");
        Epic epic1 = new Epic("Epic1", "Большой эпик", 0, Status.NEW);
        manager.addEpic(epic1);

        Subtask sub1 = new Subtask("Sub1", "Подзадача 1", 0, Status.NEW, epic1);
        Subtask sub2 = new Subtask("Sub2", "Подзадача 2", 0, Status.DONE, epic1);
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        System.out.println("Эпики: " + manager.getEpics());
        System.out.println("Подзадачи: " + manager.getSubtasks());
        System.out.println("Epic1 статус: " + epic1.getStatus());
        System.out.println("Subtask Epic1: " + epic1.getSubtaskIds());

        System.out.println("\n=== Тестирование удаления Subtask ===");
        manager.removeSubtask(sub1.getId());
        System.out.println("После удаления Sub1: " + manager.getSubtasks());
        System.out.println("Epic1 статус после удаления: " + epic1.getStatus());

        System.out.println("\n=== Тестирование полного удаления ===");
        System.out.println("До очистки:");
        System.out.println("Tasks: " + manager.getTasks().size());
        System.out.println("Subtasks: " + manager.getSubtasks().size());
        System.out.println("Epics: " + manager.getEpics().size());

        manager.removeAllTasks();
        manager.removeAllSubtasks();
        manager.removeAllEpics();

        System.out.println("После полной очистки:");
        System.out.println("Tasks: " + manager.getTasks().size());
        System.out.println("Subtasks: " + manager.getSubtasks().size());
        System.out.println("Epics: " + manager.getEpics().size());

        System.out.println("\n=== Тестирование получения по ID ===");
        Epic epic2 = new Epic("Epic2", "Тестовый эпик", 0, Status.NEW);
        manager.addEpic(epic2);
        System.out.println("Получен Epic2: " + manager.getEpic(epic2.getId()));
        manager.removeEpic(epic2.getId());
        System.out.println("Epic2 после удаления: " + manager.getEpic(epic2.getId()));
    }
}
