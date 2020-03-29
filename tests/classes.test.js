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

describe('Classes', () => {
  test('Set/get public attribute directly', () => {
    const reference = wasmInstance.Classes_createInstanceWithoutConstructor();
    wasmInstance.Classes_setXAttributeDirectly(reference, 10);
    const result = wasmInstance.Classes_lookupXAttributeDirectly(reference);
    expect(result).toBe(10);
  })
  test('Use setter/getter for public attribute', () => {
    const reference = wasmInstance.Classes_createInstanceWithoutConstructor();
    wasmInstance.Classes_callSetX(reference, 10);
    const result = wasmInstance.Classes_callGetX(reference);
    expect(result).toBe(10);
  })
  test('Use setter/getter for private attribute', () => {
    const reference = wasmInstance.Classes_createInstanceWithoutConstructor();
    wasmInstance.Classes_callSetY(reference, 10);
    const result = wasmInstance.Classes_callGetY(reference);
    expect(result).toBe(10);
  })
  test('Set public attribute using constructor', () => {
    const reference = wasmInstance.Classes_createInstanceUsingConstructor(5);
    const result = wasmInstance.Classes_lookupXAttributeDirectly(reference);
    expect(result).toBe(5);
  })
  test('Mutate public attribute', () => {
    const reference = wasmInstance.Classes_createInstanceUsingConstructor(1);
    wasmInstance.Classes_callIncrementX(reference);
    const result = wasmInstance.Classes_lookupXAttributeDirectly(reference);
    expect(result).toBe(2);
  })
  test('Mutate private attribute', () => {
    const reference = wasmInstance.Classes_createInstanceWithoutConstructor();
    wasmInstance.Classes_callSetY(reference, 1);
    wasmInstance.Classes_callIncrementY(reference);
    const result = wasmInstance.Classes_callGetY(reference);
    expect(result).toBe(2);
  })
  test('More than 32 attributes', () => {
    const reference = wasmInstance.Classes_createClassWith33Attributes();
    wasmInstance.Classes_setX1(reference, 1);
    wasmInstance.Classes_setX32(reference, 32);
    wasmInstance.Classes_setX33(reference, 33);
    const x1 = wasmInstance.Classes_getX1(reference);
    const x32 = wasmInstance.Classes_getX32(reference);
    const x33 = wasmInstance.Classes_getX33(reference);
    expect(x1).toBe(1);
    expect(x32).toBe(32);
    expect(x33).toBe(33);
  })
})

describe('Dynamic polymorphism', () => {
  test('Parent instance uses parent method', () => {
    const reference = wasmInstance.DynamicPolymorphism_createParentInstance();
    const result = wasmInstance.DynamicPolymorphism_callIsParentFromParentContext(reference);
    expect(result).toBeTruthy();
  })
  test('Child instance uses child method in parent context', () => {
    const reference = wasmInstance.DynamicPolymorphism_createChildInstance();
    const result = wasmInstance.DynamicPolymorphism_callIsParentFromParentContext(reference);
    expect(result).toBeFalsy();
  })
  test('Child instance uses child method in child context', () => {
    const reference = wasmInstance.DynamicPolymorphism_createChildInstance();
    const result = wasmInstance.DynamicPolymorphism_callIsParentFromChildContext(reference);
    expect(result).toBeFalsy();
  })
})
