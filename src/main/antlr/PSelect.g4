parser grammar PSelect;

options {
    tokenVocab = LCombine;
}

query
    : select_stmt EOF
    ;

select_stmt
    : KW_SELECT select_list KW_FROM table_name (KW_WHERE where_condition)? SEMICOLON?
    ;

select_list
    : KW_STAR
    | column_index (KW_COMMA column_index)*
    ;

table_name
    : ID
    ;

column_index
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

value
    : STRING
    | NUMBER
    | KW_NULL
    ;

string_pattern
    : STRING // Паттерн для LIKE ('%test%', 'apple%' и т.д.)
    ;