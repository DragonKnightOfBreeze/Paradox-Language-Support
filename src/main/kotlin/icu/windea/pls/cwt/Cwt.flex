package icu.windea.pls.cwt.lexer;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import java.util.*;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;

%%

%{
    // private int depth;

    public _CwtLexer() {
        this((java.io.Reader)null);
    }

    private void beginNextMemberState() {
        int nextState = YYINITIAL;
        yybegin(nextState);
    }
%}

%public
%class _CwtLexer
%implements FlexLexer
%function advance
%type IElementType

%s PK
%s PS
%s PV

%unicode

BLANK=\s+
COMMENT=#[^\r\n]*

SEPARATOR_CHECK=(=)|(\!=)|(<>)
PROPERTY_KEY_CHECK=({PROPERTY_KEY_TOKEN})?\s*{SEPARATOR_CHECK}

PROPERTY_KEY_TOKEN=({UNQUOTED_PROPERTY_KEY_TOKEN})|({QUOTED_KEY_TOKEN})
UNQUOTED_PROPERTY_KEY_TOKEN=[^#={}\s\"]+\"?
QUOTED_KEY_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?
BOOLEAN_TOKEN=(yes)|(no)
INT_TOKEN=[+-]?[0-9]+ // leading zero is permitted
FLOAT_TOKEN=[+-]?[0-9]*(\.[0-9]+) // leading zero is permitted
STRING_TOKEN=({UNQUOTED_STRING_TOKEN})|({QUOTED_STRING_TOKEN})
UNQUOTED_STRING_TOKEN=[^#={}\s\"]+\"?
QUOTED_STRING_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?

%%

<YYINITIAL, PK, PS, PV> {
    "{" {
        // depth++;
        beginNextMemberState();
        return LEFT_BRACE;
    }
    "}" {
        // depth--;
        beginNextMemberState();
        return RIGHT_BRACE;
    }
}
<PS>{
    "="|"==" { yybegin(PV); return EQUAL_SIGN; }
    "!="|"<>" { yybegin(PV); return NOT_EQUAL_SIGN; }
}

<YYINITIAL, PV> {
    {PROPERTY_KEY_CHECK} { yypushback(yylength()); yybegin(PK); }
    {BOOLEAN_TOKEN} { beginNextMemberState(); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { beginNextMemberState(); return INT_TOKEN; }
    {FLOAT_TOKEN} { beginNextMemberState(); return FLOAT_TOKEN; }
    {STRING_TOKEN} { beginNextMemberState(); return STRING_TOKEN; }
}
<PK>{
    {PROPERTY_KEY_TOKEN} { yybegin(PS); return PROPERTY_KEY_TOKEN; }
}

<YYINITIAL, PK, PS, PV> {
    {BLANK} { return WHITE_SPACE; }
    {COMMENT} {
        int state = yystate();
        if (state == YYINITIAL || state == PK) {
            int length = yylength();
            if (length >= 2 && yycharat(1) == '#') {
                if (length >= 3 && yycharat(2) == '#') {
                    return DOC_COMMENT_TOKEN;
                }
                return OPTION_COMMENT_TOKEN;
            }
        }
        return COMMENT;
    }
}

[^] { return BAD_CHARACTER; }
