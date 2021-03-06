package util;

import ast.types.AccessModifier;
import ast.types.GenericJavaClass;
import ast.types.JavaClass;
import ast.types.Type;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains data about a single function.
 */
public class FunctionTableEntry {

    private int index;
    private JavaClass containingClass;
    private String functionName;
    private Type returnType;
    private boolean isStatic;
    private List<Type> parameterTypes;
    private AccessModifier accessModifier;

    public FunctionTableEntry(int index,
                              JavaClass containingClass,
                              String functionName,
                              Type returnType,
                              boolean isStatic,
                              List<Type> parameterTypes,
                              AccessModifier accessModifier) {
        this.index = index;
        this.containingClass = containingClass;
        this.functionName = functionName;
        this.returnType = returnType;
        this.isStatic = isStatic;
        this.parameterTypes = parameterTypes;
        this.accessModifier = accessModifier;
    }

    public int getIndex() {
        return index;
    }

    public JavaClass getContainingClass() {
        return containingClass;
    }

    public String getFunctionName() {
        return functionName;
    }

    public Type getReturnType() {
        return returnType;
    }

    public boolean getIsStatic() {
        return isStatic;
    }

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    /**
     * Determines whether the method referenced by this table entry can be
     * executed from the given context.
     *
     * If the method is public then this will always return true. For private
     * methods, it will return true only if the function is called from the
     * same class.
     *
     * @param javaClass The class from which the method is called
     * @return true if the method can legally be executed; false otherwise
     */
    public boolean canBeCalledFrom(JavaClass javaClass) {
        if (accessModifier.equals(AccessModifier.PUBLIC))
            return true;
        else
            return javaClass.equals(containingClass);
    }

    public String getQualifiedName() {
        GenericJavaClass genericClass = containingClass.getGenericClass();
        JavaClass classToUse = (genericClass == null) ? containingClass : genericClass;
        return classToUse + "::" + functionName;
    }

    public String getQualifiedSignature() {
        List<String> typeStrings = parameterTypes
                .stream()
                .map(Type::toString)
                .collect(Collectors.toList());
        return getQualifiedName() + "(" + String.join(", ", typeStrings) + ")";
    }

    public void validateTypes(ClassTable classTable) {
        for (int i = 0; i < parameterTypes.size(); i++) {
            Type type = parameterTypes.get(i);
            Type validated = classTable.validateType(type);
            parameterTypes.set(i, validated);
        }
        returnType = classTable.validateType(returnType);
    }
}
