package icu.windea.pls.csv.lexer;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import java.util.*;
import java.util.concurrent.atomic.*;
import icu.windea.pls.model.ParadoxGameType;
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*;

// Lexer for Paradox CSV.
// Notes:
// - Tokens are simple: separator ';', comments '#', EOL, and column tokens.
// - Do NOT rename %class, token names, or ElementTypes; they are part of the public interface.
// - QUOTED_COLUMN_TOKEN tolerates an optional closing quote for better error recovery.

%%

%{
    private ParadoxGameType gameType;

    public _ParadoxCsvLexer() {
        this((java.io.Reader)null);
        this.gameType = null;
    }

    public _ParadoxCsvLexer(ParadoxGameType gameType) {
        this((java.io.Reader)null);
        this.gameType = gameType;
    }

    public ParadoxGameType getGameType() {
        return this.gameType;
    }
%}

%public
%class _ParadoxCsvLexer
%implements FlexLexer
%function advance
%type IElementType

%unicode

EOL=\s*\R\s*
BLANK=[\s&&[^\r\n]]+
COMMENT=#[^\r\n]*
SEPARATOR=;

// No extra token kinds beyond columns (booleans/numbers are treated as plain text)

COLUMN_TOKEN=({UNQUOTED_COLUMN_TOKEN})|({QUOTED_COLUMN_TOKEN})
UNQUOTED_COLUMN_TOKEN=[^#;\"\s]([^#;\"\r\n]*[^#;\s])? // inner whitespaces are permitted
QUOTED_COLUMN_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"? // closing quote optional for recovery

%%

<YYINITIAL> {
    {EOL} { return EOL; }
    {BLANK} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
    {SEPARATOR} { return SEPARATOR; }
    {COLUMN_TOKEN} { return COLUMN_TOKEN; }
}

[^] { return BAD_CHARACTER; }
