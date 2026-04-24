package history;

import task.BaseTask;

public class Node {
    private Node prev;
    private Node next;
    private BaseTask value;

    public Node(BaseTask value, Node prev, Node next) {
        this.prev = prev;
        this.next = next;
        this.value = value;
    }

    public Node getPrev() {
        return prev;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public BaseTask getValue() {
        return value;
    }

    public void setValue(BaseTask value) {
        this.value = value;
    }
}
