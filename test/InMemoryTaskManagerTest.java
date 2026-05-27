import org.junit.jupiter.api.*;
import task.*;
import taskManager.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryTaskManagerTest {
    Task task1;
    Task task2;
    Subtask subtask1;
    Subtask subtask2;
    Epic epic1;
    Epic epic2;
    InMemoryTaskManager taskManager;

    @BeforeEach
    void init() {
        task1 = new Task("Задача1", "Описание1", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 5, 12), Duration.ofHours(4));
        task2 = new Task("Задача2", "Описание2", Status.NEW,
                LocalDateTime.of(2026, 4, 29, 5, 12), Duration.ofHours(2));
        epic1 = new Epic("Эпик1", "Описание1", Status.NEW,
                LocalDateTime.of(2026, 5, 9, 5, 12), Duration.ofHours(5));
        epic2 = new Epic("Эпик2", "Описание2", Status.NEW,
                LocalDateTime.of(2026, 5, 13, 5, 12), Duration.ofHours(1));
        subtask1 = new Subtask("подзадача1", "Описание1", epic1.getId(), Status.NEW,
                LocalDateTime.of(2026, 5, 19, 5, 13), Duration.ofHours(4));
        subtask2 = new Subtask("подзадача2", "Описание2", epic1.getId(),
                Status.NEW, LocalDateTime.of(2026, 4, 19, 5, 12), Duration.ofHours(2));
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void updateEpicStatusEmptyEpicTest() {
        epic1.setStatus(Status.DONE);
        taskManager.addEpic(epic1);
        taskManager.updateEpicStatus(epic1);

        Assertions.assertEquals(Status.NEW, epic1.getStatus());
    }

    @Test
    void updateEpicStatusAllNewTest() {
        taskManager.addEpic(epic1);
        epic1.setStatus(Status.IN_PROGRESS);
        subtask1.setEpicId(1);
        subtask2.setEpicId(1);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Assertions.assertEquals(2, epic1.getAllSubtask().size());
        taskManager.updateEpicStatus(epic1);
        Assertions.assertEquals(Status.NEW, epic1.getStatus());
    }

    @Test
    void updateEpicStatusAllDoneTest() {
        taskManager.addEpic(epic1);
        epic1.setStatus(Status.IN_PROGRESS);
        subtask1.setEpicId(1);
        subtask1.setStatus(Status.DONE);
        subtask2.setEpicId(1);
        subtask2.setStatus(Status.DONE);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Assertions.assertEquals(2, epic1.getAllSubtask().size());
        taskManager.updateEpicStatus(epic1);
        Assertions.assertEquals(Status.DONE, epic1.getStatus());
    }

    @Test
    void updateEpicStatusAllNewAndDoneTest() {
        taskManager.addEpic(epic1);
        epic1.setStatus(Status.IN_PROGRESS);
        subtask1.setEpicId(1);
        subtask2.setEpicId(1);
        subtask2.setStatus(Status.DONE);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        Assertions.assertEquals(2, epic1.getAllSubtask().size());
        taskManager.updateEpicStatus(epic1);
        Assertions.assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    void updateEpicStatusAllInProgressTest() {
        taskManager.addEpic(epic1);
        epic1.setStatus(Status.IN_PROGRESS);
        subtask1.setEpicId(1);
        subtask1.setStatus(Status.IN_PROGRESS);
        subtask2.setEpicId(1);
        subtask2.setStatus(Status.IN_PROGRESS);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Assertions.assertEquals(2, epic1.getAllSubtask().size());
        taskManager.updateEpicStatus(epic1);
        Assertions.assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Disabled
    @Test
    void getHistoryEmptyTest() {
        Assertions.assertTrue(taskManager.getHistory().isEmpty());
    }

    @Disabled
    @Test
    void getHistoryDuplicationTest() {
        taskManager.addTask(task1);
        taskManager.getTask(task1.getId());
        taskManager.getTask(task1.getId());
        List<BaseTask> expected = List.of(task1);

        Assertions.assertEquals(expected, taskManager.getHistory());
    }

    @Test
    void getTasksEmptyTest() {
        Assertions.assertTrue(taskManager.getTasks().isEmpty());
    }

    @Test
    void getTasksFilledTest() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        List<Task> expected = List.of(task1, task2);

        Assertions.assertEquals(expected, taskManager.getTasks());
    }

    @Test
    void getSubtaskEmptyTest() {
        Assertions.assertTrue(taskManager.getSubtasks().isEmpty());
    }

    @Test
    void getSubtasksFilledTest() {
        taskManager.addEpic(epic1);
        subtask1.setEpicId(1);
        subtask2.setEpicId(1);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        // List<Subtask> expected = new ArrayList<>(List.of(subtask1, subtask2));
        Subtask[] expected2 = {subtask1, subtask2};

        Assertions.assertArrayEquals(expected2, taskManager.getSubtasks().toArray());
    }


}
