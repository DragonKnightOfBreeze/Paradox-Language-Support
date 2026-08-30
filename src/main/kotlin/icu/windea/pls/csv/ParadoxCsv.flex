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

Eol = \s*\R\s*
WHITE_SPACE = [\s&&[^\r\n]]+
// Blank = \s+

Comment = #[^\r\n]*

Quote = \"
Separator = ";"

LiteralChar = [^#;\"\r\n]
LiteralBoundChar = [^#;\"\s]
LiteralToken = {LiteralBoundChar}({LiteralChar}*{LiteralBoundChar})? // inner whitespaces are allowed
LiteralTokenQuoted = ([^\"\\\r\n]|\\.)+

// no extra token kinds beyond columns (booleans/numbers are treated as plain text)

ColumnTokenQuoted = {LiteralTokenQuoted} // compatible with missing closing quote
ColumnTokenUnquoted = {LiteralToken} // literal
ColumnContent = ({Quote}{ColumnTokenQuoted}|{ColumnTokenUnquoted}){Quote}?

%%

<YYINITIAL> {
    {Eol} { return EOL; }
    {WHITE_SPACE} { return WHITE_SPACE; }
    {Comment} { return COMMENT; }
    {Separator} { return SEPARATOR; }
    {ColumnContent} { return COLUMN_TOKEN; }
}

[^] { return BAD_CHARACTER; }
