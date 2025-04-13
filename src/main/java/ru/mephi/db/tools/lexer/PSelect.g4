parser grammar PSelect;
options {
    tokenVocab=LCombine;
}

query :
    select_stmt EOF
;

select_stmt:
    select_expr
    KW_FROM table_name
;

select_expr :
    KW_SELECT (column_list  |
               KW_STAR
               )
;

column_list :
    column (KW_COMMA column)*
;

column :
    (ID | agregate_funcs) (KW_AS ID)?
;

table_name : ID;

agregate_funcs :
   (FC_SUM  |
    FC_AVG  |
    FC_MIN  |
    FC_MAX  |
    FC_CNT
    ) LBRACE ID RBRACE
;