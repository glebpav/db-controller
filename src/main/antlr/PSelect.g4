parser grammar PSelect;

options {
    tokenVocab = LCombine;
}

query
    : select_stmt EOF
    ;

select_stmt
    : KW_SELECT select_columns KW_FROM table_name (KW_WHERE where_condition)? SEMICOLON?
    ;

select_columns
    : '*'                          # AllColumns
    | column_list                  # ColumnList
    ;

column_list
    : NUMBER (KW_COMMA NUMBER)*
    ;

table_name
    : ID
    ;

where_condition
    : expression (logical_op expression)*
    ;

expression
    : string_pattern comparison_operator value          # ColumnComparison
    | string_pattern KW_LIKE string_pattern             # ColumnLike
    | string_pattern comparison_operator string_pattern             # ColumnLike
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
    : STRING
    ;