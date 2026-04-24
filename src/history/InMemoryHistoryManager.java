package history;

import task.BaseTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    //private List<BaseTask> history = new ArrayList<>();

    private Node head;
    private Node tail;
    private Map<Integer, Node> history = new HashMap<>();

    private Node linkLast(BaseTask task) {
        Node node = new Node(task, tail, null);

        if (tail == null) {
            head = node;
        } else {
            tail.setNext(node);
        }

        tail = node;

        return node;
    }

    private void removeMode(Node node) {
        if (node == null) {
            return;
        }

        if (node.getPrev() == null && node.getNext() != null) { // удаление первого не единственного элемента
            head = node.getNext();
            head.setPrev(null);
        } else if (node.getPrev() != null && node.getNext() == null) { // удаление последнего не единственного элемента
            tail = node.getPrev();
            tail.setNext(null);
        } else if (head == tail) { // удаление единственного
            head = null;
            tail = null;
        } else if (node.getPrev() != null && node.getNext() != null) { // удаление по ноде у которого есть элемент справа и слева
            node.getPrev().setNext(node.getNext());
            node.getNext().setPrev(node.getPrev());
        }

    }

    private List<BaseTask> getTasks() {
        List<BaseTask> history = new ArrayList<>();

        for (Node i = head; i != null; i = i.getNext()) {
            history.add(i.getValue());
        }

        return history;
    }

    @Override
    public void add(BaseTask task) {
        if (task == null) {
            return;
        }

        if (history.containsKey(task.getId())) {
            remove(task.getId());
        }

        history.put(task.getId(), linkLast(task));
    }

    @Override
    public void remove(int id) {
        removeMode(history.remove(id));
    }

    @Override
    public List<BaseTask> getHistory() {
        return getTasks();
    }
}
