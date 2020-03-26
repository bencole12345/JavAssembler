const fs = require('fs');
const path = require('path');
const wabt = require('wabt')();

let wasmInstance;
beforeAll(async () => {
  const watPath = path.resolve(__dirname, '..', 'sample_programs_compiled', 'tests.wat');
  const watBuffer = fs.readFileSync(watPath, 'utf8');
  const wasmModule = wabt.parseWat(watPath, watBuffer);
  const {buffer} = wasmModule.toBinary({});
  const module = await WebAssembly.compile(buffer);
  const instance = await WebAssembly.instantiate(module);
  wasmInstance = instance.exports;
})

describe('Function calling', () => {
  test('Call another function', () => {
    const result = wasmInstance.Functions_callAnotherFunction();
    expect(result).toBe(1);
  })
  test('Call a public external function', () => {
    const result = wasmInstance.Functions_callPublicExternalFunction();
    expect(result).toBe(1);
  })
  test('Parameters work', () => {
    const result = wasmInstance.Functions_callAddOneFunction(1);
    expect(result).toBe(2);
  })
})

describe('Private methods', () => {
  test('Private methods not accessible externally', () => {
    expect(wasmInstance.FunctionsExternal_privateExternalFunction).not.toBeDefined();
  })
})
