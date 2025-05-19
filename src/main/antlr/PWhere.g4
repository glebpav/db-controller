parser grammar PWhere;
options { tokenVocab=LCombine; }

condition :
    expr
    | condition OP_AND condition
    | condition OP_OR condition
    | LBRACE condition RBRACE
    | OP_NOT condition
;

expr :
    column OP_Equal value
    | column OP_Equal column
    | column OP_Less value
    | column OP_More value
    | column OP_EqualLess value
    | column OP_EqualMore value
    | column KW_IS (KW_NOT)? KW_NULL
    | column FC_LIKE pattern  // Используем FC_LIKE из LFuncs
;

column : ID;
value : NUMBER | STRING | KW_NULL;
pattern : STRING;  // Шаблоны с % и _