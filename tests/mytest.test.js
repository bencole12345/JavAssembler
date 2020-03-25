const fs = require('fs');
const path = require('path');

describe('test.wasm tests', () => {

    let wasmInstance;

    beforeAll(async() => {
        const wasmPath = path.resolve(__dirname, '..', 'generated_wasm', 'test.wasm');
        const buffer = fs.readFileSync(wasmPath);
        const module = await WebAssembly.compile(buffer);
        const instance = await WebAssembly.instantiate(module);
        wasmInstance = instance.exports;
    });

    test('my first test', () => {
        expect(wasmInstance.Test_testThis()).toBe(2);
    });

    test('my other test', () => {
        expect(true).toBe(true);
    });

});