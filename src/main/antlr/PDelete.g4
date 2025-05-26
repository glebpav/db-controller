grammar PDelete;
options { tokenVocab=LCombine; }

query: delete_stmt EOF;

delete_stmt:
    KW_DELETE KW_FROM table_name
    (KW_WHERE condition)?
    SEMICOLON;

table_name: ID;

condition:
    expr
    | LBRACE condition RBRACE
    | OP_NOT condition
    | condition OP_AND condition
    | condition OP_OR condition;

expr:
    column comparator value
    | column comparator column
    | column KW_IS (KW_NOT)? KW_NULL
    | column KW_NOT? FC_LIKE pattern;

comparator:
    OP_Equal | OP_NotEqual |
    OP_Less | OP_More |
    OP_EqualLess | OP_EqualMore;

column: ID;

value: NUMBER | STRING | KW_NULL;

pattern: STRING;