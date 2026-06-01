package taskManager;

import exception.FileNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import taskManager.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

    // ---------- historyToString, historyFromString----------

    @Test
    void historyToStringEmptyTest() {
        String result = FileBackedTaskManager.historyToString(taskManager.getHistoryManager()); //todo убрать getHistoryManager из inMemorytaskManager, а здесь использовать рефлекшн апи для получения доступа поля historyManager

        assertEquals("", result);
    }

    @Test
    void historyToStringFilledHistoryTest() {
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());

        String result = FileBackedTaskManager.historyToString(taskManager.getHistoryManager());

        assertEquals(task1.getId() + "," + task2.getId(), result);
    }

    @Test
    void historyFromStringFilledTest() {
        List<Integer> result = FileBackedTaskManager.historyFromString("1,2,3");

        assertEquals(List.of(1, 2, 3), result);
    }

}
