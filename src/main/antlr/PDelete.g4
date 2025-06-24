grammar PDelete;

options {
    tokenVocab = LCombine;
}

query
    : delete_stmt EOF
    ;

delete_stmt
    : KW_DELETE KW_FROM table_name
      (where_clause | row_index)?
      SEMICOLON?
    ;

table_name
    : ID
    ;

where_clause
    : KW_WHERE condition
    ;

row_index
    : NUMBER
    ;

condition
    : column_reference comparison_operator value       #CompareCondition
    | column_reference KW_LIKE string_pattern         #LikeCondition
    | column_reference comparison_operator column_reference #ColumnCompareCondition
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