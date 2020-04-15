const fs = require('fs');
const path = require('path');
const wabt = require('wabt')();
const Benchmark = require('benchmark');
const jsReference = require('../sample_programs/benchmarks/javascript/benchmarks');
const cppModule = require('../sample_programs_compiled/cpp_benchmarks');
const cppReference = {
  sumSquares: cppModule.cwrap('sumSquares', 'number', ['number']),
  recurse: cppModule.cwrap('recurse', null, ['number']),
  linkedListInsertTraverse: cppModule.cwrap('linkedListInsertTraverse', null, ['number']),
  traverseArray: cppModule.cwrap('traverseArray', null, ['number'])
};

let suite = new Benchmark.Suite;

suite.on('cycle', function(event) {
  console.log(String(event.target));
  console.log('         mean: ' + event.target.stats.mean);
  console.log('    deviation: ' + event.target.stats.deviation)
})

suite.add('JavAssembler sum of first 1000 squares', function() {
  wasmInstance.Benchmarks_sumSquares(1000);
})

suite.add('JavaScript sum of first 1000 squares', function() {
  jsReference.sumSquares(1000);
})

suite.add('C++ sum of first 1000 squares', function() {
  cppReference.sumSquares(1000);
})

suite.add('JavAssembler sum of first 10000 squares', function() {
  wasmInstance.Benchmarks_sumSquares(10000);
})

suite.add('JavaScript sum of first 10000 squares', function() {
  jsReference.sumSquares(10000);
})

suite.add('C++ sum of first 10000 squares', function() {
  cppReference.sumSquares(10000);
})

suite.add('JavAssembler recurse 1000 times', function() {
  wasmInstance.Benchmarks_recurse(1000);
})

suite.add('JavaScript recurse 1000 times', function() {
  jsReference.recurse(1000);
})

suite.add('C++ recurse 1000 times', function() {
  cppReference.recurse(1000);
})

suite.add('JavAssembler recurse 10000 times', function () {
  wasmInstance.Benchmarks_recurse(10000);
})

suite.add('JavaScript recurse 10000 times', function() {
  jsReference.recurse(10000);
})

suite.add('C++ recurse 10000 times', function() {
  cppReference.recurse(10000);
})

suite.add('JavAssembler insert and traverse 1000 linked list nodes', function() {
  wasmInstance.reset_allocator();
  wasmInstance.Benchmarks_linkedListInsertTraverse(1000);
})

suite.add('JavaScript insert and traverse 1000 linked list nodes', function() {
  jsReference.linkedListInsertTraverse(1000);
})

suite.add('C++ insert and traverse 1000 linked list nodes', function() {
  cppReference.linkedListInsertTraverse(1000);
})

suite.add('JavAssembler traverse array of length 1000', function() {
  wasmInstance.reset_allocator();
  wasmInstance.Benchmarks_traverseArray(1000);
})

suite.add('JavaScript traverse array of length 1000', function() {
  jsReference.traverseArray(1000);
})

suite.add('C++ insert and traverse 10000 linked list nodes', function() {
  cppReference.linkedListInsertTraverse(10000);
})

suite.add('JavAssembler traverse array of length 10000', function() {
  wasmInstance.reset_allocator();
  wasmInstance.Benchmarks_traverseArray(10000);
})

suite.add('JavaScript traverse array of length 10000', function() {
  jsReference.traverseArray(10000);
})

suite.add('C++ traverse array of length 10000', function() {
  cppReference.traverseArray(10000);
})

suite.add('JavAssembler traverse array of length 100000', function() {
  wasmInstance.reset_allocator();
  wasmInstance.Benchmarks_traverseArray(100000);
})

suite.add('JavaScript traverse array of length 100000', function() {
  jsReference.traverseArray(100000);
})

suite.add('C++ traverse array of length 100000', function() {
  cppReference.traverseArray(100000);
})

const watPath = path.resolve(__dirname, '..', 'sample_programs_compiled', 'javassembler_benchmarks.wat');
const watBuffer = fs.readFileSync(watPath, 'utf8');
const wasmModule = wabt.parseWat(watPath, watBuffer);
const { buffer } = wasmModule.toBinary({});
let wasmInstance;

function runBenchmarks() {
  WebAssembly.compile(buffer).then(themodule => {
    WebAssembly.instantiate(themodule).then(instance => {
      wasmInstance = instance.exports;
      jsReference.linkedListInsertTraverse(1000);
      suite.run();
    })
  })
}

cppModule.onRuntimeInitialized = runBenchmarks;
