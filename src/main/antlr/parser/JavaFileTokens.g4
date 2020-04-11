lexer grammar JavaFileTokens;

// TODO: Handle comments
// TODO: Handle string literals

EQUALS: '=' ;
PLUS: '+' ;
MINUS: '-' ;
MULTIPLY: '*' ;
DIVIDE: '/' ;
NOT: '!' ;
INCREMENT: '++' ;
DECREMENT: '--' ;
PLUS_EQUALS: '+=' ;
MINUS_EQUALS: '-=' ;
MULTIPLY_EQUALS: '*=' ;
DIVIDE_EQUALS: '/=' ;
EQUAL_TO: '==' ;
NOT_EQUAL_TO: '!=' ;
LESS_THAN: '<' ;
LESS_THAN_EQUAL_TO: '<=' ;
GREATER_THAN: '>' ;
GREATER_THAN_EQUAL_TO: '>=' ;
LOGICAL_AND: '&&' ;
LOGICAL_OR: '||' ;

LPAREN: '(' ;
RPAREN: ')' ;
LBRACE: '{' ;
RBRACE: '}' ;
LSQBRACKET: '[' ;
RSQBRACKET: ']' ;
SINGLE_QUOTE: '\'' ;
DOUBLE_QUOTE: '"' ;
SEMICOLON: ';' ;
COMMA: ',' ;
DOT: '.' ;
QUESTION_MARK: '?' ;
COLON: ':' ;

PUBLIC: 'public' ;
PROTECTED: 'protected' ;
PRIVATE: 'private' ;
STATIC: 'static' ;
CLASS: 'class' ;
RETURN: 'return' ;
EXTENDS: 'extends' ;
THIS: 'this' ;
SUPER: 'super' ;
NEW: 'new' ;

IF: 'if' ;
ELSE: 'else' ;
WHILE: 'while' ;
FOR: 'for' ;
BREAK: 'break' ;

VOID: 'void' ;
INT: 'int' ;
SHORT: 'short' ;
LONG: 'long' ;
BYTE: 'byte' ;
CHAR: 'char' ;
BOOLEAN: 'boolean' ;
FLOAT: 'float' ;
DOUBLE: 'double' ;

fragment TRUE: 'true' ;
fragment FALSE: 'false' ;

fragment DIGITS: [0-9]+ ;
// TODO: Fix the fact that '2-1' parses to ['2', '-1'] (it sees -1 as a SIGNED_INTEGER)
fragment SIGNED_INTEGER: MINUS? DIGITS ;
fragment DECIMAL: DIGITS DOT DIGITS ;

SHORT_LITERAL: SIGNED_INTEGER 's' ;
LONG_LITERAL: SIGNED_INTEGER ('l'|'L') ;
INT_LITERAL: SIGNED_INTEGER ;
FLOAT_LITERAL: DECIMAL 'f' ;
DOUBLE_LITERAL: DECIMAL ;
BOOLEAN_LITERAL: TRUE | FALSE ;
NULL_LITERAL: 'null' ;

IDENTIFIER: [a-zA-Z_][a-zA-Z_0-9]* ;

WS: [ \t\r\n]+ -> skip ;
