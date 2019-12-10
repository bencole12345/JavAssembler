package codegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionTable {

    // TODO: Include the whole function signature in here, not just name
    // (can have multiple methods with same name but different params)

    private List<String> functionTable;
    private Map<String, Integer> functionLookupCache;
    private int nextIndexToAssign;

    public FunctionTable() {
        functionTable = new ArrayList<>();
        functionLookupCache = new HashMap<>();
        nextIndexToAssign = 0;
    }

    /**
     * Adds a function to the function table.
     *
     * @param functionName The name of the function.
     * @return The index assigned to the function
     * @throws DuplicateFunctionSignatureException if a function with this signature has already
     *      been declared.
     */
    public int registerFunction(String functionName) throws DuplicateFunctionSignatureException {
        if (functionLookupCache.containsKey(functionName)) {
            throw new DuplicateFunctionSignatureException();
        }
        functionTable.add(functionName);
        functionLookupCache.put(functionName, nextIndexToAssign);
        return nextIndexToAssign++;
    }

    /**
     * Looks up a function from the registry.
     *
     * @param name The function to look up
     * @return The index of the function in the function table
     * @throws FunctionNotRegisteredException
     */
    public int lookupFunction(String name) throws FunctionNotRegisteredException {
        Integer index = functionLookupCache.get(name);
        if (index == null) {
            throw new FunctionNotRegisteredException();
        }
        return index;
    }

    public static class DuplicateFunctionSignatureException extends Exception {}

    public static class FunctionNotRegisteredException extends Exception {}
}
