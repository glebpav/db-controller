parser grammar PCreateTable;

options {
tokenVocab = LCombine;
}

query
: create_table_stmt EOF
;

create_table_stmt
: KW_CREATE KW_TABLE table_name LBRACE data_type (KW_COMMA data_type)* RBRACE SEMICOLON?
;

table_name
: ID
;

data_type
: KW_INT
| KW_TEXT
| KW_VARCHAR LBRACE NUMBER RBRACE
;