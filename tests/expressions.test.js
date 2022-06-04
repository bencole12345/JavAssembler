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
  ])('Integer addition: %f + %f = %f', (a, b, sum) => {
    const result = wasmInstance.Expressions_integerAdd(a, b);
    expect(result).toBe(sum);
  })
  test.each([
    [1.0, 2.0, 3.0],
    [2.0, 1.0, 3.0],
    [1.0, 0.0, 1.0],
    [0.0, 1.0, 1.0],
    [1.0, -1.0, 0.0]
  ])('Floating-point addition: %f + %f is approximately %f', (a, b, sum) => {
    const result = wasmInstance.Expressions_floatingPointAdd(a, b);
    expect(result).toBeCloseTo(sum);
  })
})

describe('Subtraction', () => {
  test.each([
    [3, 2, 1],
    [3, 1, 2],
    [1, 0, 1],
    [0, 1, -1]
  ])('Integer subtraction: %f - %f = %f', (a, b, difference) => {
    const result = wasmInstance.Expressions_integerSubtract(a, b);
    expect(result).toBe(difference);
  })
  test.each([
    [3.0, 2.0, 1.0],
    [3.0, 1.0, 2.0],
    [1.0, 0.0, 1.0],
    [0.0, 1.0, -1.0]
  ])('Floating-point subtraction: %f - %f = %f', (a, b, difference) => {
    const result = wasmInstance.Expressions_floatingPointSubtract(a, b);
    expect(result).toBeCloseTo(difference);
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
  ])('Integer multiplication: %f * %f = %f', (a, b, product) => {
    const result = wasmInstance.Expressions_integerMultiply(a, b);
    expect(result).toBe(product);
  })
  test.each([
    [1.0, 0.0, 0.0],
    [0.0, 1.0, 0.0],
    [2.0, 3.0, 6.0],
    [3.0, 2.0, 6.0],
    [12.0, -1.0, -12.0],
    [-2.0, 6.0, -12.0],
    [1.41421356237, 1.41421356237, 2]
  ])('Integer multiplication: %f * %f = %f', (a, b, product) => {
    const result = wasmInstance.Expressions_floatingPointMultiply(a, b);
    expect(result).toBeCloseTo(product);
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
  ])('Integer division: %f / %f = %f', (a, b, divisor) => {
    const result = wasmInstance.Expressions_integerDivide(a, b);
    expect(result).toBe(divisor);
  })
  test('Integer divide by zero causes trap', () => {
    expect(() => {
      wasmInstance.Expressions_divide(1, 0);
    }).toThrow();
  })
  test.each([
    [1.0, 1.0, 1.0],
    [3.0, 1.0, 3.0],
    [1.0, 3.0, 0.333333333333],
    [2.0, 3.0, 0.666666666666],
    [12.0, 3.0, 4.0],
    [-12.0, 3.0, -4.0]
  ])('Floating-point division: %f / %f = %f', (a, b, divisor) => {
    const result = wasmInstance.Expressions_floatingPointDivide(a, b);
    expect(result).toBeCloseTo(divisor);
  })
  test('Floating-point divide by zero gives infinity', () => {
    const result = wasmInstance.Expressions_floatingPointDivide(1, 0);
    expect(result).toBe(Infinity);
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
