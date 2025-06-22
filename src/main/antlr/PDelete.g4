parser grammar PDelete;

options {
    tokenVocab = LCombine;
}

query
    : delete_stmt EOF
    ;

delete_stmt
    : KW_DELETE KW_FROM table_name delete_condition SEMICOLON?
    ;

table_name
    : ID
    ;

delete_condition
    : row_index
    | where_condition
    ;

row_index
    : NUMBER
    ;

where_condition
    : expression (logical_op expression)*
    ;

expression
    : column_index comparison_operator value
    | column_index KW_LIKE string_pattern
    | LBRACE where_condition RBRACE
    ;

comparison_operator
    : OP_Equal
    | OP_NotEqual
    | OP_Less
    | OP_More
    | OP_EqualLess
    | OP_EqualMore
    ;

logical_op
    : OP_AND
    | OP_OR
    ;

column_index
    : NUMBER
    ;

value
    : STRING
    | NUMBER
    | KW_NULL
    ;

string_pattern
    : STRING
    ;