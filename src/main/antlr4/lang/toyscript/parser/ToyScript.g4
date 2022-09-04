grammar ToyScript;

program: statement+ EOF;

varDecl: VAR ID (ASSIGN expr)? END;

blockStatement: CURLY_LEFT statement* CURLY_RIGHT;

ifStatement: IF PAREN_LEFT expr PAREN_RIGHT statement (ELSE statement)?;

whileStatement: WHILE PAREN_LEFT expr PAREN_RIGHT statement;

statement:
        blockStatement
    |   ifStatement
    |   whileStatement
    |   varDecl
    |   expr END
    |   END
    ;

expr:
        expr op=( ADD | SUB | MUL | DIV | MOD ) expr        # ArithmeticExpr
    |   expr op=( EQ | NEQ | LT | LTE | GT | GTE ) expr     # CompareExpr
    |   expr op=( AND | OR ) expr                           # AndOrExpr
    |   NOT expr                                            # NegationExpr
    |   ID ASSIGN expr                                      # AssignExpr
    |   BOOL                                                # BooleanLiteralExpr
    |   INT                                                 # IntLiteralExpr
    |   FLOAT                                               # FloatLiteralExpr
    |   STRING                                              # StringLiteralExpr
    |   NULL                                                # NullLiteralExpr
    |   ID                                                  # VarExpr
    |   PAREN_LEFT expr PAREN_RIGHT                         # NestedExpr
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

MUL: '*';
DIV: '/';
ADD: '+';
SUB: '-';
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
