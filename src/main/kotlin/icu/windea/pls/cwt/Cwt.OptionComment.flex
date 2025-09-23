package icu.windea.pls.cwt.lexer;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import java.util.*;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;

%%

%{
    private int depth;

    public _CwtOptionCommentLexer() {
        this((java.io.Reader)null);
    }

    private void beginNextOptionValueState() {
        int nextState = depth <= 0 ? OV_TOP : OV_NOT_TOP;
        yybegin(nextState);
    }

    private void beginNextOptionState() {
        int nextState = depth <= 0 ? TOP : NOT_TOP;
        yybegin(nextState);
    }
%}

%public
%class _CwtOptionCommentLexer
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

SEPARATOR_CHECK=(=)|(\!=)|(<>)
OPTION_KEY_CHECK=({OPTION_KEY_TOKEN})?\s*{SEPARATOR_CHECK}

OPTION_KEY_TOKEN=([^#={}\s\"]+\"?)|({QUOTED_KEY_TOKEN})
QUOTED_KEY_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?
BOOLEAN_TOKEN=(yes)|(no)
INT_TOKEN=[+-]?[0-9]+ // leading zero is permitted
FLOAT_TOKEN=[+-]?[0-9]*(\.[0-9]+) // leading zero is permitted
STRING_TOKEN=({UNQUOTED_STRING_TOKEN})|({QUOTED_STRING_TOKEN})
UNQUOTED_STRING_TOKEN=[^#={}\s\"]+\"?
QUOTED_STRING_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?

TOP_STRING_TOKEN=({TOP_UNQUOTED_STRING_TOKEN})|({QUOTED_STRING_TOKEN})
TOP_UNQUOTED_STRING_TOKEN=[^#=<>{}\"\s]([^#=<>{}\"\r\n]*[^#=<>{}\s])? // middle whitespaces are permitted

%%

<YYINITIAL> {
    "##" { yybegin(TOP); return OPTION_COMMENT_START; }
}
<TOP, NOT_TOP, OK, OS, OV_TOP, OV_NOT_TOP> {
    "{" {
        depth++;
        yybegin(NOT_TOP);
        return LEFT_BRACE;
    }
    "}" {
        depth--;
        beginNextOptionState();
        return RIGHT_BRACE;
    }
}
<OS>{
    "="|"==" { beginNextOptionValueState(); return EQUAL_SIGN; }
    "!="|"<>" { beginNextOptionValueState(); return NOT_EQUAL_SIGN; }
}

<TOP, NOT_TOP, OV_TOP, OV_NOT_TOP>{
    {OPTION_KEY_CHECK} { yypushback(yylength()); yybegin(OK); }
    {BOOLEAN_TOKEN} { beginNextOptionState(); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { beginNextOptionState(); return INT_TOKEN; }
    {FLOAT_TOKEN} { beginNextOptionState(); return FLOAT_TOKEN; }
}
<TOP, OV_TOP> {
    {TOP_STRING_TOKEN} { beginNextOptionState(); return STRING_TOKEN; }
}
<NOT_TOP, OV_NOT_TOP> {
    {STRING_TOKEN} { beginNextOptionState(); return STRING_TOKEN; }
}
<OK>{
    {OPTION_KEY_TOKEN} { yybegin(OS); return OPTION_KEY_TOKEN; }
}

<TOP, NOT_TOP, OK, OS, OV_TOP, OV_NOT_TOP> {
    {BLANK} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
}

[^] { return BAD_CHARACTER; }
