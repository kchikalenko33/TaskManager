package task;

public class Task extends BaseTask{
    public Task(String name, String description, int id, Status status) {
        super(name, description, id, status);
    }

    public Task(String name, String description, Status status) {
        super(name, description, status);
    }
}
