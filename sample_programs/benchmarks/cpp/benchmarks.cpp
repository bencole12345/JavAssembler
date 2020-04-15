#include <iostream>
#include <emscripten.h>

#include "linked_list.h"

extern "C" {

EMSCRIPTEN_KEEPALIVE
int sumSquares(int n)
{
    int sum = 0;
    for (int i = 1; i <= n; i++) {
        sum = sum + i*i;
    }
    return sum;
}

EMSCRIPTEN_KEEPALIVE
void recurse(int depth)
{
    if (depth > 0) {
        recurse(depth - 1);
    }
}

EMSCRIPTEN_KEEPALIVE
void linkedListInsertTraverse(int numValues)
{
    LinkedList list;
    for (int i = 0; i < numValues; i++) {
        list.append(i);
    }
    for (int i = 0; i < numValues; i++) {
        int result = list.popFirstElement();
    }
};

EMSCRIPTEN_KEEPALIVE
void traverseArray(int size)
{
    int array[size];
    for (int i = 0; i < size; i++) {
        array[i] = i;
    }
    for (int i = 0; i < size; i++) {
        int temp = array[i];
    }
}

}
