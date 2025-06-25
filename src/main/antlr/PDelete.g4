grammar PDelete;

options {
    tokenVocab = LCombine;
}

query
    : delete_stmt EOF
    ;

delete_stmt
    : KW_DELETE KW_FROM table_name (row_index | KW_WHERE where_clause)? SEMICOLON?
    ;

table_name
    : ID
    ;

row_index
    : NUMBER
    ;

where_clause
    : condition
    ;


condition
    : string_pattern comparison_operator value       #CompareCondition
    | string_pattern KW_LIKE string_pattern         #LikeCondition
    | string_pattern comparison_operator string_pattern #ColumnCompareCondition
    ;

column_reference
    : COLUMN_PREFIX? NUMBER
    ;

comparison_operator
    : OP_Equal      // =
    | OP_DoubleEqual // ==
    | OP_NotEqual   // !=
    | OP_Less       // <
    | OP_More       // >
    | OP_EqualLess  // <=
    | OP_EqualMore  // >=
    ;

value
    : STRING
    | NUMBER
    | KW_NULL
    | column_reference
    ;

string_pattern
    : STRING
    ;