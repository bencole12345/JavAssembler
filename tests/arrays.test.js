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

describe('Arrays', () => {
  test('Integer array elements are stored correctly', () => {
    const success = wasmInstance.Arrays_testIntegerArrayElementsStoredCorrectly();
    expect(success).toBeTruthy();
  })
  test('Short array elements are stored correctly', () => {
    const success = wasmInstance.Arrays_testShortArrayElementsStoredCorrectly();
    expect(success).toBeTruthy();
  })
  test('Float array elements are stored correctly', () => {
    const success = wasmInstance.Arrays_testFloatArrayElementsStoredCorrectly();
    expect(success).toBeTruthy();
  })
  test('Double array elements are stored correctly', () => {
    const success = wasmInstance.Arrays_testDoubleArrayElementsStoredCorrectly();
    expect(success).toBeTruthy();
  })
  test('Non-primitive array elements are stored correctly', () => {
    const success = wasmInstance.Arrays_testNonPrimitiveArrayElementsStoredCorrectly();
    expect(success).toBeTruthy();
  })
  test('Can pass array as parameter', () => {
    const success = wasmInstance.Arrays_testPassArrayAsParameter();
    expect(success).toBeTruthy();
  })
  test('Reading array element -1 causes trap', () => {
    expect(() => {
      wasmInstance.Arrays_readElementAtIndexInArrayOfSize(-1, 10);
    }).toThrow();
  })
  test('Reading element 0 of empty array causes trap', () => {
    expect(() => {
      wasmInstance.Arrays_readElementAtIndexInArrayOfSize(0, 0);
    }).toThrow();
  })
  test('Reading element 10 of 10-element array causes trap', () => {
    expect(() => {
      wasmInstance.Arrays_readElementAtIndexInArrayOfSize(10, 10);
    }).toThrow();
  })
  test('Reading element 0 of 10-element array does not cause trap', () => {
    expect(() => {
      wasmInstance.Arrays_readElementAtIndexInArrayOfSize(0, 10);
    }).not.toThrow();
  })
  test('Reading element 9 of 10-element array does not cause trap', () => {
    expect(() => {
      wasmInstance.Arrays_readElementAtIndexInArrayOfSize(9, 10);
    }).not.toThrow();
  })
  test('Writing array element -1 causes trap', () => {
    expect(() => {
      wasmInstance.Arrays_writeElementAtIndexInArrayOfSize(-1, 10);
    }).toThrow();
  })
  test('Writing element 0 of empty array causes trap', () => {
    expect(() => {
      wasmInstance.Arrays_writeElementAtIndexInArrayOfSize(0, 0);
    }).toThrow();
  })
  test('Writing element 10 of 10-element array causes trap', () => {
    expect(() => {
      wasmInstance.Arrays_writeElementAtIndexInArrayOfSize(10, 10);
    }).toThrow();
  })
  test('Writing element 0 of 10-element array does not cause trap', () => {
    expect(() => {
      wasmInstance.Arrays_writeElementAtIndexInArrayOfSize(0, 10);
    }).not.toThrow();
  })
  test('Writing element 9 of 10-element array does not cause trap', () => {
    expect(() => {
      wasmInstance.Arrays_writeElementAtIndexInArrayOfSize(9, 10);
    }).not.toThrow();
  })
})
