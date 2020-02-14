package util;

import ast.types.JavaClass;
import errors.DuplicateClassDefinitionException;
import errors.UnknownClassException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassTable {

    private List<JavaClass> classes;
    private static Map<String, JavaClass> classesNameMap;

    public ClassTable() {
        classes = new ArrayList<>();
        classesNameMap = new HashMap<>();
    }

    public void registerClass(String name, JavaClass javaClass) throws DuplicateClassDefinitionException {
        if (classesNameMap.containsKey(name)) {
            String message = "Multiple definitions of class " + name;
            throw new DuplicateClassDefinitionException(message);
        }
        classes.add(javaClass);
        classesNameMap.put(name, javaClass);
    }

    public JavaClass lookupClass(String name) throws UnknownClassException {
        JavaClass javaClass = classesNameMap.get(name);
        if (javaClass == null) {
            String message = "Unknown class " + name;
            throw new UnknownClassException(message);
        }
        return javaClass;
    }

    /**
     * Forces all classes in the table to validate any unchecked class
     * references.
     */
    public void validateAllClassReferences() {
        for (JavaClass javaClass : classes) {
            javaClass.validateAllClassReferences(this);
        }
    }

}