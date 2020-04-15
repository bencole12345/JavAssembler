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
            CLASS className=IDENTIFIER
            (EXTENDS parentClassName=IDENTIFIER)?
            LBRACE classItem* RBRACE
    ;

classItem
    : classAttributeDeclaration     # ClassAttribute
    | methodDefinition              # ClassMethod
    | constructorDefinition         # Constructor
    ;

classAttributeDeclaration
    : accessModifier type IDENTIFIER SEMICOLON
    ;

statement
    : variableDeclarationAndAssignment SEMICOLON   # DeclarationAssignmentStatement
    | variableDeclaration SEMICOLON                # DeclarationStatement
    | assignment SEMICOLON                         # AssignmentStatement
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

assignment
    : variableName
        op=(EQUALS
            | PLUS_EQUALS | MINUS_EQUALS
            | MULTIPLY_EQUALS | DIVIDE_EQUALS)
        expr                                        # VariableAssignment
    | variableName DOT IDENTIFIER
        op=(EQUALS
            | PLUS_EQUALS | MINUS_EQUALS
            | MULTIPLY_EQUALS | DIVIDE_EQUALS)
        expr                                        # AttributeAssignment
    | expr LSQBRACKET expr RSQBRACKET
        op=(EQUALS
            | PLUS_EQUALS | MINUS_EQUALS
            | MULTIPLY_EQUALS | DIVIDE_EQUALS)
        expr                                        # ArrayIndexAssignment
    ;

variableDeclarationAndAssignment
    : type IDENTIFIER EQUALS expr
    ;

expr
    : literal                                           # LiteralExpr
    | functionCall                                      # FunctionCallExpr
    | LPAREN expr RPAREN                                # ParenthesesExpr
    | MINUS expr                                        # NegateExpr
    | NOT expr                                          # NotExpr
    | NEW IDENTIFIER LPAREN functionArgs RPAREN         # NewObjectExpr
    | NEW type (LSQBRACKET expr RSQBRACKET)+            # NewArrayExpr
    | variableIncrementExpr                             # IncrementExpr
    | expr multiplicativeBop expr                       # MultiplicativeBopExpr
    | expr additiveBop expr                             # AdditiveBopExpr
    | expr comparisonBop expr                           # ComparisonBopExpr
    | expr logicalAndBop expr                           # LogicalAndBopExpr
    | expr logicalOrBop expr                            # LogicalOrBopExpr
    | expr QUESTION_MARK expr COLON expr                # BinarySelectorExpr
    | expr (LSQBRACKET expr RSQBRACKET)+                # ArrayLookupExpr
    | variableName DOT IDENTIFIER
            LPAREN functionArgs RPAREN                  # MethodCallExpr
    | variableName DOT variableName                     # AttributeLookupExpr
    | variableName                                      # VariableNameExpr
    ;

multiplicativeBop
    : op=MULTIPLY
    | op=DIVIDE
    ;

additiveBop
    : op=PLUS
    | op=MINUS
    ;

comparisonBop
    : op=EQUAL_TO
    | op=NOT_EQUAL_TO
    | op=LESS_THAN
    | op=LESS_THAN_EQUAL_TO
    | op=GREATER_THAN
    | op=GREATER_THAN_EQUAL_TO
    ;

logicalAndBop
    : op=LOGICAL_AND
    ;

logicalOrBop
    : op=LOGICAL_OR
    ;

variableName
    : THIS                                          # ThisReference
    | IDENTIFIER                                    # VariableReference
    ;

variableIncrementExpr
    : op=(INCREMENT|DECREMENT) IDENTIFIER           # PreIncrementExpr
    | IDENTIFIER op=(INCREMENT|DECREMENT)           # PostIncrementExpr
    ;

functionCall
    : IDENTIFIER DOT IDENTIFIER
      LPAREN functionArgs RPAREN                    # QualifiedFunctionCall
    | IDENTIFIER LPAREN functionArgs RPAREN         # UnqualifiedFunctionCall
    ;

functionArgs
    :                       # NoArgs
    | expr (COMMA expr)*    # SomeArgs
    ;

methodDefinition
    : accessModifier? STATIC? type IDENTIFIER LPAREN methodParams RPAREN codeBlock
    ;

constructorDefinition
    : PUBLIC IDENTIFIER LPAREN methodParams RPAREN codeBlock
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
    | assignment                            # ForLoopAssignOnly
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
    : type LSQBRACKET RSQBRACKET                                       # ArrayType
    | VOID                                                             # VoidType
    | primitiveType=(INT|SHORT|LONG|BYTE|CHAR|BOOLEAN|FLOAT|DOUBLE)    # PrimitiveType
    | nonPrimitiveType=IDENTIFIER                                      # NonPrimitiveType
    ;

literal
    : BOOLEAN_LITERAL
    | FLOAT_LITERAL
    | DOUBLE_LITERAL
    | SHORT_LITERAL
    | LONG_LITERAL
    | INT_LITERAL
    | NULL_LITERAL
    ;
