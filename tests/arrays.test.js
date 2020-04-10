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
    const success = wasmInstance.Arrays_testArrayElementsStoredCorrectly();
    expect(success).toBeTruthy();
  })
  test('Can pass array as parameter', () => {
    const success = wasmInstance.Arrays_testPassArrayAsParameter();
    expect(success).toBeTruthy();
  })
  test('Accessing array element -1 causes trap', () => {
    expect(() => {
      wasmInstance.Arrays_readElementAtIndexInArrayOfSize(-1, 10);
    }).toThrow();
  })
  test('Accessing element 0 of empty array causes trap', () => {
    expect(() => {
      wasmInstance.Arrays_readElementAtIndexInArrayOfSize(0, 0);
    }).toThrow();
  })
  test('Accessing element 10 of 10-element array causes trap', () => {
    expect(() => {
      wasmInstance.Arrays_readElementAtIndexInArrayOfSize(10, 10);
    }).toThrow();
  })
  test('Accessing element 9 of 10-element array does not cause trap', () => {
    expect(() => {
      wasmInstance.Arrays_readElementAtIndexInArrayOfSize(9, 10);
    }).not.toThrow();
  })
  // TODO: Add 2D arrays test
})
