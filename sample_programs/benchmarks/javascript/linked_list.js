class LinkedListNode {
  constructor(value) {
    this.value = value;
  }
}

export class LinkedList {
  constructor() {
    this.size = 0;
  }

  append(value) {
    const newNode = new LinkedListNode(value);
    if (this.size == 0) {
      this.first = newNode;
      this.last = newNode;
    } else {
      const last = this.last;
      last.next = newNode;
      this.last = newNode;
    }
    this.size++;
  }

  popFirstElement() {
    const first = this.first;
    const value = first.value;
    this.first = first.next;
    this.size--;
    return value;
  }
}
