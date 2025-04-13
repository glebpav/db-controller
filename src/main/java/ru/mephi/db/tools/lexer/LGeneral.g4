lexer grammar LGeneral;
import LLetters;

ID      :   [a-zA-Z_][a-zA-Z0-9_]*  ;
LBRACE  :   '('                     ;
RBRACE  :   ')'                     ;
WS      :   [ \t\r\n]+ -> skip;