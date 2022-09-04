grammar ToyScript;

program: statement+ EOF;

varDecl: VAR ID (ASSIGN expr)? END;

blockStatement: CURLY_LEFT statement* CURLY_RIGHT;

ifStatement: IF PAREN_LEFT expr PAREN_RIGHT statement (ELSE statement)?;

whileStatement: WHILE PAREN_LEFT expr PAREN_RIGHT statement;

statement:
        varDecl
    |   ifStatement
    |   whileStatement
    |   blockStatement
    |   expr END
    |   END
    ;

expr:
        MINUS expr                                          # UnaryMinusExpr
    |   NOT expr                                            # NegationExpr
    |   ID op=( INCR | DECR )                               # IncrDecrExpr
    |   expr op=( MUL | DIV | MOD ) expr                    # MulDivModExpr
    |   expr op=( PLUS | MINUS ) expr                       # AddSubExpr
    |   expr op=( LT | LTE | GT | GTE ) expr                # CompareExpr
    |   expr op=( EQ | NEQ ) expr                           # EqualCheckExpr
    |   expr op=( AND | OR ) expr                           # AndOrExpr
    |   BOOL                                                # BooleanLiteralExpr
    |   FLOAT                                               # FloatLiteralExpr
    |   INT                                                 # IntLiteralExpr
    |   STRING                                              # StringLiteralExpr
    |   NULL                                                # NullLiteralExpr
    |   ID                                                  # VarExpr
    |   PAREN_LEFT expr PAREN_RIGHT                         # NestedExpr
    |   ID ASSIGN expr                                      # AssignExpr
    ;

VAR: 'var';
IF: 'if';
ELSE: 'else';
WHILE: 'while';

END: ';';
PAREN_LEFT: '(';
PAREN_RIGHT: ')';
CURLY_LEFT: '{';
CURLY_RIGHT: '}';
ASSIGN: '=';

INCR: '++';
DECR: '--';
MUL: '*';
DIV: '/';
PLUS: '+';
MINUS: '-';
MOD: '%';

EQ: '==';
NEQ: '!=';
GTE: '>=';
LTE: '<=';
GT: '>';
LT: '<';

AND: '&&';
OR: '||';
NOT: '!';

NULL: 'null';

ID: [a-zA-Z_][a-zA-Z0-9_]*;

STRING:  '"' ( ESC_CHAR | ~('\\'|'"') )* '"';
CHAR:'\'' ( ESC_CHAR | ~('\''|'\\') ) '\'';
ESC_CHAR: '\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\');

BOOL: 'true' | 'false';
INT: [0-9]+;
FLOAT: [0-9]+ '.' [0-9]*;

COMMENT: ( '//' ~[\r\n]* | '/*' .*? '*/' ) -> skip;
WS: [ \t\r\n]+ -> skip;
