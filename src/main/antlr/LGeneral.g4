lexer grammar LGeneral;

import LLetters;

ID      : [a-zA-Z_][a-zA-Z0-9_]*;
NUMBER  : [0-9]+ ('.' [0-9]+)?;

UNDERSCORE: '_';
QUOTE: '"';
STRING  : '\'' ('\\'. | ~('\'' | '\\'))* '\'';
PLUS    : '+';
MINUS   : '-';
DIV     : '/';
WS      : [ \t\r\n]+ -> skip;
SEMICOLON : ';';

