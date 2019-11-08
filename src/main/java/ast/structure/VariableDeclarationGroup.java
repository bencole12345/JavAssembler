package ast.structure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Handles grouping variable declarations.
 *
 * This class maintains the invariant that there should never be two declarations for the
 * same variable name within the same group.
 */
public class VariableDeclarationGroup {

    /**
     * This is mapping of "variable name" -> the declaration for that variable
     *
     * There is an invariant that there should never be two declarations for
     * the same variable name at any time.
     */
    private Map<String, VariableDeclaration> declarations;

    public VariableDeclarationGroup() {
        declarations = new HashMap<>();
    }

    public Collection<VariableDeclaration> getDeclarations() {
        return declarations.values();
    }

    public void addDeclaration(VariableDeclaration declaration) throws MultipleDeclarationsException {
        if (declarations.containsKey(declaration.getVariableName())) {
            throw new MultipleDeclarationsException();
        }
        declarations.put(declaration.getVariableName(), declaration);
    }

    /**
     * Indicates that there have been multiple declarations for the same variable.
     */
    public static class MultipleDeclarationsException extends Exception {}

}
