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

describe('Null object reference', () => {
  test('Lookup valid attribute on null reference causes trap', () => {
    expect(() => {
      wasmInstance.NullTest_lookupValueOnNullObject();
    }).toThrow();
  })
  test('Can pass null reference to function', () => {
    expect(() => {
      wasmInstance.NullTest_passNullObjectAsArgument();
    }).toThrow();
  })
  test('Test for null on null object is true', () => {
    const equal = wasmInstance.NullTest_testNullObjectIsNull();
    expect(equal).toBeTruthy();
  })
  test('Test for null on non-null object is false', () => {
    const equal = wasmInstance.NullTest_testNonNullObjectIsNull();
    expect(equal).toBeFalsy();
  })
  test('Null test is true when testing null argument', () => {
    const isNull = wasmInstance.NullTest_testNullOnNullArgument();
    expect(isNull).toBeTruthy();
  })
  test('Null test is false when testing non-null argument', () => {
    const isNull = wasmInstance.NullTest_testNullOnNonNullArgument();
    expect(isNull).toBeFalsy();
  })
})
