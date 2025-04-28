package icu.windea.pls.cwt.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import java.util.*;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;

%%

%{
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final Deque<Integer> optionStack = new ArrayDeque<>();

    public _CwtLexer() {
        this((java.io.Reader)null);
    }

    private void enterState(Deque<Integer> stack, int state) {
        stack.offerLast(state);
        yybegin(state);
    }

    private void exitState(Deque<Integer> stack, int defaultState) {
        Integer state = stack.pollLast();
        if(state != null) {
            yybegin(state);
        } else {
            yybegin(defaultState);
        }
    }

    private void processBlank() {
        boolean lineBreak = false;
        for (int i = 0; i < yylength(); i++) {
            char c = yycharat(i);
            if(c == '\r' || c == '\n') {
                lineBreak = true;
                break;
            }
        }
        if(lineBreak) {
            yybegin(YYINITIAL);
            optionStack.clear();
        } else {
            if(yystate() == EXPECT_NEXT) {
                yybegin(YYINITIAL);
                optionStack.clear();
            } else if(yystate() == EXPECT_NEXT_OPTION) {
                yybegin(IN_OPTION);
            }
        }
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
%s EXPECT_NEXT
%s IN_DOCUMENTATION
%s IN_OPTION
%s IN_OPTION_KEY
%s IN_OPTION_SEPARATOR
%s IN_OPTION_VALUE
%s IN_OPTION_VALUE_TOP_STRING
%s EXPECT_NEXT_OPTION

%unicode

BLANK=\s+

DOC_COMMENT_START=###
OPTION_COMMENT_START=##
DOC_COMMENT_TOKEN=[^\s][^\r\n]*
COMMENT=(#)|(#[^#\r\n][^\r\n]*)
RELAX_COMMENT=#[^\r\n]*

CHECK_SEPARATOR=(=)|(\!=)|(<>)
CHECK_PROPERTY_KEY=({PROPERTY_KEY_TOKEN})?\s*{CHECK_SEPARATOR}
CHECK_OPTION_KEY=({OPTION_KEY_TOKEN})?\s*{CHECK_SEPARATOR}

PROPERTY_KEY_TOKEN=([^#={}\s\"]+\"?)|({QUOTED_KEY_TOKEN})
OPTION_KEY_TOKEN=([^#={}\s\"]+\"?)|({QUOTED_KEY_TOKEN})
QUOTED_KEY_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?
BOOLEAN_TOKEN=(yes)|(no)
INT_TOKEN=[+-]?[0-9]+ // leading zero is permitted
FLOAT_TOKEN=[+-]?[0-9]*(\.[0-9]+) // leading zero is permitted
STRING_TOKEN=([^#={}\s\"]+\"?)|({QUOTED_STRING_TOKEN})
TOP_STRING_TOKEN=([^#={}\s\"]([^#={}\r\n\"]*[^#={}\s\"])?\"?)|({QUOTED_STRING_TOKEN}) // top option value can contain whitespaces
QUOTED_STRING_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?

%%

<YYINITIAL> {
    {BLANK} { return WHITE_SPACE; }
    "{" { enterState(stack, YYINITIAL); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }

    {CHECK_PROPERTY_KEY} { yypushback(yylength()); yybegin(IN_PROPERTY_KEY); }
    {BOOLEAN_TOKEN} { yybegin(EXPECT_NEXT); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(EXPECT_NEXT); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(EXPECT_NEXT); return FLOAT_TOKEN; }
    {STRING_TOKEN} { yybegin(EXPECT_NEXT); return STRING_TOKEN; }

    {DOC_COMMENT_START} { yybegin(IN_DOCUMENTATION); return DOC_COMMENT_START; }
    {OPTION_COMMENT_START} { yybegin(IN_OPTION); return OPTION_COMMENT_START; }
    {COMMENT} { return COMMENT; }
}
<IN_PROPERTY_KEY>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(stack, YYINITIAL); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }

    {PROPERTY_KEY_TOKEN} { yybegin(IN_PROPERTY_SEPARATOR); return PROPERTY_KEY_TOKEN; }

    {COMMENT} { return COMMENT; }
}
<IN_PROPERTY_SEPARATOR>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(stack, YYINITIAL); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "="|"==" { yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN; }
    "!="|"<>" { yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN; }

    {COMMENT} { return COMMENT; }
}
<IN_PROPERTY_VALUE>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(stack, YYINITIAL); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }

    {CHECK_PROPERTY_KEY} { yypushback(yylength()); yybegin(IN_PROPERTY_KEY); }
    {BOOLEAN_TOKEN} { yybegin(EXPECT_NEXT); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(EXPECT_NEXT); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(EXPECT_NEXT); return FLOAT_TOKEN; }
    {STRING_TOKEN} { yybegin(EXPECT_NEXT); return STRING_TOKEN; }

    {DOC_COMMENT_START} { yybegin(IN_DOCUMENTATION); return DOC_COMMENT_START; }
    {OPTION_COMMENT_START} { yybegin(IN_OPTION); return OPTION_COMMENT_START; }
    {COMMENT} { return COMMENT; }
}
<EXPECT_NEXT>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(stack, YYINITIAL); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }

    {RELAX_COMMENT} { return COMMENT; }
}

<IN_DOCUMENTATION>{
    {BLANK} { processBlank(); return WHITE_SPACE; }

    {DOC_COMMENT_TOKEN} { yybegin(YYINITIAL); return DOC_COMMENT_TOKEN; }
}

<IN_OPTION>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(optionStack, IN_OPTION); return LEFT_BRACE; }
    "}" { exitState(optionStack, IN_OPTION); return RIGHT_BRACE; }

    {CHECK_OPTION_KEY} { yypushback(yylength()); yybegin(IN_OPTION_KEY); }
    {BOOLEAN_TOKEN} { yybegin(EXPECT_NEXT_OPTION); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(EXPECT_NEXT_OPTION); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(EXPECT_NEXT_OPTION); return FLOAT_TOKEN; }
    {STRING_TOKEN} {
        if(optionStack.isEmpty()){
              yypushback(yylength()); yybegin(IN_OPTION_VALUE_TOP_STRING);
         } else {
              yybegin(EXPECT_NEXT_OPTION); return STRING_TOKEN;
        }
    }

    {RELAX_COMMENT} { return COMMENT; }
}
<IN_OPTION_KEY>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(optionStack, IN_OPTION); return LEFT_BRACE; }
    "}" { exitState(optionStack, IN_OPTION); return RIGHT_BRACE; }

    {OPTION_KEY_TOKEN} { yybegin(IN_OPTION_SEPARATOR); return OPTION_KEY_TOKEN; }

    {RELAX_COMMENT} { return COMMENT; }
}
<IN_OPTION_SEPARATOR>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(optionStack, IN_OPTION); return LEFT_BRACE; }
    "}" { exitState(optionStack, IN_OPTION); return RIGHT_BRACE; }
    "="|"==" { yybegin(IN_OPTION_VALUE); return EQUAL_SIGN; }
    "!="|"<>" { yybegin(IN_OPTION_VALUE); return NOT_EQUAL_SIGN; }

    {RELAX_COMMENT} { return COMMENT; }
}

<IN_OPTION_VALUE_TOP_STRING>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(optionStack, IN_OPTION); return LEFT_BRACE; }
    "}" { exitState(optionStack, IN_OPTION); return RIGHT_BRACE; }

    {TOP_STRING_TOKEN} { yybegin(EXPECT_NEXT_OPTION); return STRING_TOKEN; }

    {RELAX_COMMENT} { return COMMENT; }
}

<IN_OPTION_VALUE>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(optionStack, IN_OPTION); return LEFT_BRACE; }
    "}" { exitState(optionStack, IN_OPTION); return RIGHT_BRACE; }

    {CHECK_OPTION_KEY} { yypushback(yylength()); yybegin(IN_OPTION_KEY); }
    {BOOLEAN_TOKEN} { yybegin(EXPECT_NEXT_OPTION); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(EXPECT_NEXT_OPTION); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(EXPECT_NEXT_OPTION); return FLOAT_TOKEN; }
    {STRING_TOKEN} {
        if(optionStack.isEmpty()){
              yypushback(yylength()); yybegin(IN_OPTION_VALUE_TOP_STRING);
         } else {
              yybegin(EXPECT_NEXT_OPTION); return STRING_TOKEN;
        }
    }

    {RELAX_COMMENT} { return COMMENT; }
}

<EXPECT_NEXT_OPTION>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(optionStack, IN_OPTION); return LEFT_BRACE; }
    "}" { exitState(optionStack, IN_OPTION); return RIGHT_BRACE; }

    {RELAX_COMMENT} { return COMMENT; }
}

[^] { return BAD_CHARACTER; }
