package icu.windea.pls.csv.lexer;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import java.util.*;
import java.util.concurrent.atomic.*;
import icu.windea.pls.model.ParadoxGameType;
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*;

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

EOL=\R
BLANK=\s[^\S\r\n]*
COMMENT=#[^\r\n]*
SEPARATOR=;

//no non-column tokens (boolean tokens, number tokens, etc)

COLUMN_TOKEN=({UNQUOTED_COLUMN_TOKEN})|({QUOTED_COLUMN_TOKEN})
UNQUOTED_COLUMN_TOKEN=[^#;\"\s]([^#;\"\r\n]*[^#;\s])? // middle whitespaces are permitted
QUOTED_COLUMN_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?

%%

<YYINITIAL> {
    {EOL} { return EOL; }
    {BLANK} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
    {SEPARATOR} { return SEPARATOR; }
    {COLUMN_TOKEN} { return COLUMN_TOKEN; }
}

[^] { return BAD_CHARACTER; }
