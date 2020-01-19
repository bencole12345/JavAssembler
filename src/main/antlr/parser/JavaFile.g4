grammar JavaFile;

import JavaFileTokens;

@header{
package parser;
}

file
    : imports classDefinition EOF
    ;

imports
    :       # NoImports
    |       # SomeImports
    ;

classDefinition
    : accessModifier?
            CLASS IDENTIFIER
            LBRACE classItem* RBRACE
    ;

classItem
    : classAttributeDeclaration     # ClassAttribute
    | methodDefinition              # ClassMethod
    ;

classAttributeDeclaration
    : accessModifier type IDENTIFIER SEMICOLON
    ;

statement
    : variableDeclarationAndAssignment SEMICOLON   # DeclarationAssignmentStatement
    | variableAssignment SEMICOLON                 # AssignmentStatement
    | variableDeclaration SEMICOLON                # DeclarationStatement
    | RETURN expr SEMICOLON                        # ReturnStatement
    | variableIncrementExpr                        # VariableIncrementStatement
    | ifStatement                                  # IfStatementWrap
    | whileLoop                                    # WhileLoopWrap
    | forLoop                                      # ForLoopWrap
    | functionCall SEMICOLON                       # FunctionCallStatement
    | SEMICOLON                                    # EmptyStatement
    ;

variableDeclaration
    : type IDENTIFIER
    ;

variableAssignment
    : IDENTIFIER op=(EQUALS
        | PLUS_EQUALS
        | MINUS_EQUALS
        | MULTIPLY_EQUALS
        | DIVIDE_EQUALS) expr
    ;

variableDeclarationAndAssignment
    : type IDENTIFIER EQUALS expr
    ;

expr
    : literal                                       # LiteralExpr
    | functionCall                                  # FunctionCallExpr
    | LPAREN expr RPAREN                            # ParenthesesExpr
    | MINUS expr                                    # NegateExpr
    | NOT expr                                      # NotExpr
    | variableIncrementExpr                         # IncrementExpr
    | expr op=(MULTIPLY|DIVIDE) expr                # InfixExpr
    | expr op=(PLUS|MINUS) expr                     # InfixExpr
    | expr op=(EQUAL_TO
                | NOT_EQUAL_TO
                | LESS_THAN
                | LESS_THAN_EQUAL_TO
                | GREATER_THAN
                | GREATER_THAN_EQUAL_TO) expr       # InfixExpr
    | expr QUESTION_MARK expr COLON expr SEMICOLON  # BinarySelectorExpr
    | IDENTIFIER                                    # VariableNameExpr
    ;

variableIncrementExpr
    : op=(INCREMENT|DECREMENT) IDENTIFIER           # PreIncrementExpr
    | IDENTIFIER op=(INCREMENT|DECREMENT)           # PostIncrementExpr
    ;

functionCall
    : IDENTIFIER LPAREN functionArgs RPAREN
    ;

functionArgs
    :                       # NoArgs
    | expr (COMMA expr)*    # SomeArgs
    ;

methodDefinition
    : accessModifier? STATIC? type IDENTIFIER LPAREN methodParams RPAREN codeBlock
    ;

methodParams
    :                                           # NoParams
    | type IDENTIFIER (COMMA type IDENTIFIER)*  # SomeParams
    ;

codeBlock
    : LBRACE statement* RBRACE                  # StatementList
    ;

ifStatement
    : IF LPAREN expr RPAREN codeBlock ELSE ifStatement   # IfElseIf
    | IF LPAREN expr RPAREN codeBlock ELSE codeBlock     # IfElse
    | IF LPAREN expr RPAREN codeBlock                    # If
    ;

whileLoop
    : WHILE LPAREN expr RPAREN codeBlock
    ;

forLoop
    : FOR LPAREN forLoopInitialiser? SEMICOLON
      forLoopCondition? SEMICOLON
      forLoopUpdater? RPAREN codeBlock
    ;

forLoopInitialiser
    : variableDeclarationAndAssignment      # ForLoopDeclareAndAssign
    | variableAssignment                    # ForLoopAssignOnly
    ;

forLoopCondition
    : expr
    ;

forLoopUpdater
    : expr
    ;

accessModifier
    : modifier=(PUBLIC|PRIVATE|PROTECTED)
    ;

type
    : VOID                                                             # VoidType
    | primitiveType=(INT|SHORT|LONG|BYTE|CHAR|BOOLEAN|FLOAT|DOUBLE)    # PrimitiveType
    | nonPrimitiveType=IDENTIFIER                                      # NonPrimitiveType
    ;

literal
    : SHORT_LITERAL
    | INT_LITERAL
    | LONG_LITERAL
    | FLOAT_LITERAL
    | DOUBLE_LITERAL
    | BOOLEAN_LITERAL
    ;
