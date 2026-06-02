package taskManager;

import exception.FileNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import task.*;
import taskManager.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager>{
    private File file;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            file = File.createTempFile("tasks", ".csv");
        } catch (IOException e) {
            throw new FileNotFoundException("Файл не найден.");
        }
        return new FileBackedTaskManager(file);
    }

    @AfterEach
    void fileDelete() {
        if (file != null && file.exists()) file.delete();
    }

    // ---------- historyToString, historyFromString ---------- todo добавить проверки, что приходит нулл

    @Test
    void historyToStringEmptyTest() {
        String result = FileBackedTaskManager.historyToString(taskManager.getHistoryManager());

        assertEquals("", result,
                "Пустая история должна преобразовываться в пустую строку");
    }

    @Test
    void historyToStringFilledHistoryTest() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());

        String result = FileBackedTaskManager.historyToString(taskManager.getHistoryManager());
        String expected = task1.getId() + "," + task2.getId();

        assertEquals(expected, result, "История должна преобразовываться в строку id через запятую в порядке просмотров");
    }

    @Test
    void historyFromStringEmptyStringTest() {
        List<Integer> ids = FileBackedTaskManager.historyFromString("");

        assertTrue(ids.isEmpty(),
                "Пустая строка истории должна преобразовываться в пустой список id");
    }

    @Test
    void historyFromStringFilledTest() {
        List<Integer> result = FileBackedTaskManager.historyFromString("1,2,3");
        List<Integer> expected = List.of(1, 2, 3);

        assertEquals(expected, result, "Строка истории '1,2,3' должна преобразовываться в список [1, 2, 3]");
    }

    // ---------- toString / fromString ----------

    @Test
    void toStringTaskTest() {
        taskManager.addTask(task1);

        String result = taskManager.toString(task1);

        String expected = task1.getId() + ",TASK," +
                task1.getName() + "," +
                task1.getStatus() + "," +
                task1.getDescription() + "," +
                task1.getStartTime() + "," +
                task1.getDuration();

        assertEquals(expected, result,
                "Обычная задача должна корректно сериализоваться в строку");
    }

    @Test
    void toStringEpicTest() {
        taskManager.addEpic(epic1);

        String result = taskManager.toString(epic1);

        String expected = epic1.getId() + ",EPIC," +
                epic1.getName() + "," +
                epic1.getStatus() + "," +
                epic1.getDescription() + "," +
                epic1.getStartTime() + "," +
                epic1.getDuration();

        assertEquals(expected, result,
                "Эпик должен корректно сериализоваться в строку");
    }

    @Test
    void toStringSubtaskTest() {
        taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        taskManager.addSubtask(subtask1);

        String result = taskManager.toString(subtask1);

        String expected = subtask1.getId() + ",SUBTASK," +
                subtask1.getName() + "," +
                subtask1.getStatus() + "," +
                subtask1.getDescription() + "," +
                subtask1.getStartTime() + "," +
                subtask1.getDuration() + "," +
                subtask1.getEpicId();

        assertEquals(expected, result,
                "Подзадача должна корректно сериализоваться в строку вместе с epicId");
    }

    @Test
    void fromStringTaskTest() {
        String value = "1,TASK,Задача,NEW,Описание,2026-05-19T05:12,PT4H";
        Task expected = new Task("Задача","Описание", 1, Status.NEW,
                LocalDateTime.of(2026,5,19,5,12),Duration.ofHours(4));

        BaseTask task = taskManager.fromString(value);

        assertEquals(expected, task);

        /*assertInstanceOf(Task.class, task,
                "Строка с типом TASK должна преобразовываться в объект Task");
        assertEquals(1, task.getId(),
                "После десериализации id задачи должен совпадать");
        assertEquals("Задача", task.getName(),
                "После десериализации имя задачи должно совпадать");
        assertEquals(Status.NEW, task.getStatus(),
                "После десериализации статус задачи должен совпадать");
        assertEquals("Описание", task.getDescription(),
                "После десериализации описание задачи должно совпадать");
        assertEquals(LocalDateTime.of(2026, 5, 19, 5, 12), task.getStartTime(),
                "После десериализации startTime задачи должен совпадать");
        assertEquals(Duration.ofHours(4), task.getDuration(),
                "После десериализации duration задачи должен совпадать");*/
    }

    @Test
    void fromStringEpicTest() {
        String value = "2,EPIC,Эпик,IN_PROGRESS,Описание эпика,2026-05-09T05:12,PT5H";

        BaseTask task = taskManager.fromString(value);

        assertInstanceOf(Epic.class, task,
                "Строка с типом EPIC должна преобразовываться в объект Epic");
        assertEquals(2, task.getId(),
                "После десериализации id эпика должен совпадать");
        assertEquals("Эпик", task.getName(),
                "После десериализации имя эпика должно совпадать");
        assertEquals(Status.IN_PROGRESS, task.getStatus(),
                "После десериализации статус эпика должен совпадать");
        assertEquals("Описание эпика", task.getDescription(),
                "После десериализации описание эпика должно совпадать");
        assertEquals(LocalDateTime.of(2026, 5, 9, 5, 12), task.getStartTime(),
                "После десериализации startTime эпика должен совпадать");
        assertEquals(Duration.ofHours(5), task.getDuration(),
                "После десериализации duration эпика должен совпадать");
    }

    @Test
    void fromStringSubtaskTest() {
        String value = "3,SUBTASK,Подзадача,DONE,Описание подзадачи,2026-05-21T09:00,PT2H,10";

        BaseTask task = taskManager.fromString(value);
        Subtask subtask = (Subtask) task;

        assertInstanceOf(Subtask.class, task,
                "Строка с типом SUBTASK должна преобразовываться в объект Subtask");
        assertEquals(3, subtask.getId(),
                "После десериализации id подзадачи должен совпадать");
        assertEquals("Подзадача", subtask.getName(),
                "После десериализации имя подзадачи должно совпадать");
        assertEquals(Status.DONE, subtask.getStatus(),
                "После десериализации статус подзадачи должен совпадать");
        assertEquals("Описание подзадачи", subtask.getDescription(),
                "После десериализации описание подзадачи должно совпадать");
        assertEquals(LocalDateTime.of(2026, 5, 21, 9, 0), subtask.getStartTime(),
                "После десериализации startTime подзадачи должен совпадать");
        assertEquals(Duration.ofHours(2), subtask.getDuration(),
                "После десериализации duration подзадачи должен совпадать");
        assertEquals(10, subtask.getEpicId(),
                "После десериализации epicId подзадачи должен совпадать");
    }

    @Test
    void fromStringNullOrBlankReturnsNullTest() {
        assertNull(taskManager.fromString(null),
                "Метод fromString должен возвращать null при передаче null");
        assertNull(taskManager.fromString(""),
                "Метод fromString должен возвращать null при передаче пустой строки");
        assertNull(taskManager.fromString("   "),
                "Метод fromString должен возвращать null при передаче строки из пробелов");
    }

    @Test
    void fromStringInvalidFormatThrowsExceptionTest() {
        String invalid = "1,TASK,too,few,fields";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> taskManager.fromString(invalid),
                "Метод fromString должен выбрасывать IllegalArgumentException при невалидном формате строки");
        assertEquals("Невалидный формат строки", exception.getMessage());
    }

    // ---------- save() ----------

    @Test
    void saveEmptyTest() throws IOException {
        taskManager.save();
        String expected = "id,type,name,status,description,startTime,duration,epicId\n\n\n1";
        String actual = String.join("\n", Files.readAllLines(file.toPath()));

        assertEquals(expected, actual, "формат не соответствует ожидаемому");
    }

    @Test
    void saveThrowFileSaveExceptionTest() {
        //todo через рефлекшн
    }

}
