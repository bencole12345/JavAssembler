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

describe('Bytes', () => {
  test('Overflow is correct', () => {
    const result = wasmInstance.TypeRanges_addBytes(127, 1);
    expect(result).toBe(-128);
  })
  test('Underflow is correct', () => {
    const result = wasmInstance.TypeRanges_subtractBytes(-128, 1);
    expect(result).toBe(127);
  })
})

describe('Chars', () => {
  test('Overflow is correct', () => {
    const result = wasmInstance.TypeRanges_addChars(Math.pow(2,16)-1, 1);
    expect(result).toBe(0);
  })
  test('Underflow is correct', () => {
    const result = wasmInstance.TypeRanges_subtractChars(0, 1);
    expect(result).toBe(Math.pow(2,16)-1);
  })
})

describe('Shorts', () => {
  test('Overflow is correct', () => {
    const result = wasmInstance.TypeRanges_addShorts(Math.pow(2,15)-1, 1);
    expect(result).toBe(-Math.pow(2,15));
  })
  test('Underflow is correct', () => {
    const result = wasmInstance.TypeRanges_subtractShorts(-Math.pow(2,15), 1);
    expect(result).toBe(Math.pow(2,15)-1);
  })
})

describe('Integers', () => {
  test('Overflow is correct', () => {
    const result = wasmInstance.TypeRanges_addInts(Math.pow(2,31)-1, 1);
    expect(result).toBe(-Math.pow(2,31));
  })
  test('Underflow is correct', () => {
    const result = wasmInstance.TypeRanges_subtractInts(-Math.pow(2,31), 1);
    expect(result).toBe(Math.pow(2,31)-1);
  })
});

describe('Longs', () => {
  test('Overflow is correct', () => {
    const result = wasmInstance.TypeRanges_testLongOverflow();
    expect(result).toBeTruthy();
  })
  test('Underflow is correct', () => {
    const result = wasmInstance.TypeRanges_testLongUnderflow();
    expect(result).toBeTruthy();
  })
})
