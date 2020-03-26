const fs = require('fs');
const path = require('path');
const wabt = require('wabt')();
const Benchmark = require('benchmark');

let suite = new Benchmark.Suite;

suite.on('cycle', function(event) {
  console.log(String(event.target));
  console.log('         mean: ' + event.target.stats.mean);
  console.log('    deviation: ' + event.target.stats.deviation)
})

suite.add('wasm sum of first 100 squares', function() {
  wasmInstance.Benchmarks_sumSquares(100);
})

suite.add('wasm sum of first 1000 squares', function() {
  wasmInstance.Benchmarks_sumSquares(1000);
})

const watPath = path.resolve(__dirname, '.', 'generated_wat', 'benchmarks.wat');
const watBuffer = fs.readFileSync(watPath, 'utf8');
const wasmModule = wabt.parseWat(watPath, watBuffer);
const { buffer } = wasmModule.toBinary({});
let wasmInstance;
WebAssembly.compile(buffer).then(themodule => {
  WebAssembly.instantiate(themodule).then(instance => {
    wasmInstance = instance.exports;
    suite.run();
  })
})
