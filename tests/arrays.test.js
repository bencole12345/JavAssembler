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
})
