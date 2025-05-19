parser grammar PBeginTransaction;
options { tokenVocab=LCombine; }

query :
    begin_transaction_stmt EOF
;

begin_transaction_stmt:
    KW_BEGIN (KW_TRANSACTION)? (transaction_name)? SEMICOLON
;

transaction_name : ID;