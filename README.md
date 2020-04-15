# JavAssembler

This is the official repository for my Cambridge Computer Science Tripos Part II project. JavAssembler is a compiler from Java to WebAssembly.

## How to Compile JavAssembler
To compile JavAssembler, you can use the Gradle task
```
$ ./gradlew buildFatJar
```
This requires that you have a Java compiler installed. The resulting Jar file will be located at `build/libs/JavAssembler-fat-1.0.jar`.

## How to Run JavAssembler
The format for running JavAssembler is
```
$ java -jar /path/to/jar.jar -i <list of all .java files to compile> -o <output file>.wat
```
For example, the command used by the `compileExamples` script to compile the benchmark files is
```
$ java -jar build/libs/JavAssembler-fat-1.0.jar -i sample_programs/benchmarks/Benchmarks.java sample_programs/benchmarks/util/LinkedList.java sample_programs/benchmarks/util/LinkedListNode.java -o sample_programs_compiled/benchmarks.wat
```

## Tests and Benchmarks
There are four scripts provided to simplify the task of running the tests and benchmarks. Before using the scripts, you must have installed:

- [NodeJS](https://nodejs.org/en/) and the `npm` package manager
- [Emscripten](https://emscripten.org/) (C/C++ to WebAssembly compiler) with the command `em++` available on your PATH

To run the scripts:

```bash
# Compile JavAssembler
$ ./gradlew buildFatJar

# Run unit tests
$ ./gradlew test

# Install JavaScript dependencies
$ npm install

# Use JavAssembler to compile the tests and benchmarks (requires ./gradlew buildFatJar)
$ npm run compileExamples

# Run the JavaScript tests (requires compileExamples)
$ npm test

# Run the benchmarks (requires compileExamples)
$ npm run benchmark
```

The JavaScript tests can be found in the `tests` directory. The benchmarks can be found in `scripts/run_benchmarks.js`.
