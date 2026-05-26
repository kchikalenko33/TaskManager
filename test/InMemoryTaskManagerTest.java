import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;
import taskManager.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

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
                LocalDateTime.of(2026, 05, 19, 5, 12), Duration.ofHours(4));
        task2 = new Task("Задача2", "Описание2", Status.NEW,
                LocalDateTime.of(2026, 04, 29, 5, 12), Duration.ofHours(2));
        epic1 = new Epic("Эпик1", "Описание1", Status.NEW,
                LocalDateTime.of(2026, 05, 9, 5, 12), Duration.ofHours(5));
        epic2 = new Epic("Эпик2", "Описание2", Status.NEW,
                LocalDateTime.of(2026, 05, 13, 5, 12), Duration.ofHours(1));
        subtask1 = new Subtask("подзадача1", "Описание1", epic1.getId(), Status.NEW,
                LocalDateTime.of(2026, 5, 19, 5, 12), Duration.ofHours(4));
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
    void  updateEpicStatusAllNewTest() {
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
}
