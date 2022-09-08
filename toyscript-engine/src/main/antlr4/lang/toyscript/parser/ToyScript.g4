grammar ToyScript;

program: statement* EOF;

varDecl: VAR ID (ASSIGN expr)? END;

functionDecl: FUNCTION ID PAREN_L ( ID (COMMA ID)* )? PAREN_R CURLY_L statement* CURLY_R;

ifStatement: IF PAREN_L expr PAREN_R statement (ELSE statement)?;

whileStatement: WHILE PAREN_L expr PAREN_R statement;

tryStatement: TRY blockStatement CATCH (PAREN_L ID PAREN_R)? CURLY_L statement* CURLY_R;

throwStatement: THROW expr? END;

loopExitClause: op=(BREAK | CONTINUE) END;

returnClause: RETURN expr? END;

exprStatement: expr END;

blockStatement: CURLY_L statement* CURLY_R;

statement:
        varDecl
    |   functionDecl
    |   ifStatement
    |   whileStatement
    |   tryStatement
    |   throwStatement
    |   returnClause
    |   exprStatement
    |   blockStatement
    |   loopExitClause
    |   END
    ;

expr:
        PAREN_L expr PAREN_R                                # NestedExpr
    |   MINUS expr                                          # UnaryMinusExpr
    |   NOT expr                                            # NegationExpr
    |   ID op=( INCR | DECR )                               # IncrDecrExpr
    |   expr DOT ID                                         # MemberAccessExpr
    |   expr INDEX_L expr INDEX_R                           # IndexAccessExpr
    |   ID PAREN_L ( expr (COMMA expr)* )? PAREN_R          # FunctionCallExpr
    |   expr op=( MUL | DIV | MOD ) expr                    # MulDivModExpr
    |   expr op=( PLUS | MINUS ) expr                       # AddSubExpr
    |   expr op=( LT | LTE | GT | GTE ) expr                # CompareExpr
    |   expr op=( EQ | NEQ ) expr                           # EqualCheckExpr
    |   expr op=( AND | OR ) expr                           # AndOrExpr
    |   STRUCT CURLY_L ( ID ASSIGN expr END )* CURLY_R      # StructInitExpr
    |   ARRAY CURLY_L ( expr (COMMA expr)* )? CURLY_R       # ArrayInitExpr
    |   ARRAY INDEX_L expr INDEX_R                          # ArrayDefExpr
    |   BOOL                                                # BooleanLiteralExpr
    |   FLOAT                                               # FloatLiteralExpr
    |   INT                                                 # IntLiteralExpr
    |   STRING                                              # StringLiteralExpr
    |   NULL                                                # NullLiteralExpr
    |   ID                                                  # VarExpr
    |   expr DOT ID ASSIGN expr                             # MemberAssignExpr
    |   expr INDEX_L expr INDEX_R ASSIGN expr               # IndexAssignExpr
    |   ID ASSIGN expr                                      # AssignExpr
    ;

VAR: 'var';
ARRAY: 'array';
STRUCT: 'struct';
FUNCTION: 'function';
IF: 'if';
ELSE: 'else';
WHILE: 'while';
BREAK: 'break';
CONTINUE: 'continue';
TRY: 'try';
CATCH: 'catch';
THROW: 'throw';
RETURN: 'return';

END: ';';
COMMA: ',';
PAREN_L: '(';
PAREN_R: ')';
CURLY_L: '{';
CURLY_R: '}';
INDEX_L: '[';
INDEX_R: ']';
ASSIGN: '=';
DOT: '.';

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

BOOL: 'true' | 'false';
INT: [0-9]+;
FLOAT: [0-9]+ '.' [0-9]*;

ID: [a-zA-Z_][a-zA-Z0-9_]*;

STRING:  '"' ( ESC_CHAR | ~('\\'|'"') )* '"';
CHAR:'\'' ( ESC_CHAR | ~('\''|'\\') ) '\'';
ESC_CHAR: '\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\');

COMMENT: ( '//' ~[\r\n]* | '/*' .*? '*/' ) -> skip;
WS: [ \t\r\n]+ -> skip;
