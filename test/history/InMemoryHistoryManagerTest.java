package history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.BaseTask;
import task.Status;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;
    private BaseTask task1;
    private BaseTask task2;
    private BaseTask task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Task1", "Desc1", 1, Status.NEW,
                LocalDateTime.of(2026, 1, 1, 10, 0), Duration.ofHours(1));
        task2 = new Task("Task2", "Desc2", 2, Status.NEW,
                LocalDateTime.of(2026, 1, 1, 11, 0), Duration.ofHours(1));
        task3 = new Task("Task3", "Desc3", 3, Status.NEW,
                LocalDateTime.of(2026, 1, 1, 12, 0), Duration.ofHours(1));
    }

    @Test
    void addNullTaskDoesNothingTest() {
        historyManager.add(null);

        List<BaseTask> history = historyManager.getHistory();

        assertTrue(history.isEmpty(),
                "Добавление null не должно изменять историю");
    }

    @Test
    void addSingleTaskAppearsInHistoryTest() {
        historyManager.add(task1);

        List<BaseTask> history = historyManager.getHistory();

        assertEquals(1, history.size(),
                "После добавления одной задачи история должна содержать один элемент");
        assertEquals(task1, history.get(0),
                "Единственным элементом истории должна быть добавленная задача");
    }

    @Test
    void addSeveralTasksOrderIsPreservedTest() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<BaseTask> history = historyManager.getHistory();

        assertEquals(3, history.size(),
                "После добавления трёх задач история должна содержать три элемента");
        assertEquals(List.of(task1, task2, task3), history,
                "Порядок задач в истории должен соответствовать порядку добавления");
    }

    @Test
    void addSameTaskTwiceMovesItToEndTest() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1); // повторно

        List<BaseTask> history = historyManager.getHistory();

        assertEquals(2, history.size(),
                "Повторное добавление той же задачи не должно дублировать её в истории");
        assertEquals(task2, history.get(0),
                "После повторного добавления задача с другим id должна остаться первой");
        assertEquals(task1, history.get(1),
                "Повторно добавленная задача должна переместиться в конец истории");
    }

    @Test
    void removeFromEmptyHistoryDoesNothingTest() {
        historyManager.remove(1);

        List<BaseTask> history = historyManager.getHistory();

        assertTrue(history.isEmpty(),
                "Удаление из пустой истории не должно вызывать ошибок и не должно изменять историю");
    }

    @Test
    void removeFirstTaskTest() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());

        List<BaseTask> history = historyManager.getHistory();

        assertEquals(2, history.size(),
                "После удаления первого элемента история должна содержать два элемента");
        assertEquals(List.of(task2, task3), history,
                "После удаления первого элемента история должна начинаться со второго");
    }

    @Test
    void removeMiddleTaskTest() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        List<BaseTask> history = historyManager.getHistory();

        assertEquals(2, history.size(),
                "После удаления среднего элемента история должна содержать два элемента");
        assertEquals(List.of(task1, task3), history,
                "После удаления среднего элемента соседние должны связаться напрямую");
    }

    @Test
    void removeLastTaskTest() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getId());

        List<BaseTask> history = historyManager.getHistory();

        assertEquals(2, history.size(),
                "После удаления последнего элемента история должна содержать два элемента");
        assertEquals(List.of(task1, task2), history,
                "После удаления последнего элемента порядок оставшихся должен сохраниться");
    }

    @Test
    void removeOnlyTaskMakesHistoryEmptyTest() {
        historyManager.add(task1);

        historyManager.remove(task1.getId());

        List<BaseTask> history = historyManager.getHistory();

        assertTrue(history.isEmpty(),
                "После удаления единственной задачи история должна стать пустой");
    }

    @Test
    void removeNonExistingTaskDoesNothingTest() {
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(999); // нет такого id

        List<BaseTask> history = historyManager.getHistory();

        assertEquals(List.of(task1, task2), history,
                "Удаление несуществующего id не должно изменять историю");
    }
}
