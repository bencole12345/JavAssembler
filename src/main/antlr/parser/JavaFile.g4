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
    : IDENTIFIER EQUALS expr
    ;

variableDeclarationAndAssignment
    : type IDENTIFIER EQUALS expr
    ;

expr
    : value                                         # ValueExpr
    | functionCall                                  # FunctionCallExpr
    | LPAREN expr RPAREN                            # ParenthesesExpr
    | MINUS expr                                    # NegateExpr
    | expr op=(MULTIPLY|DIVIDE) expr                # InfixExpr
    | expr op=(PLUS|MINUS) expr                     # InfixExpr
    | expr op=(INCREMENT|DECREMENT)                 # PostfixExpr
    | expr QUESTION_MARK expr COLON expr SEMICOLON  # BinarySelectorExpr
    | IDENTIFIER                                    # VariableNameExpr
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
    : IF LPAREN expr LPAREN codeBlock ELSE ifStatement   # IfElseIf
    | IF LPAREN expr RPAREN codeBlock ELSE codeBlock     # IfElse
    | IF LPAREN expr RPAREN codeBlock                    # If
    ;

whileLoop
    : WHILE LPAREN expr RPAREN codeBlock
    ;

forLoop
    : FOR LPAREN
            statement SEMICOLON
            expr SEMICOLON
            statement RPAREN codeBlock
    ;

accessModifier
    : modifier=(PUBLIC|PRIVATE|PROTECTED)
    ;

type
    : VOID                                                             # VoidType
    | primitiveType=(INT|SHORT|LONG|BYTE|CHAR|BOOLEAN|FLOAT|DOUBLE)    # PrimitiveType
    | nonPrimitiveType=IDENTIFIER                                      # NonPrimitiveType
    ;

value
    : SIGNED_INTEGER        # SignedIntegerValue
    | DECIMAL               # DecimalValue
    ;
