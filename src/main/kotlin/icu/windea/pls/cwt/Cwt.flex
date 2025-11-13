package icu.windea.pls.cwt.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;

// Lexer for CWT.

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

%s IN_PROPERTY_VALUE
%s IN_PROPERTY_SEPARATOR

%s IN_OPTION
%s IN_OPTION_SEPARATOR
%s IN_OPTION_VALUE
%s IN_OPTION_NESTED
%s IN_OPTION_SEPARATOR_NESTED
%s IN_OPTION_VALUE_NESTED

%unicode

EOL=\s*\R\s*
WHITE_SPACE=[\s&&[^\r\n]]+
BLANK=\s+
COMMENT=#[^\r\n]*
OPTION_COMMENT=##[^\r\n]*
DOC_COMMENT=###[^\r\n]*

// [^#\={}\s\"]+\"?

PROPERTY_KEY_TOKEN=({UNQUOTED_PROPERTY_KEY_TOKEN})|({QUOTED_PROPERTY_KEY_TOKEN})
UNQUOTED_PROPERTY_KEY_TOKEN=[^#={}\s\"]+\"?
QUOTED_PROPERTY_KEY_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?
PROPERTY_KEY_TRAILING=\s*(==|=|\!=|<>)
BOOLEAN_TOKEN=(yes|no)
INT_TOKEN=[+-]?\d+ // leading zero is permitted
FLOAT_TOKEN=[+-]?\d*\.\d+ // leading zero is permitted
STRING_TOKEN=({UNQUOTED_STRING_TOKEN})|({QUOTED_STRING_TOKEN})
UNQUOTED_STRING_TOKEN=[^#={}\s\"]+\"?
QUOTED_STRING_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?

OPTION_KEY_TOKEN=({UNQUOTED_OPTION_KEY_TOKEN})|({QUOTED_OPTION_KEY_TOKEN})
UNQUOTED_OPTION_KEY_TOKEN=[^#={}\s\"]+\"?
QUOTED_OPTION_KEY_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?
OPTION_KEY_TRAILING=\s*(==|=|\!=|<>)

// top level option text (value in option comment, or option value of some option in option comment)
// inner whitespaces are permitted and required
OPTION_TEXT_TOKEN=[^#=!<>{}\s\"]([^#=!<>{}\r\n]*[^#=!<>{}\s])+

%%

<YYINITIAL, IN_PROPERTY_VALUE, IN_PROPERTY_SEPARATOR> {
    "{" { return LEFT_BRACE; }
    "}" { return RIGHT_BRACE; }
    {BLANK} { return WHITE_SPACE; }
    {DOC_COMMENT} { return DOC_COMMENT_TOKEN; }
    {OPTION_COMMENT} { yypushback(yylength() - 2); yybegin(IN_OPTION); return OPTION_COMMENT_START; }
    {COMMENT} { return COMMENT; }
}
<IN_PROPERTY_SEPARATOR> {
    "=="|"=" { yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN; }
    "!="|"<>" { yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN; }
}
<YYINITIAL, IN_PROPERTY_VALUE> {
    {PROPERTY_KEY_TOKEN} / {PROPERTY_KEY_TRAILING} { yybegin(IN_PROPERTY_SEPARATOR); return PROPERTY_KEY_TOKEN; }
    {BOOLEAN_TOKEN} { yybegin(YYINITIAL); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(YYINITIAL); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(YYINITIAL); return FLOAT_TOKEN; }
    {STRING_TOKEN} { yybegin(YYINITIAL); return STRING_TOKEN; }
}

<IN_OPTION, IN_OPTION_VALUE, IN_OPTION_SEPARATOR> {
    "{" {
        int state = yystate();
        if (state == IN_OPTION) yybegin(IN_OPTION_NESTED);
        else if(state == IN_OPTION_VALUE) yybegin(IN_OPTION_VALUE_NESTED);
        return LEFT_BRACE;
    }
    "}" { return RIGHT_BRACE; }
    {EOL} { yybegin(YYINITIAL); return EOL; }
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { yybegin(YYINITIAL);  return COMMENT; }
}
<IN_OPTION_SEPARATOR> {
    "="|"==" { yybegin(IN_OPTION_VALUE); return EQUAL_SIGN; }
    "!="|"<>" { yybegin(IN_OPTION_VALUE); return NOT_EQUAL_SIGN; }
}
<IN_OPTION, IN_OPTION_VALUE> {
    {OPTION_KEY_TOKEN} / {OPTION_KEY_TRAILING} { yybegin(IN_OPTION_SEPARATOR); return OPTION_KEY_TOKEN; }
    {OPTION_TEXT_TOKEN} { yybegin(IN_OPTION); return STRING_TOKEN; }
    {BOOLEAN_TOKEN} { yybegin(IN_OPTION); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(IN_OPTION); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(IN_OPTION); return FLOAT_TOKEN; }
    {STRING_TOKEN} { yybegin(IN_OPTION); return STRING_TOKEN; }
}

<IN_OPTION_NESTED, IN_OPTION_VALUE_NESTED, IN_OPTION_SEPARATOR_NESTED> {
    "{" { return LEFT_BRACE; }
    "}" { return RIGHT_BRACE; }
    {EOL} { yybegin(YYINITIAL); return EOL; }
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { yybegin(YYINITIAL);  return COMMENT; }
}
<IN_OPTION_SEPARATOR_NESTED> {
    "="|"==" { yybegin(IN_OPTION_VALUE_NESTED); return EQUAL_SIGN; }
    "!="|"<>" { yybegin(IN_OPTION_VALUE_NESTED); return NOT_EQUAL_SIGN; }
}
<IN_OPTION_NESTED, IN_OPTION_VALUE_NESTED> {
    {OPTION_KEY_TOKEN} / {OPTION_KEY_TRAILING} { yybegin(IN_OPTION_SEPARATOR_NESTED); return OPTION_KEY_TOKEN; }
    {BOOLEAN_TOKEN} { yybegin(IN_OPTION_NESTED); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(IN_OPTION_NESTED); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(IN_OPTION_NESTED); return FLOAT_TOKEN; }
    {STRING_TOKEN} { yybegin(IN_OPTION_NESTED); return STRING_TOKEN; }
}

[^] { return BAD_CHARACTER; }
