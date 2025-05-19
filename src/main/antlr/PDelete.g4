parser grammar PDelete;
import PWhere;
options { tokenVocab=LCombine; }

query :
    delete_stmt EOF
;

delete_stmt:
    KW_DELETE KW_FROM table_name (KW_WHERE condition)? SEMICOLON
;

table_name : ID;