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

describe('Arrays', () => {
  test('Array elements are stored correctly', () => {
    const array = wasmInstance.Arrays_makeArray(3);
    wasmInstance.Arrays_setElement(array, 0, 0);
    wasmInstance.Arrays_setElement(array, 1, -1);
    wasmInstance.Arrays_setElement(array, 2, 10);
    const first = wasmInstance.Arrays_getElement(array, 0);
    const second = wasmInstance.Arrays_getElement(array, 1);
    const third = wasmInstance.Arrays_getElement(array, 2);
    expect(first).toBe(0);
    expect(second).toBe(-1);
    expect(third).toBe(10);
  })
  test('Can pass array as parameter', () => {
    const result = wasmInstance.Arrays_passArrayAsParameter();
    expect(result).toBeTruthy();
  })
})
