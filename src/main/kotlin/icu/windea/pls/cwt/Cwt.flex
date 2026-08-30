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

Eol = \s*\R\s*
WhiteSpace = [\s&&[^\r\n]]+
Blank = \s+

Comment = #[^\r\n]*
OptionComment = ##[^\r\n]*
DocComment = ###[^\r\n]*

KeywordYes = yes
KeywordNo = no
KeywordBoolean = {KeywordYes}|{KeywordNo}

OpPlus = "+"
OpMinus = "-"

OpEqual = "="
OpNotEqual = "!="|"<>"
OpDoubleEqual = "=="

Quote = \"
NumberUnary = {OpPlus}|{OpMinus}
Separator = {OpDoubleEqual}|{OpEqual}|{OpNotEqual} // order-sensitive

IntNumberToken = [0-9]+ // leading zero is allowed
FloatNumberToken = [0-9]*\.[0-9]+ // leading zero is allowed

BooleanToken = {KeywordBoolean} // `yes` or `no` (case-sensitive)
IntToken = {NumberUnary}?{IntNumberToken} // with optional unary operator
FloatToken = {NumberUnary}?{FloatNumberToken} // with optional unary operator

LiteralChar = [^#={}\"\s] // `!?` are allowed (`<>` are always allowed)
LiteralBoundChar = [^#={}\"\s!?] // `!?` are not allowed (`<>` are always allowed)
LiteralToken = {LiteralBoundChar}({LiteralChar}*{LiteralBoundChar})? // boundary `@!?` are not allowed (`<>` are always allowed)

PropertyKeyTokenQuoted = ([^\"\\\r\n]|\\.)+ // without surrounding quotes
PropertyKeyTokenUnquoted = {LiteralToken} // literal
PropertyKeyContent = ({Quote}{PropertyKeyTokenQuoted}|{PropertyKeyTokenUnquoted}){Quote}?

StringTokenQuoted = ([^\"\\\r\n]|\\.)+ // without surrounding quotes
StringTokenUnquoted = {LiteralToken} // literal
StringContent = ({Quote}{StringTokenQuoted}|{StringTokenUnquoted}){Quote}?

OptionKeyTokenQuoted = ([^\"\\\r\n]|\\.)+ // without surrounding quotes
OptionKeyTokenUnquoted = {LiteralToken} // literal
OptionKeyContent = ({Quote}{OptionKeyTokenQuoted}|{OptionKeyTokenUnquoted}){Quote}?

// top level option text (value in option comment, or option value of some option in option comment)
// inner whitespaces are allowed and required
OptionTextChar = [^#=!<>{}\r\n] // heuristic
OptionTextBoundChar = [^#=!<>{}\"\s] // heuristic
OptionTextToken = {OptionTextBoundChar}({OptionTextChar}*{OptionTextBoundChar})? // heuristic

%%

<YYINITIAL, IN_PROPERTY_SEPARATOR, IN_PROPERTY_VALUE> {
    "{" { return LEFT_BRACE; }
    "}" { return RIGHT_BRACE; }
    {Blank} { return WHITE_SPACE; }
    {DocComment} { return DOC_COMMENT_TOKEN; }
    {OptionComment} { yypushback(yylength() - 2); yybegin(IN_OPTION); return OPTION_COMMENT_START; }
    {Comment} { return COMMENT; }
}

<IN_PROPERTY_SEPARATOR> {
    {OpDoubleEqual} { yybegin(IN_PROPERTY_VALUE); return DOUBLE_EQUAL_SIGN; }
    {OpEqual} { yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN; }
    {OpNotEqual} { yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN; }
}

<YYINITIAL, IN_PROPERTY_VALUE> {
    {BooleanToken} { yybegin(YYINITIAL); return BOOLEAN_TOKEN; }
    {IntToken} { yybegin(YYINITIAL); return INT_TOKEN; }
    {FloatToken} { yybegin(YYINITIAL); return FLOAT_TOKEN; }
    // use trailing context (high priority than normal form)
    {PropertyKeyContent} / {Blank}?{Separator} { yybegin(IN_PROPERTY_SEPARATOR); return PROPERTY_KEY_TOKEN; }
    {StringContent} { yybegin(YYINITIAL); return STRING_TOKEN; }
}
<IN_OPTION, IN_OPTION_SEPARATOR, IN_OPTION_VALUE> {
    "{" { beginStateInOption(); return LEFT_BRACE; }
    "}" { return RIGHT_BRACE; }
    {Eol} { yybegin(YYINITIAL); return EOL; }
    {WhiteSpace} { return WHITE_SPACE; }
    {Comment} { yybegin(YYINITIAL);  return COMMENT; }
}
<IN_OPTION_SEPARATOR> {
    {OpDoubleEqual} { yybegin(IN_OPTION_VALUE); return DOUBLE_EQUAL_SIGN; }
    {OpEqual} { yybegin(IN_OPTION_VALUE); return EQUAL_SIGN; }
    {OpNotEqual} { yybegin(IN_OPTION_VALUE); return NOT_EQUAL_SIGN; }
}
<IN_OPTION, IN_OPTION_VALUE> {
    {BooleanToken} { yybegin(IN_OPTION); return BOOLEAN_TOKEN; }
    {IntToken} { yybegin(IN_OPTION); return INT_TOKEN; }
    {FloatToken} { yybegin(IN_OPTION); return FLOAT_TOKEN; }
    {StringContent} { yybegin(IN_OPTION); return STRING_TOKEN; }
    // use trailing context (high priority than normal form)
    {OptionKeyContent} / {Blank}?{Separator} { yybegin(IN_OPTION_SEPARATOR); return OPTION_KEY_TOKEN; }
    {OptionTextToken} { yybegin(IN_OPTION); return STRING_TOKEN; }
}

<IN_OPTION_NESTED, IN_OPTION_SEPARATOR_NESTED, IN_OPTION_VALUE_NESTED> {
    "{" { return LEFT_BRACE; }
    "}" { return RIGHT_BRACE; }
    {Eol} { yybegin(YYINITIAL); return EOL; }
    {WhiteSpace} { return WHITE_SPACE; }
    {Comment} { yybegin(YYINITIAL);  return COMMENT; }
}
<IN_OPTION_SEPARATOR_NESTED> {
    {OpDoubleEqual} { yybegin(IN_OPTION_VALUE_NESTED); return DOUBLE_EQUAL_SIGN; }
    {OpEqual} { yybegin(IN_OPTION_VALUE_NESTED); return EQUAL_SIGN; }
    {OpNotEqual} { yybegin(IN_OPTION_VALUE_NESTED); return NOT_EQUAL_SIGN; }
}
<IN_OPTION_NESTED, IN_OPTION_VALUE_NESTED> {
    {BooleanToken} { yybegin(IN_OPTION_NESTED); return BOOLEAN_TOKEN; }
    {IntToken} { yybegin(IN_OPTION_NESTED); return INT_TOKEN; }
    {FloatToken} { yybegin(IN_OPTION_NESTED); return FLOAT_TOKEN; }
    // use trailing context (high priority than normal form)
    {OptionKeyContent} / {Blank}?{Separator} { yybegin(IN_OPTION_SEPARATOR_NESTED); return OPTION_KEY_TOKEN; }
    {StringContent} { yybegin(IN_OPTION_NESTED); return STRING_TOKEN; }
}

[^] { return BAD_CHARACTER; }
