package util;

import ast.types.JavaClass;
import errors.DuplicateClassDefinitionException;
import errors.UnknownClassException;

import java.util.HashMap;
import java.util.Map;

public class ClassTable {

    private static Map<String, JavaClass> classes;

    public ClassTable() {
        classes = new HashMap<>();
    }

    public void registerClass(String name, JavaClass javaClass) throws DuplicateClassDefinitionException {
        if (classes.containsKey(name)) {
            String message = "Multiple definitions of class " + name;
            throw new DuplicateClassDefinitionException(message);
        }
        classes.put(name, javaClass);
    }

    public JavaClass lookupClass(String name) throws UnknownClassException {
        JavaClass javaClass = classes.get(name);
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
        for (JavaClass javaClass : classes.values()) {
            javaClass.validateAllClassReferences(this);
        }
    }

}