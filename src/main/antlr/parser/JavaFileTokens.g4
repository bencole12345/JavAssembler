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

fragment DIGITS: [0-9]+ ;

SIGNED_INTEGER: MINUS? DIGITS ;
DECIMAL: DIGITS DOT DIGITS ;

IDENTIFIER: [a-zA-Z_][a-zA-Z_0-9]* ;

WS: [ \t\r\n]+ -> skip ;
