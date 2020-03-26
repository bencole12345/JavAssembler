const fs = require('fs');
const path = require('path');
const wabt = require('wabt')();

let wasmInstance;
beforeAll(async() => {
  const watPath = path.resolve(__dirname, '..', 'sample_programs_compiled', 'tests.wat');
  const watBuffer = fs.readFileSync(watPath, 'utf8');
  const wasmModule = wabt.parseWat(watPath, watBuffer);
  const {buffer} = wasmModule.toBinary({});
  const module = await WebAssembly.compile(buffer);
  const instance = await WebAssembly.instantiate(module);
  wasmInstance = instance.exports;
})

describe('Addition', () => {
  test.each([
    [1, 2, 3],
    [2, 1, 3],
    [1, 0, 1],
    [0, 1, 1],
    [1, -1, 0]
  ])('%f + %f = %f', (a, b, sum) => {
    const result = wasmInstance.Expressions_add(a, b);
    expect(result).toBe(sum);
  })
})

describe('Subtraction', () => {
  test.each([
    [3, 2, 1],
    [3, 1, 2],
    [1, 0, 1],
    [0, 1, -1]
  ])('%f + %f = %f', (a, b, difference) => {
    const result = wasmInstance.Expressions_subtract(a, b);
    expect(result).toBe(difference);
  })
})

describe('Multiplication', () => {
  test.each([
    [1, 0, 0],
    [0, 1, 0],
    [2, 3, 6],
    [3, 2, 6],
    [12, -1, -12],
    [-2, 6, -12]
  ])('%f * %f = %f', (a, b, product) => {
    const result = wasmInstance.Expressions_multiply(a, b);
    expect(result).toBe(product);
  })
})

describe('Division', () => {
  test.each([
    [1, 1, 1],
    [3, 1, 3],
    [1, 3, 0],
    [2, 3, 0],
    [12, 3, 4],
    [-12, 3, -4]
  ])('%f / %f = %f', (a, b, divisor) => {
    const result = wasmInstance.Expressions_divide(a, b);
    expect(result).toBe(divisor);
  })
})

describe('Logical operators', () => {

  test.each([
    [false, false, false],
    [false, true, false],
    [true, false, false],
    [true, true, true]
  ])('%s && %s = %f', (a, b, and) => {
    const result = wasmInstance.Expressions_and(a, b);
    if (and) {
      expect(result).toBeTruthy();
    } else {
      expect(result).toBeFalsy();
    }
  })

  test.each([
    [false, false, false],
    [false, true, true],
    [true, false, true],
    [true, true, true]
  ])('%s || %s = %f', (a, b, or) => {
    const result = wasmInstance.Expressions_or(a, b);
    if (or) {
      expect(result).toBeTruthy();
    } else {
      expect(result).toBeFalsy();
    }
  })

  test('!true = false', () => {
    const result = wasmInstance.Expressions_not(true);
    expect(result).toBeFalsy();
  })

  test('!false = true', () => {
    const result = wasmInstance.Expressions_not(false);
    expect(result).toBeTruthy();
  })

})

describe('Arithmetic parsing order', () => {
  test('1 + 2 * 3 = 7', () => {
    const result = wasmInstance.Expressions_arithmeticParsingOrder();
    expect(result).toBe(7);
  })
})

describe('Boolean logic parsing order', () => {
  test('true && false || false && true = false', () => {
    const result = wasmInstance.Expressions_booleanLogicParsingOrder1();
    expect(result).toBeFalsy();
  })
  test('true && true || false && false = true', () => {
    const result = wasmInstance.Expressions_booleanLogicParsingOrder2();
    expect(result).toBeTruthy();
  })
})
