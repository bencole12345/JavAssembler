package util;

import ast.types.AccessModifier;
import ast.types.JavaClass;
import ast.types.Type;
import errors.InvalidClassNameException;
import errors.UndeclaredFunctionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionTable {

    /**
     * The list of functions.
     */
    private List<FunctionTableEntry> functions;

    /**
     * Contains the map from
     *      class -> function name ->
     *      (parameter types list -> function table entry) tree
     */
    private Map<JavaClass, Map<String, LookupTree<FunctionTableEntry, Type>>> classesToStaticFunctionsMap;

    /**
     * Tracks the next index in the table that we are free to assign.
     */
    private int nextIndexToAssign;

    /**
     * Counts how many functions have been registered with a given name.
     *
     * This is used to determine whether or not to perform name mangling.
     */
    private Map<String, Integer> functionsWithNameCount;

    public FunctionTable() {
        functions = new ArrayList<>();
        classesToStaticFunctionsMap = null;
        functionsWithNameCount = new HashMap<>();
        nextIndexToAssign = 0;
    }

    /**
     * Looks up the entry at a given index.
     *
     * @param index The index to look up
     * @return The function table entry at that index
     */
    public FunctionTableEntry getEntry(int index) {
        if (index < functions.size())
            return functions.get(index);
        else
            return null;
    }

    /**
     * Adds a function to the function table.
     *
     * @param containingClass The class that contains this method
     * @param functionName The name of the function
     * @param parameterTypes The types of the function's parameters
     * @param returnType The return type of the function
     * @param isStatic Whether this is a static function
     * @param accessModifier The access modifier applied to this function
     * @return The function table entry that was created
     */
    public FunctionTableEntry registerFunction(JavaClass containingClass,
                                               String functionName,
                                               List<Type> parameterTypes,
                                               Type returnType,
                                               boolean isStatic,
                                               AccessModifier accessModifier) {

        // Create a function table entry for the new function
        FunctionTableEntry functionTableEntry = new FunctionTableEntry(
                nextIndexToAssign++,
                containingClass,
                functionName,
                returnType,
                isStatic,
                parameterTypes,
                accessModifier);

        // Add it to the table
        functions.add(functionTableEntry);

        if (isStatic) {
            int occurrences = functionsWithNameCount.getOrDefault(functionName, 0);
            functionsWithNameCount.put(functionName, occurrences + 1);
        }
        
        return functionTableEntry;
    }

    /**
     * Looks up a function from the registry.
     *
     * @param functionName The function to look up
     * @param parameterTypes The types of the function's parameters
     * @return The index of the function in the function table
     * @throws UndeclaredFunctionException if the function has not been registered
     */
    public FunctionTableEntry lookupFunction(JavaClass containingClass,
                                             String functionName,
                                             List<Type> parameterTypes)
            throws UndeclaredFunctionException, InvalidClassNameException {

        if (classesToStaticFunctionsMap == null) {
            classesToStaticFunctionsMap = buildStaticFunctionLookupTree();
        }

        // Look up the map for the correct class
        Map<String, LookupTree<FunctionTableEntry, Type>> functionNameMap =
                classesToStaticFunctionsMap.getOrDefault(containingClass, null);
        if (functionNameMap == null) {
            String message = "Invalid class name " + containingClass;
            throw new InvalidClassNameException(message);
        }

        // Look up the tree for the name within that class
        LookupTree<FunctionTableEntry, Type> lookupTree =
                functionNameMap.getOrDefault(functionName, null);

        // Throw an exception if there's no entry with that function name
        String errorMessage = "No function defined in class " + containingClass
                + " with signature " + ErrorReporting.getFunctionSignatureOutput(functionName, parameterTypes);
        if (lookupTree == null)
            throw new UndeclaredFunctionException(errorMessage);

        // Call the lookup method on that tree
        FunctionTableEntry entry = lookupTree.lookup(parameterTypes);

        // Throw an exception if there's no entry with those types as parameters
        if (entry == null)
            throw new UndeclaredFunctionException(errorMessage);

        return entry;
    }

    private Map<JavaClass, Map<String, LookupTree<FunctionTableEntry, Type>>> buildStaticFunctionLookupTree() {
        Map<JavaClass, Map<String, LookupTree<FunctionTableEntry, Type>>> map = new HashMap<>();
        for (FunctionTableEntry entry : functions) {
            if (!map.containsKey(entry.getContainingClass())) {
                map.put(entry.getContainingClass(), new HashMap<>());
            }
            Map<String, LookupTree<FunctionTableEntry, Type>> nameMap = map.get(entry.getContainingClass());
            if (!nameMap.containsKey(entry.getFunctionName())) {
                nameMap.put(entry.getFunctionName(), new LookupTree<>());
            }
            LookupTree<FunctionTableEntry, Type> lookupTree = nameMap.get(entry.getFunctionName());
            boolean success = lookupTree.insert(entry.getParameterTypes(), entry);
            if (!success) {
                String signature = entry.getQualifiedSignature();
                String errorMessage = "Duplicate functions with signature " + signature;
                ErrorReporting.reportError(errorMessage);
            }
        }
        return map;
    }

    /**
     * Returns the number of functions registered with a given name.
     *
     * @param name The name to look up
     * @return The number of functions registered with that name
     */
    public int getNumberOfFunctionsWithName(String name) {
        return functionsWithNameCount.getOrDefault(name, 0);
    }

    /**
     * Validates all types used in method signatures.
     *
     * @param classTable The class table to use for validation
     */
    public void validateAllTypes(ClassTable classTable) {
        functions.forEach(entry -> entry.validateTypes(classTable));
    }

    /**
     * @return A list of all functions in the table
     */
    public List<FunctionTableEntry> getFunctions() {
        return functions;
    }
}
