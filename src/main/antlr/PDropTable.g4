parser grammar PDropTable;

options {
    tokenVocab = LCombine;
}

query
: drop_table_stmt EOF
;

drop_table_stmt
: KW_DROP KW_TABLE table_name SEMICOLON?
;

table_name
: ID
 | STRING  // Add support for quoted identifiers
;
