const fs = require('fs');
const path = require('path');
const wabt = require('wabt')();
const Benchmark = require('benchmark');
const jsReference = require('../sample_programs/benchmarks/js_reference');

let suite = new Benchmark.Suite;

suite.on('cycle', function(event) {
  console.log(String(event.target));
  console.log('         mean: ' + event.target.stats.mean);
  console.log('    deviation: ' + event.target.stats.deviation)
})

suite.add('WebAssembly sum of first 1000 squares', function() {
  wasmInstance.Benchmarks_sumSquares(1000);
})

suite.add('JavaScript sum of first 1000 squares', function() {
  jsReference.sumSquares(1000);
})

suite.add('WebAssembly sum of first 10000 squares', function() {
  wasmInstance.Benchmarks_sumSquares(10000);
})

suite.add('JavaScript sum of first 10000 squares', function() {
  jsReference.sumSquares(10000);
})

suite.add('WebAssembly recurse 1000 times', function() {
  wasmInstance.Benchmarks_recurse(1000);
})

suite.add('JavaScript recurse 1000 times', function() {
  jsReference.recurse(1000);
})

suite.add('WebAssembly recurse 10000 times', function () {
  wasmInstance.Benchmarks_recurse(10000);
})

suite.add('JavaScript recurse 10000 times', function () {
  jsReference.recurse(10000);
})

const watPath = path.resolve(__dirname, '..', 'sample_programs_compiled', 'benchmarks.wat');
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
