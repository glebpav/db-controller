parser grammar PCreateTable;

options { tokenVocab=LCombine; }

query: create_table_stmt EOF;

create_table_stmt:
    KW_CREATE KW_TABLE table_name
    LBRACE column_def (KW_COMMA column_def)* RBRACE
    SEMICOLON
;

table_name: ID;

column_def:
    column_name data_type
    (KW_NOT KW_NULL)?
    (KW_DEFAULT default_value)?
    (KW_PRIMARY KW_KEY)?
    (KW_FOREIGN KW_KEY KW_REFERENCES referenced_table)?
;

column_name: ID;

data_type:
    KW_INT | KW_FLOAT | KW_TEXT | KW_VARCHAR
;

default_value:
    NUMBER | STRING | KW_NULL
;

referenced_table:
    table_name LBRACE column_name RBRACE
;