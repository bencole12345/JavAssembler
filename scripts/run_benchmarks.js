const fs = require('fs');
const path = require('path');
const wabt = require('wabt')();
const Benchmark = require('benchmark');
const CSVWriter = require('csv-writer');
const jsReference = require('../sample_programs/benchmarks/javascript/benchmarks');
const cppModule = require('../sample_programs_compiled/cpp_benchmarks');
const cppReference = {
  sumSquares: cppModule.cwrap('sumSquares', 'number', ['number']),
  recurse: cppModule.cwrap('recurse', null, ['number']),
  linkedListInsertTraverse: cppModule.cwrap('linkedListInsertTraverse', null, ['number']),
  traverseArray: cppModule.cwrap('traverseArray', null, ['number'])
};

const csvWriter = CSVWriter.createObjectCsvWriter({
  path: 'benchmarking_results.csv',
  header: [
    { id: 'benchmark', title: 'Benchmark' },
    { id: 'environment', title: 'Environment' },
    { id: 'size', title: 'Size' },
    { id: 'mean', title: 'Mean' },
    { id: 'deviation', title: 'Standard Deviation' }
  ]
});

let results = [];

let suite = new Benchmark.Suite;

suite.on('cycle', function(event) {
  console.log(String(event.target));
  console.log('         mean: ' + event.target.stats.mean);
  console.log('    deviation: ' + event.target.stats.deviation)
  const name = event.target.name;
  [benchmark, environment, size] = name.split(':').map(x => x.trim());
  results.push({
    benchmark: benchmark,
    environment: environment,
    size: size,
    mean: event.target.stats.mean,
    deviation: event.target.stats.deviation
  });
})

suite.on('complete', function(event) {
  csvWriter.writeRecords(results);
})

suite.add('Sum of squares: JavAssembler: 1000', function() {
  wasmInstance.Benchmarks_sumSquares(1000);
})

suite.add('Sum of squares: JavaScript: 1000', function() {
  jsReference.sumSquares(1000);
})

suite.add('Sum of squares: C++: 1000', function() {
  cppReference.sumSquares(1000);
})

suite.add('Sum of squares: JavAssembler: 10000', function() {
  wasmInstance.Benchmarks_sumSquares(10000);
})

suite.add('Sum of squares: JavaScript: 10000', function() {
  jsReference.sumSquares(10000);
})

suite.add('Sum of squares: C++: 10000', function() {
  cppReference.sumSquares(10000);
})

suite.add('Sum of squares: JavAssembler: 50000', function () {
  wasmInstance.Benchmarks_sumSquares(50000);
})

suite.add('Sum of squares: JavaScript: 50000', function () {
  jsReference.sumSquares(50000);
})

suite.add('Sum of squares: C++: 50000', function () {
  cppReference.sumSquares(50000);
})

suite.add('Recursion: JavAssembler: 1000', function() {
  wasmInstance.Benchmarks_recurse(1000);
})

suite.add('Recursion: JavaScript: 1000', function() {
  jsReference.recurse(1000);
})

suite.add('Recursion: C++: 1000', function() {
  cppReference.recurse(1000);
})

suite.add('Recursion: JavAssembler: 10000', function () {
  wasmInstance.Benchmarks_recurse(10000);
})

suite.add('Recursion: JavaScript: 10000', function() {
  jsReference.recurse(10000);
})

suite.add('Recursion: C++: 10000', function() {
  cppReference.recurse(10000);
})

suite.add('Recursion: JavAssembler: 50000', function () {
  wasmInstance.Benchmarks_recurse(50000);
})

suite.add('Recursion: JavaScript: 50000', function () {
  jsReference.recurse(50000);
})

suite.add('Recursion: C++: 50000', function () {
  cppReference.recurse(50000);
})

suite.add('Linked list traversal: JavAssembler: 1000', function() {
  wasmInstance.Benchmarks_linkedListInsertTraverse(1000);
})

suite.add('Linked list traversal: JavaScript: 1000', function() {
  jsReference.linkedListInsertTraverse(1000);
})

suite.add('Linked list traversal: C++: 1000', function() {
  cppReference.linkedListInsertTraverse(1000);
})

suite.add('Linked list traversal: JavAssembler: 10000', function () {
  wasmInstance.Benchmarks_linkedListInsertTraverse(10000);
})

suite.add('Linked list traversal: JavaScript: 10000', function () {
  jsReference.linkedListInsertTraverse(10000);
})

suite.add('Linked list traversal: C++: 10000', function() {
  cppReference.linkedListInsertTraverse(10000);
})

suite.add('Linked list traversal: JavAssembler: 100000', function () {
  wasmInstance.Benchmarks_linkedListInsertTraverse(100000);
})

suite.add('Linked list traversal: JavaScript: 100000', function () {
  jsReference.linkedListInsertTraverse(100000);
})

suite.add('Linked list traversal: C++: 100000', function () {
  cppReference.linkedListInsertTraverse(100000);
})

suite.add('Array traversal: JavAssembler: 1000', function() {
  wasmInstance.Benchmarks_traverseArray(1000);
})

suite.add('Array traversal: JavaScript: 1000', function() {
  jsReference.traverseArray(1000);
})

suite.add('Array traversal: JavAssembler: 10000', function() {
  wasmInstance.reset_allocator();
  wasmInstance.Benchmarks_traverseArray(10000);
})

suite.add('Array traversal: JavaScript: 10000', function() {
  jsReference.traverseArray(10000);
})

suite.add('Array traversal: C++: 10000', function() {
  cppReference.traverseArray(10000);
})

suite.add('Array traversal: JavAssembler: 100000', function() {
  wasmInstance.Benchmarks_traverseArray(100000);
})

suite.add('Array traversal: JavaScript: 100000', function() {
  jsReference.traverseArray(100000);
})

suite.add('Array traversal: C++: 100000', function() {
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
