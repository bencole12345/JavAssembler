// const fs = require('fs');
import fs from 'fs';
// const path = require('path');
import path from 'path';
// const wabt = require('wabt')();
import wabt_ from 'wabt';
const wabt = wabt_();
// const Benchmark = require('benchmark');
import Benchmark from 'benchmark';
// const CSVWriter = require('csv-writer');
import CSVWriter from 'csv-writer';
// const jsReference = require('../sample_programs/benchmarks/javascript/benchmarks');
import jsReference from '../sample_programs/benchmarks/javascript/benchmarks.js';
// const cppModule = require('../sample_programs_compiled/cpp_benchmarks');
import cppModule from '../sample_programs_compiled/cpp_benchmarks.js';
const cppReference = {
  sumSquares: cppModule.cwrap('sumSquares', 'number', ['number']),
  recurse: cppModule.cwrap('recurse', null, ['number']),
  linkedListInsertTraverse: cppModule.cwrap('linkedListInsertTraverse', null, ['number']),
  traverseArray: cppModule.cwrap('traverseArray', null, ['number'])
};

const CSV_FILE = 'benchmarking_results/benchmarking_results.csv';

const csvWriter = CSVWriter.createObjectCsvWriter({
  path: CSV_FILE,
  header: [
    { id: 'benchmark', title: 'Benchmark' },
    { id: 'environment', title: 'Environment' },
    { id: 'size', title: 'Size' },
    { id: 'mean', title: 'Mean (ms)' },
    { id: 'deviation', title: 'Standard Deviation (ms)' }
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
    mean: event.target.stats.mean * 1000,  // in ms
    deviation: event.target.stats.deviation * 1000  // in ms
  });
})

suite.on('complete', function(event) {
  csvWriter.writeRecords(results);
  console.log('Wrote ' + CSV_FILE);
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

suite.add('Recursion: JavAssembler: 100', function() {
  wasmInstance.Benchmarks_recurse(100);
})

suite.add('Recursion: JavaScript: 100', function() {
  jsReference.recurse(100);
})

suite.add('Recursion: C++: 100', function() {
  cppReference.recurse(100);
})

suite.add('Recursion: JavAssembler: 1000', function () {
  wasmInstance.Benchmarks_recurse(1000);
})

suite.add('Recursion: JavaScript: 1000', function () {
  jsReference.recurse(1000);
})

suite.add('Recursion: C++: 1000', function () {
  cppReference.recurse(1000);
})

suite.add('Recursion: JavAssembler: 5000', function () {
  wasmInstance.Benchmarks_recurse(5000);
})

suite.add('Recursion: JavaScript: 5000', function() {
  jsReference.recurse(5000);
})

suite.add('Recursion: C++: 5000', function() {
  cppReference.recurse(5000);
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

suite.add('Linked list traversal: JavAssembler: 20000', function () {
  wasmInstance.Benchmarks_linkedListInsertTraverse(20000);
})

suite.add('Linked list traversal: JavaScript: 20000', function () {
  jsReference.linkedListInsertTraverse(20000);
})

suite.add('Linked list traversal: C++: 20000', function () {
  cppReference.linkedListInsertTraverse(20000);
})

suite.add('Array traversal: JavAssembler: 1000', function() {
  wasmInstance.Benchmarks_traverseArray(1000);
})

suite.add('Array traversal: JavaScript: 1000', function() {
  jsReference.traverseArray(1000);
})

suite.add('Array traversal: C++: 1000', function() {
  cppReference.traverseArray(1000);
})

suite.add('Array traversal: JavAssembler: 10000', function() {
  wasmInstance.Benchmarks_traverseArray(10000);
})

suite.add('Array traversal: JavaScript: 10000', function() {
  jsReference.traverseArray(10000);
})

suite.add('Array traversal: C++: 10000', function() {
  cppReference.traverseArray(10000);
})

suite.add('Array traversal: JavAssembler: 20000', function() {
  wasmInstance.Benchmarks_traverseArray(20000);
})

suite.add('Array traversal: JavaScript: 20000', function() {
  jsReference.traverseArray(20000);
})

suite.add('Array traversal: C++: 20000', function() {
  cppReference.traverseArray(20000);
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
      wasmInstance.Benchmarks_traverseArray(50000);
      suite.run();
    })
  })
}

cppModule.onRuntimeInitialized = runBenchmarks;
