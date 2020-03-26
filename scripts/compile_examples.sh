#!/bin/bash

JAR_LOCATION=build/libs/JavAssembler-fat-1.0.jar

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
java -jar $JAR_LOCATION -i sample_programs/tests/Arrays.java sample_programs/tests/Child.java sample_programs/tests/Classes.java sample_programs/tests/DynamicPolymorphism.java sample_programs/tests/ExampleClass.java sample_programs/tests/Expressions.java sample_programs/tests/Functions.java sample_programs/tests/FunctionsExternal.java sample_programs/tests/Integer.java sample_programs/tests/LanguageConstructs.java sample_programs/tests/Parent.java -o sample_programs_compiled/tests.wat
echo "Generated sample_programs_compiled/tests.wat"

# Compile the benchmarks
echo "Compiling benchmarks..."
java -jar $JAR_LOCATION -i sample_programs/benchmarks/Benchmarks.java -o sample_programs_compiled/benchmarks.wat
echo "Generated sample_programs_compiled/benchmarks.wat"

echo "Done!"
