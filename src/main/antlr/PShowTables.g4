parser grammar PShowTables;

options {
tokenVocab = LCombine;
}

query
: show_tables_stmt EOF
;

show_tables_stmt
: KW_SHOW KW_TABLES SEMICOLON?
;