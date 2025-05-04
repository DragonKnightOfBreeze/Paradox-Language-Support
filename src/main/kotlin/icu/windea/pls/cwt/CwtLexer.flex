package icu.windea.pls.cwt.psi;

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

    private int nextState() {
        return YYINITIAL;
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

CHECK_SEPARATOR=(=)|(\!=)|(<>)
CHECK_PROPERTY_KEY=({PROPERTY_KEY_TOKEN})?\s*{CHECK_SEPARATOR}

PROPERTY_KEY_TOKEN=([^#={}\s\"]+\"?)|({QUOTED_KEY_TOKEN})
QUOTED_KEY_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?
BOOLEAN_TOKEN=(yes)|(no)
INT_TOKEN=[+-]?[0-9]+ // leading zero is permitted
FLOAT_TOKEN=[+-]?[0-9]*(\.[0-9]+) // leading zero is permitted
STRING_TOKEN=([^#={}\s\"]+\"?)|({QUOTED_STRING_TOKEN})
QUOTED_STRING_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?

%%

<YYINITIAL> {
    {CHECK_PROPERTY_KEY} { yypushback(yylength()); yybegin(PK); }
    {BOOLEAN_TOKEN} { return BOOLEAN_TOKEN; }
    {INT_TOKEN} { return INT_TOKEN; }
    {FLOAT_TOKEN} { return FLOAT_TOKEN; }
    {STRING_TOKEN} { return STRING_TOKEN; }
}
<PK>{
    {PROPERTY_KEY_TOKEN} { yybegin(PS); return PROPERTY_KEY_TOKEN; }
}
<PS>{
    "="|"==" { yybegin(PV); return EQUAL_SIGN; }
    "!="|"<>" { yybegin(PV); return NOT_EQUAL_SIGN; }
}
<PV>{
    {CHECK_PROPERTY_KEY} { yypushback(yylength()); yybegin(PK); }
    {BOOLEAN_TOKEN} { yybegin(nextState()); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(nextState()); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(nextState()); return FLOAT_TOKEN; }
    {STRING_TOKEN} { yybegin(nextState()); return STRING_TOKEN; }
}

<YYINITIAL, PK, PS, PV> {
    "{" {
        // depth++;
        yybegin(nextState());
        return LEFT_BRACE;
    }
    "}" {
        // depth--;
        yybegin(nextState());
        return RIGHT_BRACE;
    }
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
