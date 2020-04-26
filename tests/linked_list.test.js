const fs = require('fs');
const path = require('path');
const wabt = require('wabt')();

let wasmInstance;
beforeAll(async () => {
  const watPath = path.resolve(__dirname, '..', 'sample_programs_compiled', 'tests.wat');
  const watBuffer = fs.readFileSync(watPath, 'utf8');
  const wasmModule = wabt.parseWat(watPath, watBuffer);
  const { buffer } = wasmModule.toBinary({});
  const module = await WebAssembly.compile(buffer);
  const instance = await WebAssembly.instantiate(module);
  wasmInstance = instance.exports;
})

describe('Linked List', () => {
  test('Empty linked list demo works', () => {
    const success = wasmInstance.LinkedList_doLinkedListTest(0);
    expect(success).toBeTruthy();
  })
  test('Non-empty linked list demo works', () => {
    const success = wasmInstance.LinkedList_doLinkedListTest(100);
    expect(success).toBeTruthy();
  })
})
