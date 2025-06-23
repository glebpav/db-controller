parser grammar PDelete;

options {
    tokenVocab = LCombine;
}

query
    : delete_stmt EOF
    ;

delete_stmt
    : KW_DELETE KW_FROM table_name (delete_condition)? SEMICOLON?
    ;

table_name
    : ID
    ;

delete_condition
    : KW_WHERE condition
    ;

condition
    : column_index comparison_operator value      #SimpleCondition
    | column_index KW_LIKE string_pattern         #LikeCondition
    ;

comparison_operator
    : OP_Equal
    | OP_NotEqual
    | OP_Less
    | OP_More
    | OP_EqualLess
    | OP_EqualMore
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