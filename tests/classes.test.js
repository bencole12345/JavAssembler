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
    const result = wasmInstance.Classes_testSetGetPublicAttributeDirectly(10);
    expect(result).toBe(10);
  })
  test('Use setter/getter for public attribute', () => {
    const result = wasmInstance.Classes_testUseSetterGetterForPublicAttribute(10);
    expect(result).toBe(10);
  })
  test('Use setter/getter for private attribute', () => {
    const result = wasmInstance.Classes_testUseSetterGetterForPrivateAttribute(10);
    expect(result).toBe(10);
  })
  test('Set public attribute using constructor', () => {
    const result = wasmInstance.Classes_testSetPublicAttributeUsingConstructor(5);
    expect(result).toBe(5);
  })
  test('Mutate public attribute', () => {
    const result = wasmInstance.Classes_testMutatePublicAttribute(1);
    expect(result).toBe(2);
  })
  test('Mutate private attribute', () => {
    const result = wasmInstance.Classes_testMutatePrivateAttribute(1);
    expect(result).toBe(2);
  })
  test('More than 32 attributes', () => {
    const success = wasmInstance.Classes_testMoreThan32Attributes();
    expect(success).toBeTruthy();
  })
  test('Can pass anonymous new object as argument to function', () => {
    const success = wasmInstance.Classes_testPassAnonymousObjectAsArgument();
    expect(success).toBeTruthy();
  })
})

describe('Dynamic polymorphism', () => {
  test('Parent instance uses parent method', () => {
    const result = wasmInstance.DynamicPolymorphism_testParentInParentContext();
    expect(result).toBeTruthy();
  })
  test('Child instance uses child method in parent context', () => {
    const result = wasmInstance.DynamicPolymorphism_testChildInParentContext();
    expect(result).toBeFalsy();
  })
  test('Child instance uses child method in child context', () => {
    const result = wasmInstance.DynamicPolymorphism_testChildInChildContext();
    expect(result).toBeFalsy();
  })
})
