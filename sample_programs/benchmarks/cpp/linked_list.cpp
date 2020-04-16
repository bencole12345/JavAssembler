#define NULL 0

#include "linked_list.h"


void LinkedList::append(int value)
{
    LinkedListNode *newNode = new LinkedListNode();
    newNode->value = value;
    newNode->next = NULL;
    if (size == 0)
    {
        first = newNode;
        last = newNode;
    }
    else
    {
        last->next = newNode;
        last = newNode;
    }
    size++;
}

int LinkedList::popFirstElement()
{
    int value = first->value;
    LinkedListNode *second = first->next;
    delete first;
    first = second;
    size--;
    return value;
}
