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

    private void beginStateInOption() {
        int state = yystate();
        if (state == IN_OPTION) {
          yybegin(IN_OPTION_NESTED);
        } else if (state == IN_OPTION_VALUE) {
          yybegin(IN_OPTION_VALUE_NESTED);
        }
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

KEYWORD_YES = yes
KEYWORD_NO = no
KEYWORD_BOOLEAN = {KEYWORD_YES}|{KEYWORD_NO}

OP_UNARY_PLUS = "+"
OP_UNARY_MINUS = "-"
OP_EQUAL = "="
OP_NOT_EQUAL = "!="|"<>"
OP_DOUBLE_EQUAL = "=="

QUOTE=\"
NUMBER_UNARY = {OP_UNARY_PLUS}|{OP_UNARY_MINUS}
SEPARATOR = {OP_DOUBLE_EQUAL}|{OP_EQUAL}|{OP_NOT_EQUAL} // order-sensitive

INT_NUMBER_TOKEN=[0-9]+ // leading zero is allowed
FLOAT_NUMBER_TOKEN=[0-9]*\.[0-9]+ // leading zero is allowed

BOOLEAN_TOKEN={KEYWORD_BOOLEAN} // `yes` or `no` (case-sensitive)
INT_TOKEN={NUMBER_UNARY}?{INT_NUMBER_TOKEN} // with optional unary operator
FLOAT_TOKEN={NUMBER_UNARY}?{FLOAT_NUMBER_TOKEN} // with optional unary operator

LITERAL_CHAR=[^#={}\"\s] // `!?` are allowed (`<>` are always allowed)
LITERAL_BOUND_CHAR=[^#={}\"\s!?] // `!?` are not allowed (`<>` are always allowed)
LITERAL_TOKEN={LITERAL_BOUND_CHAR}({LITERAL_CHAR}*{LITERAL_BOUND_CHAR})? // boundary `@!?` are not allowed (`<>` are always allowed)

// IDENTIFIER_CHAR=[A-Za-z0-9_]
// IDENTIFIER_LEAD_CHAR=[A-Za-z_] // leading number is not allowed
// IDENTIFIER_TOKEN={IDENTIFIER_LEAD_CHAR}{IDENTIFIER_CHAR}* // leading number is not allowed

PROPERTY_KEY_TOKEN_QUOTED=([^\"\\\r\n]|\\.)+ // without surrounding quotes
PROPERTY_KEY_TOKEN_UNQUOTED={LITERAL_TOKEN} // literal
PROPERTY_KEY_CONTENT=({QUOTE}{PROPERTY_KEY_TOKEN_QUOTED}|{PROPERTY_KEY_TOKEN_UNQUOTED}){QUOTE}?

STRING_TOKEN_QUOTED=([^\"\\\r\n]|\\.)+ // without surrounding quotes
STRING_TOKEN_UNQUOTED={LITERAL_TOKEN} // literal
STRING_CONTENT=({QUOTE}{STRING_TOKEN_QUOTED}|{STRING_TOKEN_UNQUOTED}){QUOTE}?

OPTION_KEY_TOKEN_QUOTED=([^\"\\\r\n]|\\.)+ // without surrounding quotes
OPTION_KEY_TOKEN_UNQUOTED={LITERAL_TOKEN} // literal
OPTION_KEY_CONTENT=({QUOTE}{OPTION_KEY_TOKEN_QUOTED}|{OPTION_KEY_TOKEN_UNQUOTED}){QUOTE}?

// top level option text (value in option comment, or option value of some option in option comment)
// inner whitespaces are allowed and required
OPTION_TEXT_CHAR=[^#=!<>{}\r\n] // heuristic
OPTION_TEXT_BOUND_CHAR=[^#=!<>{}\"\s] // heuristic
OPTION_TEXT_TOKEN={OPTION_TEXT_BOUND_CHAR}({OPTION_TEXT_CHAR}*{OPTION_TEXT_BOUND_CHAR})? // heuristic

%%

<YYINITIAL, IN_PROPERTY_SEPARATOR, IN_PROPERTY_VALUE> {
    "{" { return LEFT_BRACE; }
    "}" { return RIGHT_BRACE; }
    {BLANK} { return WHITE_SPACE; }
    {DOC_COMMENT} { return DOC_COMMENT_TOKEN; }
    {OPTION_COMMENT} { yypushback(yylength() - 2); yybegin(IN_OPTION); return OPTION_COMMENT_START; }
    {COMMENT} { return COMMENT; }
}
<IN_PROPERTY_SEPARATOR> {
    {OP_DOUBLE_EQUAL} { yybegin(IN_PROPERTY_VALUE); return DOUBLE_EQUAL_SIGN; }
    {OP_EQUAL} { yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN; }
    {OP_NOT_EQUAL} { yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN; }
}
<YYINITIAL, IN_PROPERTY_VALUE> {
    {BOOLEAN_TOKEN} { yybegin(YYINITIAL); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(YYINITIAL); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(YYINITIAL); return FLOAT_TOKEN; }
    // use trailing context (high priority than normal form)
    {PROPERTY_KEY_CONTENT} / {BLANK}?{SEPARATOR} { yybegin(IN_PROPERTY_SEPARATOR); return PROPERTY_KEY_TOKEN; }
    {STRING_CONTENT} { yybegin(YYINITIAL); return STRING_TOKEN; }
}

<IN_OPTION, IN_OPTION_SEPARATOR, IN_OPTION_VALUE> {
    "{" { beginStateInOption(); return LEFT_BRACE; }
    "}" { return RIGHT_BRACE; }
    {EOL} { yybegin(YYINITIAL); return EOL; }
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { yybegin(YYINITIAL);  return COMMENT; }
}
<IN_OPTION_SEPARATOR> {
    {OP_DOUBLE_EQUAL} { yybegin(IN_OPTION_VALUE); return DOUBLE_EQUAL_SIGN; }
    {OP_EQUAL} { yybegin(IN_OPTION_VALUE); return EQUAL_SIGN; }
    {OP_NOT_EQUAL} { yybegin(IN_OPTION_VALUE); return NOT_EQUAL_SIGN; }
}
<IN_OPTION, IN_OPTION_VALUE> {
    {BOOLEAN_TOKEN} { yybegin(IN_OPTION); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(IN_OPTION); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(IN_OPTION); return FLOAT_TOKEN; }
    {STRING_CONTENT} { yybegin(IN_OPTION); return STRING_TOKEN; }
    // use trailing context (high priority than normal form)
    {OPTION_KEY_CONTENT} / {BLANK}?{SEPARATOR} { yybegin(IN_OPTION_SEPARATOR); return OPTION_KEY_TOKEN; }
    {OPTION_TEXT_TOKEN} { yybegin(IN_OPTION); return STRING_TOKEN; }
}

<IN_OPTION_NESTED, IN_OPTION_SEPARATOR_NESTED, IN_OPTION_VALUE_NESTED> {
    "{" { return LEFT_BRACE; }
    "}" { return RIGHT_BRACE; }
    {EOL} { yybegin(YYINITIAL); return EOL; }
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { yybegin(YYINITIAL);  return COMMENT; }
}
<IN_OPTION_SEPARATOR_NESTED> {
    {OP_DOUBLE_EQUAL} { yybegin(IN_OPTION_VALUE_NESTED); return DOUBLE_EQUAL_SIGN; }
    {OP_EQUAL} { yybegin(IN_OPTION_VALUE_NESTED); return EQUAL_SIGN; }
    {OP_NOT_EQUAL} { yybegin(IN_OPTION_VALUE_NESTED); return NOT_EQUAL_SIGN; }
}
<IN_OPTION_NESTED, IN_OPTION_VALUE_NESTED> {
    {BOOLEAN_TOKEN} { yybegin(IN_OPTION_NESTED); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(IN_OPTION_NESTED); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(IN_OPTION_NESTED); return FLOAT_TOKEN; }
    // use trailing context (high priority than normal form)
    {OPTION_KEY_CONTENT} / {BLANK}?{SEPARATOR} { yybegin(IN_OPTION_SEPARATOR_NESTED); return OPTION_KEY_TOKEN; }
    {STRING_CONTENT} { yybegin(IN_OPTION_NESTED); return STRING_TOKEN; }
}

[^] { return BAD_CHARACTER; }
