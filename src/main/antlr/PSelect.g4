parser grammar PSelect;
options { tokenVocab=LCombine; }

query :
    select_stmt EOF
;

select_stmt:
    select_expr KW_FROM table_name (KW_WHERE condition)? SEMICOLON?
;

select_expr :
    KW_SELECT (column_list | KW_STAR)
;

column_list :
    column (KW_COMMA column)*
;

column :
    (ID | aggregate_func) (KW_AS ID)?
;

aggregate_func :
    (FC_SUM | FC_AVG | FC_MIN | FC_MAX | FC_CNT) LBRACE ID RBRACE
;

condition :
    expr
    | LBRACE condition RBRACE
    | condition OP_AND condition
    | condition OP_OR condition
;

expr :
    column OP_Equal value
    | column OP_NotEqual value
    | column OP_Less value
    | column OP_More value
    | column OP_EqualLess value
    | column OP_EqualMore value
    | column KW_IS (KW_NOT)? KW_NULL
    | column FC_LIKE string_value
;

value :
    column
    | literal_value
;

literal_value :
    NUMBER
    | string_value
    | KW_NULL
;

string_value : STRING;

table_name :
    ID
    | KW_TABLE
;