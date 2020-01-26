package util;

import ast.types.JavaClass;
import errors.DuplicateClassDefinitionException;

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

    public JavaClass lookupClass(String name) {
        JavaClass javaClass = classes.get(name);
        if (javaClass == null) {
            // TODO: Throw an exception or something
            // (they looked up a class that hasn't been registered)
        }
        return javaClass;
    }

}