package util;

import ast.types.*;
import errors.DuplicateClassDefinitionException;
import errors.UnknownClassException;

import java.util.*;

public class ClassTable {

    private List<JavaClass> classes;
    private Map<String, JavaClass> classesNameMap;
    private Map<GenericJavaClass, Set<JavaClass>> instantiations;

    public ClassTable() {
        classes = new ArrayList<>();
        classesNameMap = new HashMap<>();
        instantiations = new HashMap<>();
    }

    public void registerClass(String name, JavaClass javaClass) throws DuplicateClassDefinitionException {
        if (classesNameMap.containsKey(name)) {
            String message = "Multiple definitions of class " + name;
            throw new DuplicateClassDefinitionException(message);
        }
        classes.add(javaClass);
        classesNameMap.put(name, javaClass);
    }

    public void registerInstantiation(GenericJavaClass genericClass, JavaClass instantiation) {
        if (!instantiations.containsKey(genericClass)) {
            instantiations.put(genericClass, new HashSet<>());
        }
        Set<JavaClass> set = instantiations.get(genericClass);
        set.add(instantiation);
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
    public void validateAllTypes() {
        for (JavaClass javaClass : classes) {
            javaClass.validateAllAttributeTypes(this);
        }
    }

    public Type validateType(Type toValidate) {
        Type validated = toValidate;
        if (toValidate instanceof UnvalidatedJavaClassReference) {
            UnvalidatedJavaClassReference unvalidatedReference = (UnvalidatedJavaClassReference) toValidate;
            try {
                validated = lookupClass(unvalidatedReference.getClassName());
            } catch (UnknownClassException e) {
                ErrorReporting.reportError(e.getMessage());
            }
        } else if (toValidate instanceof ItemArray) {
            ItemArray array = (ItemArray) toValidate;
            Type elementType = array.getElementType();
            Type validatedElementType = validateType(elementType);
            validated = new ItemArray(validatedElementType);
        }
        return validated;
    }

    /**
     * Builds a virtual table by concatenating the virtual table of every class.
     *
     * @return The combined virtual table
     */
    public VirtualTable buildCombinedVirtualTable() {
        List<Integer> table = new ArrayList<>();
        Map<JavaClass, Integer> startIndexMap = new HashMap<>();
        for (JavaClass javaClass : classes) {
            List<Integer> virtualTable = javaClass.getVirtualTable();
            int startIndex = table.size();
            startIndexMap.put(javaClass, startIndex);
            if (javaClass instanceof GenericJavaClass) {
                for (JavaClass instantiation : instantiations.get(javaClass)) {
                    startIndexMap.put(instantiation, startIndex);
                }
            }
            table.addAll(virtualTable);
        }
        return new VirtualTable(table, startIndexMap);
    }
}
