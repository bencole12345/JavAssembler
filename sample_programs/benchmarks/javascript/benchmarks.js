// const LinkedList = require('./linked_list.js');

import { LinkedList } from './linked_list.js';

export function sumSquares(n) {
  let sum = 0;
  for (let i = 1; i <= n; i++) {
    sum += i*i;
  }
  return sum;
}

export function recurse(depth) {
  if (depth > 0) {
    this.recurse(depth - 1);
  }
}

export function linkedListInsertTraverse(numValues) {
  let list = new LinkedList();
  for (let i = 0; i < numValues; i++) {
    list.append(i);
  }
  for (let i = 0; i < numValues; i++) {
    const result = list.popFirstElement();
  }
}

export function traverseArray(size) {
  let array = [];
  for (let i = 0; i < size; i++) {
    array.push(i);
  }
  for (let i = 0; i < size; i++) {
    const temp = array[i];
  }
}
