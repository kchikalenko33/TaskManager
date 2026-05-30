import exception.EpicNotFoundException;
import exception.IntersectionException;
import exception.SubtaskNotFoundException;
import exception.TaskNotFoundException;
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
        List<Task> expected = List.of(task1, task2);

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

    @Test
    void removeAllSubtasksEmptyTasksListTest() {
        taskManager.removeAllSubtasks();

        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void removeAllEpicsWhenEpicsAndSubtasksExistTest() {
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        subtask1.setEpicId(epic1.getId());
        subtask2.setEpicId(epic2.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.getEpic(epic1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());

        taskManager.removeAllEpics();

        assertTrue(taskManager.getEpics().isEmpty());
        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void removeAllEpicsEmptyTest() {
        taskManager.removeAllEpics();

        assertTrue(taskManager.getEpics().isEmpty());
        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void getTaskExistsInTasksTest() {
        taskManager.addTask(task1);

        assertEquals(new ArrayList<>(), taskManager.getHistory());
        assertEquals(task1, taskManager.getTask(task1.getId()));
    }

    @Test
    void getTaskExistsInTasksAndHistoryTest() {
        taskManager.addTask(task1);

        assertEquals(task1, taskManager.getTask(task1.getId()));
        assertEquals(List.of(task1), taskManager.getHistory());
    }

    @Test
    void getTaskNotFoundTest() {
        assertThrows(TaskNotFoundException.class, () -> taskManager.getTask(Integer.MAX_VALUE));
    }

    @Test
    void getSubtaskExistsInSubtasksTest() {
        taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        taskManager.addSubtask(subtask1);

        assertEquals(new ArrayList<>(), taskManager.getHistory());
        assertEquals(subtask1, taskManager.getSubtask(subtask1.getId()));
    }

    @Test
    void getSubtaskExistsInSubtasksAndHistoryTest() {
        taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        taskManager.addSubtask(subtask1);

        assertEquals(subtask1, taskManager.getSubtask(subtask1.getId()));
        assertEquals(List.of(subtask1), taskManager.getHistory());
    }

    @Test
    void getSubtaskNotFoundTest() {
        assertThrows(SubtaskNotFoundException.class, () -> taskManager.getSubtask(Integer.MAX_VALUE));
    }

    @Test
    void getEpicExistsInEpicsTest() {
        taskManager.addEpic(epic1);

        assertEquals(new ArrayList<>(), taskManager.getHistory());
        assertEquals(epic1, taskManager.getEpic(epic1.getId()));
    }

    @Test
    void getEpicExistsInEpicsAndHistoryTest() {
        taskManager.addEpic(epic1);

        Epic result = taskManager.getEpic(epic1.getId());

        assertEquals(epic1, result);
        assertEquals(List.of(epic1), taskManager.getHistory());
    }

    @Test
    void getEpicNotFoundTest() {
        assertThrows(EpicNotFoundException.class,
                () -> taskManager.getEpic(Integer.MAX_VALUE));
    }

    @Test
    void getSubtaskOfEpicEmptyWhenEpicNotFoundTest() {
        List<Subtask> subtasks = taskManager.getSubtaskOfEpic(Integer.MAX_VALUE);

        assertTrue(subtasks.isEmpty());
    }

    @Test
    void getSubtaskOfEpicFilledTest() {
        taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        subtask2.setEpicId(epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        List<Subtask> subtasks = taskManager.getSubtaskOfEpic(epic1.getId());

        assertEquals(2, subtasks.size());
        assertTrue(subtasks.contains(subtask1));
        assertTrue(subtasks.contains(subtask2));
    }

    @Test
    void removeTaskRemovesFromTasksHistoryAndPrioritizedTest() {
        taskManager.addTask(task1);
        taskManager.getTask(task1.getId());

        Task removed = taskManager.removeTask(task1.getId());

        assertEquals(task1, removed);
        assertTrue(taskManager.getTasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void removeTaskNonExistingReturnsNullTest() {
        Task removed = taskManager.removeTask(Integer.MAX_VALUE);

        assertNull(removed);
    }

    @Test
    void removeSubtaskRemovesFromEpicHistoryAndPrioritizedTest() {
        taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.getSubtask(subtask1.getId());

        Subtask removed = taskManager.removeSubtask(subtask1.getId());

        assertEquals(subtask1, removed);
        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
        assertTrue(epic1.getAllSubtask().isEmpty());
    }

    @Test
    void removeSubtaskNonExistingReturnsNullTest() {
        Subtask removed = taskManager.removeSubtask(Integer.MAX_VALUE);

        assertNull(removed);
    }

    @Test
    void removeEpicRemovesItsSubtasksAndHistoryTest() {
        taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        taskManager.addSubtask(subtask1);

        taskManager.getEpic(epic1.getId());
        taskManager.getSubtask(subtask1.getId());

        Epic removed = taskManager.removeEpic(epic1.getId());

        assertEquals(epic1, removed);
        assertTrue(taskManager.getEpics().isEmpty());
        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void removeEpicNonExistingReturnsNullTest() {
        Epic removed = taskManager.removeEpic(Integer.MAX_VALUE);

        assertNull(removed);
    }

    @Test
    void addTaskSuccessSetsIdAndAddsToPrioritizedTest() {
        boolean result = taskManager.addTask(task1);

        assertTrue(result);
        assertTrue(task1.getId() > 0);
        assertEquals(List.of(task1), taskManager.getTasks());
        assertEquals(List.of(task1), taskManager.getPrioritizedTasks());
    }

    @Test
    void addTaskNullReturnsFalseTest() {
        assertFalse(taskManager.addTask(null));
    }

    @Test
    void addEpicSuccessSetsIdTest() {
        boolean result = taskManager.addEpic(epic1);

        assertTrue(result);
        assertTrue(epic1.getId() > 0);
        assertEquals(List.of(epic1), taskManager.getEpics());
    }

    @Test
    void addEpicNullReturnsFalseTest() {
        assertFalse(taskManager.addEpic(null));
    }

    @Test
    void addSubtaskSuccessTest() {
        taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1.getId());

        boolean result = taskManager.addSubtask(subtask1);

        assertTrue(result);
        assertTrue(subtask1.getId() > 0);
        assertEquals(List.of(subtask1), taskManager.getSubtasks());
        assertTrue(epic1.getAllSubtask().contains(subtask1));
        assertTrue(taskManager.getPrioritizedTasks().contains(subtask1));
    }

    @Test
    void addSubtaskWithoutEpicReturnsFalseTest() {
        // epic не добавлен в менеджер
        subtask1.setEpicId(999);

        boolean result = taskManager.addSubtask(subtask1);

        assertFalse(result);
        assertTrue(taskManager.getSubtasks().isEmpty());
    }

    @Test
    void addTaskWithIntersectionThrowsExceptionTest() {
        taskManager.addTask(task1); // уже в менеджере

        Task intersecting = new Task("Пересекается", "Описание", Status.NEW,
                task1.getStartTime().plusMinutes(10), Duration.ofHours(1));

        assertThrows(IntersectionException.class,
                () -> taskManager.addTask(intersecting));
    }

    @Test
    void addSubtaskWithIntersectionThrowsExceptionTest() {
        taskManager.addTask(task1);
        taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        // время пересекается с task1
        subtask1.setStartTime(task1.getStartTime().plusMinutes(5));

        assertThrows(IntersectionException.class,
                () -> taskManager.addSubtask(subtask1));
    }

    @Test
    void updateTaskWithIntersectionThrowsExceptionTest() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        // пробуем обновить task2 так, чтобы он пересекался с task1
        Task updated = new Task(task2.getName(), task2.getDescription(), task2.getStatus(),
                task1.getStartTime(), Duration.ofHours(1));
        updated.setId(task2.getId());

        assertThrows(IntersectionException.class,
                () -> taskManager.updateTask(updated));
    }

    @Test
    void updateSubtaskWithIntersectionThrowsExceptionTest() {
        taskManager.addEpic(epic1);
        taskManager.addTask(task1);

        // добавляем подзадачу без пересечения с task1
        subtask1.setEpicId(epic1.getId());
        subtask1.setStartTime(task1.getStartTime().minusHours(5)); // далеко до task1
        subtask1.setDuration(Duration.ofHours(1));
        taskManager.addSubtask(subtask1);

        // обновляем подзадачу так, чтобы она пересекалась с task1
        Subtask updated = new Subtask(
                subtask1.getName(),
                subtask1.getDescription(),
                epic1.getId(),
                subtask1.getStatus(),
                task1.getStartTime(),           // старт совпадает с task1
                Duration.ofHours(1)
        );
        updated.setId(subtask1.getId());

        assertThrows(IntersectionException.class,
                () -> taskManager.updateSubtask(updated));
    }

    @Test
    void updateTaskSuccessChangesFieldsTest() {
        taskManager.addTask(task1);

        Task updated = new Task("Новое имя", "Новое описание", Status.IN_PROGRESS,
                task1.getStartTime().plusHours(1), Duration.ofHours(10));
        updated.setId(task1.getId());

        boolean result = taskManager.updateTask(updated);

        assertTrue(result);
        Task fromManager = taskManager.getTask(task1.getId());
        assertEquals("Новое имя", fromManager.getName());
        assertEquals("Новое описание", fromManager.getDescription());
        assertEquals(Status.IN_PROGRESS, fromManager.getStatus());
        assertEquals(updated.getStartTime(), fromManager.getStartTime());
        assertEquals(updated.getDuration(), fromManager.getDuration());
    }

    @Test
    void updateTaskNonExistingReturnsFalseTest() {
        Task updated = new Task("Имя", "Опис", Status.NEW,
                LocalDateTime.now(), Duration.ofHours(1));
        updated.setId(999);

        assertFalse(taskManager.updateTask(updated));
    }

    @Test
    void updateSubtaskSuccessChangesFieldsAndEpicStatusTest() {
        taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        taskManager.addSubtask(subtask1);

        Subtask updated = new Subtask("Новое имя", "Новое описание",
                epic1.getId(), Status.DONE,
                subtask1.getStartTime(), subtask1.getDuration());
        updated.setId(subtask1.getId());

        boolean result = taskManager.updateSubtask(updated);

        assertTrue(result);
        Subtask fromManager = taskManager.getSubtask(subtask1.getId());
        assertEquals("Новое имя", fromManager.getName());
        assertEquals("Новое описание", fromManager.getDescription());
        assertEquals(Status.DONE, fromManager.getStatus());
        // Проверяем, что статус эпика пересчитан
        taskManager.updateEpicStatus(epic1);
        assertEquals(Status.DONE, epic1.getStatus());
    }

    @Test
    void updateSubtaskNonExistingReturnsFalseTest() {
        taskManager.addEpic(epic1);
        Subtask updated = new Subtask("Имя", "Опис", epic1.getId(),
                Status.NEW, LocalDateTime.now(), Duration.ofHours(1));
        updated.setId(999);

        assertFalse(taskManager.updateSubtask(updated));
    }

    @Test
    void updateSubtaskWithNonExistingEpicReturnsFalseTest() {
        subtask1.setEpicId(999);
        subtask1.setId(1); // предполагаем id
        assertFalse(taskManager.updateSubtask(subtask1));
    }

    @Test
    void updateEpicSuccessTest() {
        taskManager.addEpic(epic1);

        Epic updated = new Epic("Новое имя эпика", "Новое описание", Status.IN_PROGRESS,
                epic1.getStartTime(), epic1.getDuration());
        updated.setId(epic1.getId());

        boolean result = taskManager.updateEpic(updated);

        assertTrue(result);
        Epic fromManager = taskManager.getEpic(epic1.getId());
        assertEquals("Новое имя эпика", fromManager.getName());
        assertEquals("Новое описание", fromManager.getDescription());
        assertEquals(Status.IN_PROGRESS, fromManager.getStatus());
    }

    @Test
    void updateEpicNonExistingReturnsFalseTest() {
        Epic updated = new Epic("Имя", "Опис", Status.NEW,
                LocalDateTime.now(), Duration.ofHours(1));
        updated.setId(999);

        assertFalse(taskManager.updateEpic(updated));
    }

    @Test
    void patchTaskForNonExistingTaskReturnsFalseTest() {
        task1.setId(999);

        assertFalse(taskManager.patchTask(task1));
    }

    @Test
    void patchTaskForNonExistingSubtaskReturnsFalseTest() {
        subtask1.setId(999);

        assertFalse(taskManager.patchTask(subtask1));
    }
}
