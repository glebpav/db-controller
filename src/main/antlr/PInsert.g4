parser grammar PInsert;

options {
tokenVocab = LCombine;
}

query
: insert_stmt EOF
;

insert_stmt
: KW_INSERT KW_INTO table_name KW_VALUES LBRACE value (KW_COMMA value)* RBRACE SEMICOLON?
;

table_name
: ID
;

value
: STRING
| NUMBER
;