lexer grammar JavaFileTokens;

fragment Digits: [0-9]+ ;
fragment SignedInteger: ('-')? Digits ;

FLOAT_LITERAL: SignedInteger '.' Digits 'f' ;
DOUBLE_LITERAL: SignedInteger '.' Digits ;
SHORT_LITERAL: SignedInteger 's' ;
LONG_LITERAL: SignedInteger ('l'|'L') ;
INT_LITERAL: SignedInteger ;
BOOLEAN_LITERAL: 'true' | 'false' ;
NULL_LITERAL: 'null' ;

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

IDENTIFIER: [a-zA-Z_][a-zA-Z_0-9]* ;

COMMENT: '//' ~[\r\n]* -> skip ;
WS: [ \t\r\n]+ -> skip ;
