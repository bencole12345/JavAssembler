package util;

import parser.JavaFileParser;

/**
 * Wraps methods and constructors as a single type so that they can be stored
 * in a single list.
 */
public class SubroutineToCompile {

    private JavaFileParser.MethodDefinitionContext methodDefinition;
    private JavaFileParser.ConstructorDefinitionContext constructorDefinition;
    private SubroutineType subroutineType;

    private enum SubroutineType {
        Method,
        Constructor
    }

    public SubroutineToCompile(JavaFileParser.MethodDefinitionContext methodDefinition) {
        this.methodDefinition = methodDefinition;
        subroutineType = SubroutineType.Method;
    }

    public SubroutineToCompile(JavaFileParser.ConstructorDefinitionContext constructorDefinition) {
        this.constructorDefinition = constructorDefinition;
        subroutineType = SubroutineType.Constructor;
    }

    public boolean isMethod() {
        return subroutineType == SubroutineType.Method;
    }

    public boolean isConstructor() {
        return subroutineType == SubroutineType.Constructor;
    }

    public JavaFileParser.MethodDefinitionContext getMethodDefinition() {
        if (isMethod()) {
            return methodDefinition;
        } else {
            return null;
        }
    }

    public JavaFileParser.ConstructorDefinitionContext getConstructorDefinition() {
        if (isConstructor()) {
            return constructorDefinition;
        } else {
            return null;
        }
    }
}
