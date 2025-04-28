package icu.windea.pls.cwt.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import java.util.*;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;

%%

%{
    public _CwtLexer() {
        this((java.io.Reader)null);
    }
%}

%public
%class _CwtLexer
%implements FlexLexer
%function advance
%type IElementType

%s IN_PROPERTY_KEY
%s IN_PROPERTY_SEPARATOR
%s IN_PROPERTY_VALUE
%s IN_COMMENT

%unicode

BLANK=\s+

COMMENT=#[^\R]*

CHECK_SEPARATOR=(=)|(\!=)|(<>)
CHECK_PROPERTY_KEY=({PROPERTY_KEY_TOKEN})?\s*{CHECK_SEPARATOR}

PROPERTY_KEY_TOKEN=([^#={}\s\"]+\"?)|({QUOTED_KEY_TOKEN})
QUOTED_KEY_TOKEN=\"([^\"\\\R]|\\[\s\S])*\"?
BOOLEAN_TOKEN=(yes)|(no)
INT_TOKEN=[+-]?[0-9]+ // leading zero is permitted
FLOAT_TOKEN=[+-]?[0-9]*(\.[0-9]+) // leading zero is permitted
STRING_TOKEN=([^#={}\s\"]+\"?)|({QUOTED_STRING_TOKEN})
QUOTED_STRING_TOKEN=\"([^\"\\\R]|\\[\s\S])*\"?

%%

<YYINITIAL> {
    "{" { yybegin(YYINITIAL); return LEFT_BRACE; }
    "}" { yybegin(YYINITIAL); return RIGHT_BRACE; }
    "#" { yypushback(1); yybegin(IN_COMMENT); }
    {BLANK} { return WHITE_SPACE; }

    {CHECK_PROPERTY_KEY} { yypushback(yylength()); yybegin(IN_PROPERTY_KEY); }
    {BOOLEAN_TOKEN} { return BOOLEAN_TOKEN; }
    {INT_TOKEN} { return INT_TOKEN; }
    {FLOAT_TOKEN} { return FLOAT_TOKEN; }
    {STRING_TOKEN} { return STRING_TOKEN; }
}
<IN_PROPERTY_KEY>{
    "{" { yybegin(YYINITIAL); return LEFT_BRACE; }
    "}" { yybegin(YYINITIAL); return RIGHT_BRACE; }
    "#" { yypushback(1); yybegin(IN_COMMENT); }
    {BLANK} { return WHITE_SPACE; }

    {PROPERTY_KEY_TOKEN} { yybegin(IN_PROPERTY_SEPARATOR); return PROPERTY_KEY_TOKEN; }
}
<IN_PROPERTY_SEPARATOR>{
    "{" { yybegin(YYINITIAL); return LEFT_BRACE; }
    "}" { yybegin(YYINITIAL); return RIGHT_BRACE; }
    "="|"==" { yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN; }
    "!="|"<>" { yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN; }

    {COMMENT} { return COMMENT; }
    {BLANK} { return WHITE_SPACE; }
}
<IN_PROPERTY_VALUE>{
    "{" { yybegin(YYINITIAL); return LEFT_BRACE; }
    "}" { yybegin(YYINITIAL); return RIGHT_BRACE; }

    {COMMENT} { return COMMENT; }
    {BLANK} { return WHITE_SPACE; }

    {CHECK_PROPERTY_KEY} { yypushback(yylength()); yybegin(IN_PROPERTY_KEY); }
    {BOOLEAN_TOKEN} { yybegin(YYINITIAL); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(YYINITIAL); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(YYINITIAL); return FLOAT_TOKEN; }
    {STRING_TOKEN} { yybegin(YYINITIAL); return STRING_TOKEN; }
}
<IN_COMMENT>{
    {COMMENT} {
        int length = yylength();
        if (length >= 2 && yycharat(1) == '#') {
            if (length >= 3 && yycharat(2) == '#') {
                return DOC_COMMENT;
            }
            return OPTION_COMMENT;
        }
        return COMMENT;
    }
}

[^] { return BAD_CHARACTER; }
