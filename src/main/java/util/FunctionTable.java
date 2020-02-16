package util;

import ast.types.AccessModifier;
import ast.types.JavaClass;
import ast.types.Type;
import ast.types.UnvalidatedJavaClassReference;
import errors.InvalidClassNameException;
import errors.UndeclaredFunctionException;
import errors.UnknownClassException;
import parser.ParserUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionTable {

    /**
     * The list of functions.
     */
    private List<FunctionTableEntry> functions;

    /**
     * Contains the map from
     *      class -> function name ->
     *      (parameter types list -> function table entry) trie
     */
    private Map<JavaClass, Map<String, LookupTrie<FunctionTableEntry, Type>>> classesToStaticFunctionsMap;

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

        // TODO: Handle case that a function with this signature has already been declared

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
     * @throws UndeclaredFunctionException
     */
    public FunctionTableEntry lookupFunction(JavaClass containingClass,
                                             String functionName,
                                             List<Type> parameterTypes)
            throws UndeclaredFunctionException, InvalidClassNameException {

        if (classesToStaticFunctionsMap == null) {
            classesToStaticFunctionsMap = buildStaticFunctionLookupTrie();
        }

        // Look up the map for the correct class
        Map<String, LookupTrie<FunctionTableEntry, Type>> functionNameMap =
                classesToStaticFunctionsMap.getOrDefault(containingClass, null);
        if (functionNameMap == null) {
            String message = "Invalid class name " + containingClass;
            throw new InvalidClassNameException(message);
        }

        // Look up the trie for the name within that class
        LookupTrie<FunctionTableEntry, Type> trie =
                functionNameMap.getOrDefault(functionName, null);

        // Throw an exception if there's no entry with that function name
        String errorMessage = "No function defined in class " + containingClass
                + " with signature " + functionSignatureToString(functionName, parameterTypes);
        if (trie == null)
            throw new UndeclaredFunctionException(errorMessage);

        // Call the lookup method on that trie
        FunctionTableEntry entry = trie.lookup(parameterTypes);

        // Throw an exception if there's no entry with those types as parameters
        if (entry == null)
            throw new UndeclaredFunctionException(errorMessage);

        return entry;
    }

    private Map<JavaClass, Map<String, LookupTrie<FunctionTableEntry, Type>>> buildStaticFunctionLookupTrie() {
        Map<JavaClass, Map<String, LookupTrie<FunctionTableEntry, Type>>> map = new HashMap<>();
        for (FunctionTableEntry entry : functions) {
            if (!map.containsKey(entry.getContainingClass())) {
                map.put(entry.getContainingClass(), new HashMap<>());
            }
            Map<String, LookupTrie<FunctionTableEntry, Type>> nameMap = map.get(entry.getContainingClass());
            if (!nameMap.containsKey(entry.getFunctionName())) {
                nameMap.put(entry.getFunctionName(), new LookupTrie<>());
            }
            LookupTrie<FunctionTableEntry, Type> trie = nameMap.get(entry.getFunctionName());
            boolean success = trie.insert(entry.getParameterTypes(), entry);
            // TODO: Handle failure (already an entry)
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

    private String functionSignatureToString(String name, List<Type> parameterTypes) {
        List<String> typeNames = parameterTypes.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        return name + "(" + String.join(", ", typeNames) + ")";
    }

    /**
     * Validates all types used in method signatures.
     *
     * @param classTable The class table to use for validation
     */
    public void validateAllTypes(ClassTable classTable) {
        for (FunctionTableEntry entry : functions) {
            List<Type> parameterTypes = entry.getParameterTypes();
            for (int i = 0; i < parameterTypes.size(); i++) {
                Type type = parameterTypes.get(i);
                if (type instanceof UnvalidatedJavaClassReference) {
                    UnvalidatedJavaClassReference unvalidatedReference = (UnvalidatedJavaClassReference) type;
                    try {
                        JavaClass validatedClass = classTable.lookupClass(unvalidatedReference.getClassName());
                        parameterTypes.set(i, validatedClass);
                    } catch (UnknownClassException e) {
                        ParserUtil.reportError(e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * @return A list of all functions in the table
     */
    public List<FunctionTableEntry> getFunctions() {
        return functions;
    }
}
