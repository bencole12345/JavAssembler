#!/bin/bash

JAR_LOCATION=build/libs/JavAssembler-fat-1.0.jar
TESTS_DIR=sample_programs/tests
BENCHMARKS_DIR=sample_programs/benchmarks

# Make sure JavAssembler has been compiled
if [ ! -f $JAR_LOCATION ]
then
    echo "The required Jar file does not exist."
    echo "Compiling JavAssembler now..."
    ./gradlew buildFatJar
    echo "Created Jar file at $JAR_LOCATION"
else
    echo "Using $JAR_LOCATION"
fi

# Make sure the directory exists
[ ! -d ./sample_programs_compiled ] && mkdir ./sample_programs_compiled

# Compile the tests
echo "Compiling tests..."
java -jar $JAR_LOCATION -i \
    $TESTS_DIR/Arrays.java \
    $TESTS_DIR/Child.java \
    $TESTS_DIR/Classes.java \
    $TESTS_DIR/ClassWith33Attributes.java \
    $TESTS_DIR/DynamicPolymorphism.java \
    $TESTS_DIR/ExampleClass.java \
    $TESTS_DIR/Expressions.java \
    $TESTS_DIR/Functions.java \
    $TESTS_DIR/FunctionsExternal.java \
    $TESTS_DIR/Integer.java \
    $TESTS_DIR/LanguageConstructs.java \
    $TESTS_DIR/Parent.java \
    $TESTS_DIR/GarbageCollection.java \
    $TESTS_DIR/NullTest.java \
    $TESTS_DIR/TypeRanges.java \
    -o sample_programs_compiled/tests.wat
echo "Generated sample_programs_compiled/tests.wat"

# Compile the JavAssembler benchmarks
echo "Compiling JavAssembler benchmarks..."
java -jar $JAR_LOCATION -i \
    $BENCHMARKS_DIR/java/Benchmarks.java \
    $BENCHMARKS_DIR/java/LinkedList.java \
    $BENCHMARKS_DIR/java/LinkedListNode.java \
    -o sample_programs_compiled/javassembler_benchmarks.wat
echo "Generated sample_programs_compiled/javassembler_benchmarks.wat"

# Compile the C++ benchmarks
echo "Compiling C++ benchmarks..."
em++ $BENCHMARKS_DIR/cpp/linked_list.cpp \
    $BENCHMARKS_DIR/cpp/benchmarks.cpp \
    -s EXTRA_EXPORTED_RUNTIME_METHODS='["ccall", "cwrap"]' \
    -s ALLOW_MEMORY_GROWTH=1 \
    -o sample_programs_compiled/cpp_benchmarks.js
echo "Generated sample_programs_compiled/cpp_benchmarks.js"
echo "Generated sample_programs_compiled/cpp_benchmarks.wasm"

echo "Done!"
