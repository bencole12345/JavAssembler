const fs = require('fs');
const path = require('path');
const wabt = require('wabt')();

let wasmInstance;
beforeAll(async () => {
  const watPath = path.resolve(__dirname, '..', 'generated_wat', 'tests.wat');
  const watBuffer = fs.readFileSync(watPath, 'utf8');
  const wasmModule = wabt.parseWat(watPath, watBuffer);
  const {buffer} = wasmModule.toBinary({});
  const module = await WebAssembly.compile(buffer);
  const instance = await WebAssembly.instantiate(module);
  wasmInstance = instance.exports;
})

describe('If statement', () => {
  test('Branch taken when condition is true', () => {
    const result = wasmInstance.LanguageConstructs_ifThen(true);
    expect(result).toBeTruthy();
  })
  test('Branch not taken when condition is false', () => {
    const result = wasmInstance.LanguageConstructs_ifThen(false);
    expect(result).toBeFalsy();
  })

  test('If-branch taken when condition is true', () => {
    const result = wasmInstance.LanguageConstructs_ifThenElse(true);
    expect(result).toBeTruthy();
  })
  test('Else-branch taken when condition is false', () => {
    const result = wasmInstance.LanguageConstructs_ifThenElse(false);
    expect(result).toBeFalsy();
  })

  test.each([
    [false, false, 3],
    [false, true, 2],
    [true, false, 1],
    [true, true, 1]
  ])('if (%s) {<1>} else if (%s) {<2>} else {<3>} takes path <%f>', (condition1, condition2, correctPath) => {
    const pathTaken = wasmInstance.LanguageConstructs_chainedIfStatements(condition1, condition2);
    expect(pathTaken).toBe(correctPath);
  })
})

describe('For loop', () => {
  test.each([
    [0, 0],
    [1, 1],
    [10, 10],
    [-1, 0]
  ])('Attempting to iterate %f times actually iterates %f times', (requested, actual) => {
    const iterations = wasmInstance.LanguageConstructs_countForIterations(requested);
    expect(iterations).toBe(actual);
  })

  test.each([
    [0, 0],
    [1, 1],
    [10, 55]
  ])('Sum of first %f integers using for loop is %f', (n, sum) => {
    const result = wasmInstance.LanguageConstructs_sumToFor(n);
    expect(result).toBe(sum);
  })
})

describe('While loop', () => {
  test.each([
    [0, 0],
    [1, 1],
    [10, 55]
  ])('Sum of first %f integers using while loop is %f', (n, sum) => {
    const result = wasmInstance.LanguageConstructs_sumToWhile(n);
    expect(result).toBe(sum);
  })
})
