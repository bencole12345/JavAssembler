struct LinkedListNode {
    int value;
    LinkedListNode* next;
};

class LinkedList
{
private:
    int size;
    LinkedListNode *first;
    LinkedListNode *last;
public:
    LinkedList();
    void append(int value);
    int popFirstElement();
};
