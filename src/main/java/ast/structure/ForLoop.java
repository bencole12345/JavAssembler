package ast.structure;

import ast.expressions.Expression;

public class ForLoop extends Statement {

    // TODO: Consider very carefully how to handle scoping with regards to the loop variable
    // Eg if we have
    //      for (int i = 0; i < 10; i++) {
    //          ...
    //      }
    // We need can't just add i to the parent scope
    // Ideally it should be inserted into the for loop's code block

    private Statement initialiser;
    private Expression condition;
    private Expression updater;
    private CodeBlock codeBlock;

    public ForLoop(Statement initialiser, Expression condition, Expression updater, CodeBlock codeBlock) {
        this.initialiser = initialiser;
        this.condition = condition;
        this.updater = updater;
        this.codeBlock = codeBlock;
    }

    public Statement getInitialiser() {
        return initialiser;
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getUpdater() {
        return updater;
    }

    public CodeBlock getCodeBlock() {
        return codeBlock;
    }
}
