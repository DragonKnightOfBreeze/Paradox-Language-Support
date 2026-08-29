package icu.windea.pls.csv.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import java.util.*;
import java.util.concurrent.atomic.*;
import icu.windea.pls.model.ParadoxGameType;
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*;

// Lexer for Paradox CSV.

%%

%{
    public _ParadoxCsvLexer() {
        this((java.io.Reader)null);
    }
%}

%public
%class _ParadoxCsvLexer
%implements FlexLexer
%function advance
%type IElementType

%unicode

EOL=\s*\R\s*
WHITE_SPACE=[\s&&[^\r\n]]+
//BLANK=\s+

COMMENT=#[^\r\n]*

QUOTE=\"
SEPARATOR=";"

LITERAL_CHAR=[^#;\"\r\n]
LITERAL_BOUND_CHAR=[^#;\"\s]
LITERAL_TOKEN={LITERAL_BOUND_CHAR}({LITERAL_CHAR}*{LITERAL_BOUND_CHAR})? // inner whitespaces are allowed
LITERAL_TOKEN_QUOTED=([^\"\\\r\n]|\\.)+

// no extra token kinds beyond columns (booleans/numbers are treated as plain text)

// 3.0.2 NOTE not split quotes into individual tokens in columns atm
COLUMN_TOKEN_QUOTED={LITERAL_TOKEN_QUOTED} // compatible with missing closing quote
COLUMN_TOKEN_UNQUOTED={LITERAL_TOKEN} // literal
COLUMN_TOKEN=({QUOTE}{COLUMN_TOKEN_QUOTED}|{COLUMN_TOKEN_UNQUOTED}){QUOTE}?

%%

<YYINITIAL> {
    {EOL} { return EOL; }
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
    {SEPARATOR} { return SEPARATOR; }
    {COLUMN_TOKEN} { return COLUMN_TOKEN; }
}

[^] { return BAD_CHARACTER; }
