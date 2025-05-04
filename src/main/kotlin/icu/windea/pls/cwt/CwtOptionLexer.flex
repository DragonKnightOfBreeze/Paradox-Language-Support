package icu.windea.pls.cwt.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import java.util.*;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;

%%

%{
    private int depth;

    public _CwtOptionLexer() {
        this((java.io.Reader)null);
    }

    private int nextState() {
        return depth <= 0 ? TOP : NOT_TOP;
    }

    private int nextOvState() {
        return depth <= 0 ? OV_TOP : OV_NOT_TOP;
    }
%}

%public
%class _CwtOptionLexer
%implements FlexLexer
%function advance
%type IElementType

%s TOP
%s NOT_TOP
%s OK
%s OS
%s OV_TOP
%s OV_NOT_TOP

%unicode

BLANK=\s+
COMMENT=#[^\r\n]*

CHECK_SEPARATOR=(=)|(\!=)|(<>)
CHECK_OPTION_KEY=({OPTION_KEY_TOKEN})?\s*{CHECK_SEPARATOR}

OPTION_KEY_TOKEN=([^#={}\s\"]+\"?)|({QUOTED_KEY_TOKEN})
QUOTED_KEY_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?
BOOLEAN_TOKEN=(yes)|(no)
INT_TOKEN=[+-]?[0-9]+ // leading zero is permitted
FLOAT_TOKEN=[+-]?[0-9]*(\.[0-9]+) // leading zero is permitted
STRING_TOKEN=([^#={}\s\"]+\"?)|({QUOTED_STRING_TOKEN})
QUOTED_STRING_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?

// top option value can contain whitespaces
TOP_STRING_TOKEN=([^#={}\s\"]([^#={}\r\n\"]*[^#={}\s\"])?\"?)|({QUOTED_STRING_TOKEN})

%%

<YYINITIAL> {
    "##" { yybegin(TOP); return OPTION_COMMENT_START; }
}
<TOP, NOT_TOP> {
    {CHECK_OPTION_KEY} { yypushback(yylength()); yybegin(OK); }
    {BOOLEAN_TOKEN} { return BOOLEAN_TOKEN; }
    {INT_TOKEN} { return INT_TOKEN; }
    {FLOAT_TOKEN} { return FLOAT_TOKEN; }
}
<TOP> {
    {TOP_STRING_TOKEN} { return STRING_TOKEN; }
}
<NOT_TOP> {
    {STRING_TOKEN} { return STRING_TOKEN; }
}
<OK>{
    {OPTION_KEY_TOKEN} { yybegin(OS); return OPTION_KEY_TOKEN; }
}
<OS>{
    "="|"==" { yybegin(nextOvState()); return EQUAL_SIGN; }
    "!="|"<>" { yybegin(nextOvState()); return NOT_EQUAL_SIGN; }
}
<OV_TOP, OV_NOT_TOP>{
    {BOOLEAN_TOKEN} { yybegin(nextState()); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(nextState()); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(nextState()); return FLOAT_TOKEN; }
}
<OV_TOP> {
    {TOP_STRING_TOKEN} { yybegin(nextState()); return STRING_TOKEN; }
}
<OV_NOT_TOP> {
    {STRING_TOKEN} { yybegin(nextState()); return STRING_TOKEN; }
}

<TOP, NOT_TOP, OK, OS, OV_TOP, OV_NOT_TOP> {
    "{" {
        depth++;
        yybegin(NOT_TOP);
        return LEFT_BRACE;
    }
    "}" {
        depth--;
        yybegin(nextState());
        return RIGHT_BRACE;
    }
}
<TOP, NOT_TOP, OK, OS, OV_TOP, OV_NOT_TOP> {
    {BLANK} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
}

[^] { return BAD_CHARACTER; }
