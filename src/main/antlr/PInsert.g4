parser grammar PInsert;

options { tokenVocab=LCombine; }

query : insert_stmt EOF;

insert_stmt:
    KW_INSERT KW_INTO table_name
    (
      (column_list KW_VALUES LBRACE value_list RBRACE)
      | KW_VALUES LBRACE value_list RBRACE
    )
    SEMICOLON
;

table_name : ID;

column_list : LBRACE ID (KW_COMMA ID)* RBRACE;

value_list : value (KW_COMMA value)*;

value : NUMBER | STRING | KW_NULL;