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

describe('Garbage collection', () => {
  test('Repeatedly allocate memory', () => {
    const success = wasmInstance.GarbageCollection_testGarbageCollection(100000);
    expect(success).toBeTruthy();
  })
  test('Request more memory from JavaScript host', () => {
    const success = wasmInstance.GarbageCollection_testRequestingAdditionalMemory(100000);
    expect(success).toBeTruthy();
  })
})
