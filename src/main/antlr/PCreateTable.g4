parser grammar PCreateTable;
options { tokenVocab=LCombine; }

query :
    create_table_stmt EOF
;

create_table_stmt:
    KW_CREATE KW_TABLE table_name LBRACE column_defs RBRACE SEMICOLON
;

table_name : ID;

column_defs :
    column_def (KW_COMMA column_def)*
;

column_def :
    column_name data_type
    (KW_NOT KW_NULL)?          // Исправлено: NOT NULL как опциональная группа
    (KW_DEFAULT default_value)?
    (KW_PRIMARY KW_KEY)?
    (foreign_key_def)?
;

column_name : ID;

data_type :
    KW_INT
    | KW_FLOAT
    | KW_TEXT
    | KW_VARCHAR LBRACE NUMBER RBRACE
;

default_value :
    NUMBER
    | STRING
    | KW_NULL
;

foreign_key_def :
    KW_FOREIGN KW_KEY KW_REFERENCES referenced_table LBRACE referenced_column RBRACE
;

referenced_table : ID;
referenced_column : ID;