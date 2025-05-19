parser grammar PShowFiles;
options { tokenVocab=LCombine; }

query :
    show_files_stmt EOF
;

show_files_stmt:
    KW_SHOW KW_FILES (KW_FOR KW_DATABASE db_name)? SEMICOLON
;

db_name : ID;