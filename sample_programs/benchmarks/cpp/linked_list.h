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
    LinkedList()
    {
        size = 0;
        first = NULL;
        last = NULL;
    }
    ~LinkedList()
    {
        LinkedListNode* curr;
        while (curr != NULL) {
            LinkedListNode* next = curr->next;
            delete curr;
            curr = next;
        }
    }
    void append(int value);
    int popFirstElement();
};
