public class LinkedList {

    private int size;
    private LinkedListNode first;
    private LinkedListNode last;

    public LinkedList() {
        this.size = 0;
    }

    public void append(int value) {
        LinkedListNode newNode = new LinkedListNode(value);
        if (this.size == 0) {
            this.first = newNode;
            this.last = newNode;
        } else {
            LinkedListNode last = this.last;
            last.next = newNode;
            this.last = newNode;
        }
        this.size = this.size + 1;
    }

    public int popFirstElement() {
        LinkedListNode first = this.first;
        int value = first.value;
        this.first = first.next;
        this.size = this.size - 1;
        return value;
    }

    public static boolean doLinkedListTest(int length) {
        LinkedList list = new LinkedList();
        for (int i = 0; i < length; i++) {
            list.append(i);
        }
        boolean correct = true;
        for (int i = 0; i < length; i++) {
            int element = list.popFirstElement();
            correct = correct && (element == i);
        }
        return correct;
    }
    
}
