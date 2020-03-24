const { readFileSync } = require("fs");

const loadModule = async (filepath) => {
    const testFilePath = filepath;
    const buffer = readFileSync(testFilePath);
    const module = await WebAssembly.compile(buffer);
    const instance = await WebAssembly.instantiate(module);
    return instance.exports;
};

const runTests = async () => {
    const module = await loadModule(process.argv[2]);

    // Test while loop
    let result = module.Test_sumSquaresWhile(10);
    let passFail = result == 385 ? "PASS" : "FAIL";
    console.log(passFail + ": While loop test");

    // Test for loop
    result = module.Test_sumSquaresForLoop(10);
    passFail = result == 385 ? "PASS" : "FAIL";
    console.log(passFail + ": For loop test");

    // Test if statement
    result = module.Test_isAbove100IfStatement(101);
    passFail = result == true ? "PASS" : "FAIL";
    console.log(passFail + ": If statement positive test");
    result = module.Test_isAbove100IfStatement(100);
    passFail = result == false ? "PASS" : "FAIL";
    console.log(passFail + ": If statement negative test");

    // Test boolean expression
    result = module.Test_isAbove100BooleanExpression(101)
    passFail = result == true ? "PASS" : "FAIL";
    console.log(passFail + ": Boolean expression positive test");
    result = module.Test_isAbove100BooleanExpression(100);
    passFail = result == false ? "PASS" : "FAIL";
    console.log(passFail + ": Boolean expression negative test");

    // Test expression parsing
    result = module.Test_testExpressionParsing();
    passFail = result ? "PASS" : "FAIL";
    console.log(passFail + ": Expression parsing order");

    // Test Boolean logical operators
    result = module.Test_testBooleanLogicalOperators();
    passFail = result ? "PASS" : "FAIL";
    console.log(passFail + ": Boolean logical operators");

    // Test call other function
    result = module.Test_callOtherFunction();
    passFail = result == 10.0 ? "PASS" : "FAIL";
    console.log(passFail + ": Call other function test");

    // Test private function not exported
    passFail = module.Test_otherFunction == null ? "PASS" : "FAIL";
    console.log(passFail + ": Private function test");

    // Test can call functions defined in other classes
    result = module.Test_callFunctionFromOtherClass();
    passFail = result == 42 ? "PASS" : "FAIL";
    console.log(passFail + ": Call function in another class test");

    // Test dynamically allocating memory
    result = module.Test_dynamicallyAllocateMemory();
    passFail = result == 12 ? "PASS" : "FAIL";
    console.log(passFail + ": Dynamic memory allocation test");

    // Test dynamic method dispatch
    result = module.Test_callMethod();
    passFail = result == 20 ? "PASS" : "FAIL";
    console.log(passFail + ": Dynamic method dispatch test");

    // Test 'this' keyword
    result = module.Test_testThis();
    passFail = result == 2 ? "PASS" : "FAIL";
    console.log(passFail + ": 'this' keyword test");

    // Test array set/get
    result = module.Test_testArrays();
    passFail = result ? "PASS" : "FAIL";
    console.log(passFail + ": Array set/get");

    // Test nested arrays
    result = module.Test_testNestedArrays();
    passFail = result ? "PASS" : "FAIL";
    console.log(passFail + ": Nested arrays");

    // Test passing array as reference
    result = module.Test_testPassArrayReference();
    passFail = result ? "PASS" : "FAIL";
    console.log(passFail + ": Pass array as reference");

    // Test constructors
    result = module.Test_testConstructor();
    passFail = result ? "PASS" : "FAIL";
    console.log(passFail + ": Constructors");
};

runTests();
