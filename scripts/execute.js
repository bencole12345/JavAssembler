const { readFileSync } = require("fs");

const run = async () => {
    const buffer = readFileSync("./test.wasm");
    const module = await WebAssembly.compile(buffer);
    const instance = await WebAssembly.instantiate(module);
    const output = instance.exports.main();
    console.log(output);
};

run();
