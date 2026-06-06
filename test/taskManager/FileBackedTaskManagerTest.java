package taskManager;

import exception.FileNotFoundException;
import exception.FileSaveException;
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
        File dir = new File("saveTestDir");
        dir.mkdir();

        FileBackedTaskManager badManager = new FileBackedTaskManager(dir);

        assertThrows(FileSaveException.class,
                badManager::save,
                "save() должен выбросить FileSaveException при ошибке записи в файл");

        dir.delete();
    }

    @Test
    void saveWithSingleTaskFormatTest() throws IOException {
        taskManager.addTask(task1);

        String actual = String.join("\n", Files.readAllLines(file.toPath()));

        String taskLine = taskManager.toString(task1);
        String expected = String.join("\n", List.of(
                "id,type,name,status,description,startTime,duration,epicId",
                taskLine,
                "",
                "",
                String.valueOf(taskManager.idGen)
        ));

        assertEquals(expected, actual,
                "Формат файла с одной задачей не соответствует ожидаемому");
    }

    // ---------- loadFromFile() ----------
    @Test
    void loadFromFileEmptyFileReturnsEmptyManagerTest() {
        taskManager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded.getTasks().isEmpty(),
                "После загрузки из пустого менеджера список обычных задач должен быть пустым");
        assertTrue(loaded.getEpics().isEmpty(),
                "После загрузки из пустого менеджера список эпиков должен быть пустым");
        assertTrue(loaded.getSubtasks().isEmpty(),
                "После загрузки из пустого менеджера список подзадач должен быть пустым");
        assertTrue(loaded.getHistory().isEmpty(),
                "После загрузки из пустого менеджера история должна быть пустой");
    }

    @Test
    void loadFromFileRestoresSingleTaskWithoutHistoryTest() {
        taskManager.addTask(task1);
        int originalIdGen = taskManager.idGen;

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        List<Task> originalTasks = taskManager.getTasks();
        List<Task> loadedTasks = loaded.getTasks();

        // Восстановился ровно одна Task
        assertEquals(originalTasks.size(), loadedTasks.size(),
                "Количество задач до сохранения и после загрузки должно совпадать");
        assertEquals(originalTasks.get(0), loadedTasks.get(0),
                "Задача до сохранения и после загрузки должна быть эквивалентна");

        // История пустая (нет вызова getTask до загрузки)
        assertTrue(loaded.getHistory().isEmpty(),
                "Если задачи не запрашивались, история после загрузки должна быть пустой");

        // Приоритетный список содержит ту же самую задачу
        List<BaseTask> prioritized = loaded.getPrioritizedTasks();
        assertEquals(1, prioritized.size(),
                "После загрузки в приоритетном списке должна быть одна задача");
        assertEquals(loadedTasks.get(0).getId(), prioritized.get(0).getId(),
                "После загрузки в приоритетном списке должна быть именно восстановленная задача");

        // idGen восстановился
        assertEquals(originalIdGen, loaded.idGen,
                "После загрузки значение idGen должно совпадать с исходным");
    }



    @Test
    void loadFromFileRestoresEpicAndSubtasksRelationsAndStatusTest() {
        // arrange: создаём эпик и две подзадачи
        taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        subtask2.setEpicId(epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.updateEpicStatus(epic1);
        int originalIdGen = taskManager.idGen;

        // act
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        List<Epic> originalEpics   = taskManager.getEpics();
        List<Epic> loadedEpics     = loaded.getEpics();
        List<Subtask> originalSubs = taskManager.getSubtasks();
        List<Subtask> loadedSubs   = loaded.getSubtasks();

        // ---------- проверка количества ----------
        assertEquals(1, loadedEpics.size(),
                "После загрузки должен восстановиться один эпик");
        assertEquals(2, loadedSubs.size(),
                "После загрузки должны восстановиться две подзадачи");

        // ---------- проверка эпика ----------
        Epic origEpic = originalEpics.get(0);
        Epic loadEpic = loadedEpics.get(0);

        assertEquals(origEpic.getId(), loadEpic.getId(),
                "Id эпика после загрузки должен совпадать");
        assertEquals(origEpic.getName(), loadEpic.getName(),
                "Имя эпика после загрузки должно совпадать");
        assertEquals(origEpic.getDescription(), loadEpic.getDescription(),
                "Описание эпика после загрузки должно совпадать");
        assertEquals(origEpic.getStatus(), loadEpic.getStatus(),
                "Статус эпика после загрузки должен совпадать");
        assertEquals(origEpic.getStartTime(), loadEpic.getStartTime(),
                "startTime эпика после загрузки должен совпадать");
        assertEquals(origEpic.getDuration(), loadEpic.getDuration(),
                "duration эпика после загрузки должен совпадать");

        assertEquals(2, loadEpic.getSubtaskIds().size(),
                "У эпика после загрузки должны быть две подзадачи");
        assertTrue(loadEpic.getSubtaskIds().containsAll(
                        List.of(subtask1.getId(), subtask2.getId())),
                "У эпика после загрузки должны быть те же subtaskId, что и до сохранения");


        originalSubs.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        loadedSubs.sort((a, b) -> Integer.compare(a.getId(), b.getId()));

        assertEquals(originalSubs.size(), loadedSubs.size(),
                "Количество подзадач до сохранения и после загрузки должно совпадать");

        for (int i = 0; i < originalSubs.size(); i++) {
            Subtask origSub = originalSubs.get(i);
            Subtask loadSub = loadedSubs.get(i);

            assertEquals(origSub.getId(), loadSub.getId(),
                    "Id подзадачи после загрузки должен совпадать");
            assertEquals(origSub.getName(), loadSub.getName(),
                    "Имя подзадачи после загрузки должно совпадать");
            assertEquals(origSub.getDescription(), loadSub.getDescription(),
                    "Описание подзадачи после загрузки должно совпадать");
            assertEquals(origSub.getStatus(), loadSub.getStatus(),
                    "Статус подзадачи после загрузки должен совпадать");
            assertEquals(origSub.getStartTime(), loadSub.getStartTime(),
                    "startTime подзадачи после загрузки должен совпадать");
            assertEquals(origSub.getDuration(), loadSub.getDuration(),
                    "duration подзадачи после загрузки должен совпадать");
            assertEquals(origSub.getEpicId(), loadSub.getEpicId(),
                    "epicId подзадачи после загрузки должен совпадать");
        }

        // ---------- проверка idGen ----------
        assertEquals(originalIdGen, loaded.idGen,
                "После загрузки значение idGen должно совпадать с исходным");
    }

    @Test
    void loadFromFileRestoresHistoryTest() {
        taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addTask(task1);

        // формируем историю
        taskManager.getEpic(epic1.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getTask(task1.getId());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        List<BaseTask> history = loaded.getHistory();
        assertEquals(3, history.size(),
                "После загрузки история должна содержать три записи");

        assertEquals(epic1.getId(), history.get(0).getId(),
                "Первым в истории после загрузки должен быть эпик");
        assertEquals(subtask1.getId(), history.get(1).getId(),
                "Вторым в истории после загрузки должна быть подзадача");
        assertEquals(task1.getId(), history.get(2).getId(),
                "Третьим в истории после загрузки должна быть обычная задача");
    }

    @Test
    void loadFromFileRestoresPrioritizedTasksForTaskAndSubtaskTest() {
        taskManager.addEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addTask(task1);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        List<BaseTask> prioritized = loaded.getPrioritizedTasks();

        assertEquals(2, prioritized.size(),
                "После загрузки в приоритетном списке должны быть задача и подзадача");
        List<Integer> ids = prioritized.stream().map(BaseTask::getId).toList();
        assertTrue(ids.contains(task1.getId()),
                "После загрузки в приоритетном списке должна быть обычная задача");
        assertTrue(ids.contains(subtask1.getId()),
                "После загрузки в приоритетном списке должна быть подзадача");
    }

    @Test
    void loadFromFileWithoutEmptyLineUsesFallbackIndexTest() throws IOException {
        // 4 строки, ни одна из которых не равна "" (чисто пустой строке)
        String content = String.join("\n", List.of(
                "id,type,name,status,description,startTime,duration,epicId",
                "1,TASK,Task,NEW,Desc,2026-05-19T05:12,PT4H,",
                "",
                "2"
        ));
        Files.writeString(file.toPath(), content);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loaded.getTasks().size(),
                "При отсутствии пустой строки-разделителя задача должна восстановиться по запасному сценарию");
    }
}
