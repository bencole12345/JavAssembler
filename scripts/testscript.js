const { readFileSync } = require("fs");

const loadModule = async () => {
    const testFilePath = process.argv[2];
    const buffer = readFileSync(testFilePath);
    const module = await WebAssembly.compile(buffer);
    const instance = await WebAssembly.instantiate(module);
    return instance.exports;
};

const runTests = async () => {
    const module = await loadModule();

    // Test while loop
    let result = module.sumSquaresWhile(10);
    let passFail = result == 385 ? "PASS" : "FAIL";
    console.log(passFail + ": While loop test");
    
    // Test for loop
    result = module.sumSquaresForLoop(10);
    passFail = result == 385 ? "PASS" : "FAIL";
    console.log(passFail + ": For loop test");

    // Test if statement
    result = module.isAbove100IfStatement(101);
    passFail = result == true ? "PASS" : "FAIL";
    console.log(passFail + ": If statement positive test");
    result = module.isAbove100IfStatement(100);
    passFail = result == false ? "PASS" : "FAIL";
    console.log(passFail + ": If statement negative test");

    // Test boolean expression
    result = module.isAbove100BooleanExpression(101)
    passFail = result == true ? "PASS" : "FAIL";
    console.log(passFail + ": Boolean expression positive test");
    result = module.isAbove100BooleanExpression(100);
    passFail = result == false ? "PASS" : "FAIL";
    console.log(passFail + ": Boolean expression negative test");

    // Test call other function
    result = module.callOtherFunction();
    passFail = result == 10.0 ? "PASS" : "FAIL";
    console.log(passFail + ": Call other function test");

    // Test private function not exported
    passFail = module.otherFunction == null ? "PASS" : "FAIL";
    console.log(passFail + ": Private function test");
    
};

runTests();
