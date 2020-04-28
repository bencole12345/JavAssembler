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

describe('Generic types', () => {
  test('Generic types work', () => {
    const result = wasmInstance.GenericTypes_testGenericContainerWithInteger();
    expect(result).toBeTruthy();
  })
  test('Can reuse a generic class with different types', () => {
    const result = wasmInstance.GenericTypes_testGenericContainerWithExampleClass();
    expect(result).toBeTruthy();
  })
})
