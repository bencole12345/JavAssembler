package ast.types;

import errors.DuplicateClassAttributeException;
import errors.DuplicateFunctionSignatureException;
import util.ClassTable;
import util.FunctionTable;
import util.FunctionTableEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a class with currently unbound generic type arguments.
 *
 * A GenericTypedJavaClass cannot be instantiated to produce an object; instead,
 * it should be instantiated statically to yield a JavaClass object using a
 * list of Type arguments.
 */
public class GenericJavaClass extends JavaClass {

    /**
     * Records the index of each generic type.
     *
     * For example, if the class were
     *   public class MyClass<T, U> { ... }
     * then the map would be { T -> 0, U -> 1 }.
     */
    private Map<String, Integer> genericTypesIndexMap;

    private List<JavaClass.ClassAttribute> attributes;
    private List<FunctionTableEntry> constructors;
    private List<FunctionTableEntry> methods;

    public GenericJavaClass(String name,
                            List<JavaClass.ClassAttribute> attributes,
                            JavaClass parent,
                            List<String> genericTypeNames)
            throws DuplicateClassAttributeException {
        super(name, attributes, parent);
        this.name = name;
        this.parent = parent;
        this.attributes = attributes;
        genericTypesIndexMap = new HashMap<>();
        for (int i = 0; i < genericTypeNames.size(); i++) {
            String genericTypeName = genericTypeNames.get(i);
            genericTypesIndexMap.put(genericTypeName, i);
        }
        constructors = new ArrayList<>();
        methods = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getNumberOfTypeArguments() {
        return genericTypesIndexMap.size();
    }

    public Map<String, Integer> getGenericTypesIndexMap() {
        return genericTypesIndexMap;
    }

    @Override
    public void registerNewConstructor(List<Type> parameterTypes, FunctionTableEntry functionTableEntry) {
        constructors.add(functionTableEntry);
    }

    @Override
    public void registerNewMethod(List<Type> parameterTypes, Type returnType, FunctionTableEntry functionTableEntry) {
        methods.add(functionTableEntry);
        virtualTable.add(functionTableEntry);
    }

    public JavaClass instantiate(List<HeapObjectReference> typeArguments,
                                 FunctionTable functionTable,
                                 ClassTable classTable)
            throws DuplicateClassAttributeException, DuplicateFunctionSignatureException {
        String instantiatedName = name
                + "<" + typeArguments
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","))
                + ">";
        List<JavaClass.ClassAttribute> instantiatedAttributes =
                attributes
                .stream()
                .map(attribute -> {
                    Type type = attribute.getType();
                    if (type instanceof GenericType) {
                        GenericType genericType = (GenericType) type;
                        int index = genericTypesIndexMap.get(genericType.getName());
                        type = typeArguments.get(index);
                        String name = attribute.getName();
                        AccessModifier accessModifier = attribute.getAccessModifier();
                        attribute = new JavaClass.ClassAttribute(name, type, accessModifier);
                    }
                    return attribute;
                })
                .collect(Collectors.toList());

        // Create the class object
        JavaClass javaClass = new JavaClass(instantiatedName, instantiatedAttributes, parent, this);

        // Register all the parameters with the parameter types instantiated
        for (FunctionTableEntry constructor : constructors) {
            List<Type> constructorParams = constructor.getParameterTypes();
            List<Type> instantiatedTypes = replaceParameterisedTypes(constructorParams, typeArguments);
            FunctionTableEntry newEntry = functionTable.registerFunction(
                    javaClass,
                    constructor.getFunctionName(),
                    instantiatedTypes,
                    constructor.getReturnType(),
                    constructor.getIsStatic(),
                    constructor.getAccessModifier()
            );
            javaClass.registerNewConstructor(instantiatedTypes, newEntry);
        }

        // Register all the methods with the parameter types and return type instantiated
        for (FunctionTableEntry method : methods) {
            List<Type> paramTypes = method.getParameterTypes();
            List<Type> instantiatedTypes = replaceParameterisedTypes(paramTypes, typeArguments);
            Type instantiatedReturnType = replaceParameterisedType(method.getReturnType(), typeArguments);
            FunctionTableEntry newEntry = functionTable.registerFunction(
                    javaClass,
                    method.getFunctionName(),
                    instantiatedTypes,
                    instantiatedReturnType,
                    method.getIsStatic(),
                    method.getAccessModifier()
            );
            javaClass.registerNewMethod(instantiatedTypes, instantiatedReturnType, newEntry);
        }

        // Register this instantiation with the class table
        classTable.registerInstantiation(this, javaClass);

        return javaClass;
    }

    private List<Type> replaceParameterisedTypes(List<Type> parameterTypes, List<HeapObjectReference> typeArguments) {
        return parameterTypes
                .stream()
                .map(type -> replaceParameterisedType(type, typeArguments))
                .collect(Collectors.toList());
    }

    private Type replaceParameterisedType(Type type, List<HeapObjectReference> typeArguments) {
        if (type instanceof GenericType) {
            GenericType genericType = (GenericType) type;
            return typeArguments.get(genericType.getPositionInArguments());
        } else {
            return type;
        }
    }

    @Override
    public boolean isSubtypeOf(Type other) {
        return other.equals(this);
    }
}
