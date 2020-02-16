package util;

import ast.types.JavaClass;
import codegen.CodeGenUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Wraps a combined virtual table and a map so that that the start index of
 * the virtual table of each class can be quickly looked up.
 */
public class VirtualTable {

    private List<Integer> entries;
    private Map<JavaClass, Integer> startIndexMap;

    public VirtualTable(List<Integer> entries, Map<JavaClass, Integer> startIndexMap) {
        this.entries = entries;
        this.startIndexMap = startIndexMap;
    }

    public List<Integer> getEntries() {
        return entries;
    }

    public List<String> getEntriesSymbolic(FunctionTable functionTable) {
        return entries
                .stream()
                .map(functionTable::getEntry)
                .map(entry -> CodeGenUtil.getFunctionNameForOutput(entry, functionTable))
                .collect(Collectors.toList());
    }

    public int getVirtualTablePosition(JavaClass javaClass) {
        return startIndexMap.get(javaClass);
    }
}
