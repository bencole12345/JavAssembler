const LinkedList = require('./linked_list.js');``

module.exports.sumSquares = function(n) {
  let sum = 0;
  for (let i = 1; i <= n; i++) {
    sum += i*i;
  }
  return sum;
}

module.exports.recurse = function(depth) {
  if (depth > 0) {
    this.recurse(depth - 1);
  }
}

module.exports.linkedListInsertTraverse = function(numValues) {
  let list = new LinkedList();
  for (let i = 0; i < numValues; i++) {
    list.append(i);
  }
  for (let i = 0; i < numValues; i++) {
    const result = list.popFirstElement();
  }
}

module.exports.traverseArray = function(size) {
  let array = [];
  for (let i = 0; i < size; i++) {
    array.push(i);
  }
  for (let i = 0; i < size; i++) {
    const temp = array[i];
  }
}
