import org.junit.jupiter.api.*;
import task.*;
import taskManager.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

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

        assertEquals(Status.NEW, epic1.getStatus());
    }

    @Test
    void updateEpicStatusAllNewTest() {
        taskManager.addEpic(epic1);
        epic1.setStatus(Status.IN_PROGRESS);
        subtask1.setEpicId(1);
        subtask2.setEpicId(1);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(2, epic1.getAllSubtask().size());
        taskManager.updateEpicStatus(epic1);
        assertEquals(Status.NEW, epic1.getStatus());
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

        assertEquals(2, epic1.getAllSubtask().size());
        taskManager.updateEpicStatus(epic1);
        assertEquals(Status.DONE, epic1.getStatus());
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
        assertEquals(2, epic1.getAllSubtask().size());
        taskManager.updateEpicStatus(epic1);
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
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

        assertEquals(2, epic1.getAllSubtask().size());
        taskManager.updateEpicStatus(epic1);
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    void getTasksEmptyTest() {
        assertTrue(taskManager.getTasks().isEmpty());
    }

    @Test
    void getTasksFilledTest() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        List<Task> expected = List.of(task1, task2);

        assertEquals(expected, taskManager.getTasks());
    }

    @Test
    void getSubtaskEmptyTest() {
        assertTrue(taskManager.getSubtasks().isEmpty());
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

        assertArrayEquals(expected2, taskManager.getSubtasks().toArray());
    }

    @Test
    void getEpicsEmptyTest() {
        assertTrue(taskManager.getEpics().isEmpty());
    }

    @Test
    void getEpicsFilledTest() {
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        List<Epic> expected = new ArrayList<>(List.of(epic1, epic2));

        assertEquals(expected, taskManager.getEpics());
    }

    @Test
    void getHistoryEmptyTest() {
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void getHistoryDuplicationTest() {
        taskManager.addTask(task1);
        taskManager.getTask(task1.getId());
        taskManager.getTask(task1.getId());
        List<BaseTask> expected = List.of(task1);

        assertEquals(expected, taskManager.getHistory());
    }

    @Test
    void getHistoryFilledTest() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        List<Task> expected = List.of(task1,task2);

        assertEquals(expected, taskManager.getHistory());
    }

    @Test
    void getPrioritizedTasksEmptyTest() {
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void getPrioritizedTasksFilledTest() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        List<BaseTask> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());

        // Проверка сортировки: сначала по времени старта, затем по ID
        if (task1.getStartTime().isBefore(task2.getStartTime())) {
            assertEquals(task1, prioritized.get(0));
            assertEquals(task2, prioritized.get(1));
        } else {
            assertEquals(task2, prioritized.get(0));
            assertEquals(task1, prioritized.get(1));
        }
    }

    @Test
    void hasIntersectionTest() {
        assertFalse(taskManager.hasIntersection(task1));
    }

    @Test
    void hasIntersectionStartInsideExistingTest() {
        Task existingTask = new Task("Существующая", "Описание", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 5, 0), Duration.ofHours(4)); // 05:00–09:00
        taskManager.addTask(existingTask);

        Task newTask = new Task("Новая", "Описание", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 7, 0), Duration.ofHours(2)); // 07:00–09:00 — старт внутри
        assertTrue(taskManager.hasIntersection(newTask));
    }

    @Test
    void hasIntersectionEndInsideExistingTest() {
        Task existingTask = new Task("Существующая", "Описание", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 5, 0), Duration.ofHours(4)); // 05:00–09:00
        taskManager.addTask(existingTask);

        Task newTask = new Task("Новая", "Описание", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 3, 0), Duration.ofHours(6)); // 03:00–09:00 — конец внутри
        assertTrue(taskManager.hasIntersection(newTask));
    }

    @Test
    void hasIntersectionSurroundingExistingTest() {
        Task existingTask = new Task("Существующая", "Описание", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 5, 0), Duration.ofHours(4)); // 05:00–09:00
        taskManager.addTask(existingTask);

        Task newTask = new Task("Новая", "Описание", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 4, 0), Duration.ofHours(6)); // 04:00–10:00 — охватывает существующую
        assertTrue(taskManager.hasIntersection(newTask));
    }

    @Test
    void hasIntersectionExistingSurroundsNewTest() {
        Task existingTask = new Task("Существующая", "Описание", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 4, 0), Duration.ofHours(6)); // 04:00–10:00
        taskManager.addTask(existingTask);

        Task newTask = new Task("Новая", "Описание", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 5, 0), Duration.ofHours(4)); // 05:00–09:00 — внутри существующей
        assertTrue(taskManager.hasIntersection(newTask));
    }

    @Test
    void hasIntersectionAdjacentTasksTest() {
        Task existingTask = new Task("Существующая", "Описание", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 5, 0), Duration.ofHours(4)); // 05:00–09:00
        taskManager.addTask(existingTask);

        Task newTask = new Task("Новая", "Описание", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 9, 1), Duration.ofHours(2)); // 09:01–11:00 — впритык, без пересечения
        assertFalse(taskManager.hasIntersection(newTask));
    }

    @Test
    void hasIntersectionNewTaskBeforeExistingTest() {
        Task existingTask = new Task("Существующая", "Описание", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 10, 0), Duration.ofHours(4)); // 10:00–14:00
        taskManager.addTask(existingTask);

        Task newTask = new Task("Новая", "Описание", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 5, 59), Duration.ofHours(4)); // 05:00–09:59 — до существующей
        assertFalse(taskManager.hasIntersection(newTask));
    }

    @Test
    void hasIntersectionWithSubtaskTest() {
        taskManager.addEpic(epic1);
        Subtask existingSubtask = new Subtask("Существующая подзадача", "Описание", epic1.getId(),
                Status.NEW, LocalDateTime.of(2026, 5, 19, 5, 0), Duration.ofHours(4)); // 05:00–09:00
        taskManager.addSubtask(existingSubtask);

        Task newTask = new Task("Новая задача", "Описание", Status.NEW,
                LocalDateTime.of(2026, 5, 19, 7, 0), Duration.ofHours(2)); // 07:00–09:00 — пересекается с подзадачей
        assertTrue(taskManager.hasIntersection(newTask));
    }

    @Test
    void removeAllTasksWhenTasksExistTest() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());

        taskManager.removeAllTasks();

        assertTrue(taskManager.getTasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void removeAllTasksEmptyTasksListTest() {
        taskManager.removeAllTasks();

        assertTrue(taskManager.getTasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void removeAllSubtasksWhenSubtasksExistTest() {
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        subtask1.setEpicId(epic1.getId());
        subtask2.setEpicId(epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());

        taskManager.removeAllSubtasks();

        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

}
