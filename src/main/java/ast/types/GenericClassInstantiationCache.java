package ast.types;

import errors.JavAssemblerException;
import util.ClassTable;
import util.ErrorReporting;
import util.FunctionTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages instantiating generic classes with type arguments. A JavaClass object
 * is built for each generic class, for each sequence of type arguments that
 * are used in the program.
 */
public class GenericClassInstantiationCache {

    private static GenericClassInstantiationCache INSTANCE;

    private FunctionTable functionTable;
    private ClassTable classTable;

    /**
     * The cache of all instantiations
     */
    Map<GenericJavaClass, Map<List<HeapObjectReference>,JavaClass>> cache;

    private GenericClassInstantiationCache() {
        cache = new HashMap<>();
    }

    public static GenericClassInstantiationCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GenericClassInstantiationCache();
        }
        return INSTANCE;
    }

    public void setFunctionTable(FunctionTable functionTable) {
        this.functionTable = functionTable;
    }

    public void setClassTable(ClassTable classTable) {
        this.classTable = classTable;
    }

    /**
     * Instantiates a generic class with given type arguments, using a cached
     * one if it exists.
     *
     * @param genericClass The generic class to instantiate with type arguments
     * @param typeArguments The type arguments to use
     * @return The actual class that was instantiated
     */
    public JavaClass getActualClass(GenericJavaClass genericClass, List<HeapObjectReference> typeArguments) {
        Map<List<HeapObjectReference>,JavaClass> mapThisClass;
        if (cache.containsKey(genericClass)) {
            mapThisClass = cache.get(genericClass);
        } else {
            mapThisClass = new HashMap<>();
            cache.put(genericClass, mapThisClass);
        }
        JavaClass javaClass = null;
        if (mapThisClass.containsKey(typeArguments)) {
            javaClass = mapThisClass.get(typeArguments);
        } else {
            try {
                javaClass = genericClass.instantiate(typeArguments, functionTable, classTable);
            } catch (JavAssemblerException e) {
                ErrorReporting.reportError(e.getMessage());
            }
            mapThisClass.put(typeArguments, javaClass);
        }
        return javaClass;
    }

}
