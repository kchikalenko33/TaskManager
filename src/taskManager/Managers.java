package taskManager;

import history.HistoryManager;
import history.InMemoryHistoryManager;
import history.InMemoryTaskManager;

public class Managers {
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}
