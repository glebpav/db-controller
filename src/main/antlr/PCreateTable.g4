parser grammar PCreateTable;
options {
    tokenVocab = LCombine;
}

query
    : create_table_stmt EOF
    ;

create_table_stmt
    : KW_CREATE KW_TABLE table_name
      LPAREN column_type_list RPAREN SEMICOLON?
    ;

table_name
    : ID
    ;

column_type_list
    : column_type (KW_COMMA column_type)*
    ;

column_type returns [String type]
    : KW_INT { $type = "int"; }
    | KW_STR LPAREN num=NUMBER RPAREN { $type = "str_" + $num.text; }
    ;