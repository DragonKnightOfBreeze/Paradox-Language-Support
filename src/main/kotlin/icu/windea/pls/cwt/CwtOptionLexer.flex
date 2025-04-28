package icu.windea.pls.cwt.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import java.util.*;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;

%%

%{
    private volatile int depth;
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final Deque<Integer> optionStack = new ArrayDeque<>();

    public _CwtOptionLexer() {
        this((java.io.Reader)null);
    }

    private void beginNextState() {
        if (depth <= 0) {
            yybegin(YYINITIAL);
        } else {
            yybegin(IN_BLOCK);
        }
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
%class _CwtOptionLexer
%implements FlexLexer
%function advance
%type IElementType

%s IN_OPTION
%s IN_OPTION_KEY
%s IN_OPTION_SEPARATOR
%s IN_OPTION_VALUE
%s IN_OPTION_VALUE_TOP_STRING
%s EXPECT_NEXT_OPTION

%s IN_PROPERTY_KEY
%s IN_PROPERTY_SEPARATOR
%s IN_PROPERTY_VALUE
%s EXPECT_NEXT

%s IN_BLOCK

%unicode

BLANK=\s+

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
QUOTED_STRING_TOKEN=\"([^\"\\\r\n]|\\[\s\S])*\"?

// top option value can contain whitespaces
TOP_STRING_TOKEN=([^#={}\s\"]([^#={}\r\n\"]*[^#={}\s\"])?\"?)|({QUOTED_STRING_TOKEN})

%%

<YYINITIAL> {
    "##" {  return OPTION_COMMENT_START; }
    "{" { depth++; return LEFT_BRACE; }
    "}" { depth--; return RIGHT_BRACE; }
    {BLANK} { return WHITE_SPACE; }

    {CHECK_OPTION_KEY} { yypushback(yylength()); yybegin(IN_OPTION_KEY); }
    {BOOLEAN_TOKEN} { return BOOLEAN_TOKEN; }
    {INT_TOKEN} { return INT_TOKEN; }
    {FLOAT_TOKEN} { return FLOAT_TOKEN; }
    {TOP_STRING_TOKEN} { return STRING_TOKEN; }
}
<IN_PROPERTY_KEY>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(stack, YYINITIAL); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }

    {PROPERTY_KEY_TOKEN} { yybegin(IN_PROPERTY_SEPARATOR); return PROPERTY_KEY_TOKEN; }
}
<IN_PROPERTY_SEPARATOR>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(stack, YYINITIAL); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "="|"==" { yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN; }
    "!="|"<>" { yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN; }
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
}
<EXPECT_NEXT>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(stack, YYINITIAL); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
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
}
<IN_OPTION_KEY>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(optionStack, IN_OPTION); return LEFT_BRACE; }
    "}" { exitState(optionStack, IN_OPTION); return RIGHT_BRACE; }

    {OPTION_KEY_TOKEN} { yybegin(IN_OPTION_SEPARATOR); return OPTION_KEY_TOKEN; }
}
<IN_OPTION_SEPARATOR>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(optionStack, IN_OPTION); return LEFT_BRACE; }
    "}" { exitState(optionStack, IN_OPTION); return RIGHT_BRACE; }
    "="|"==" { yybegin(IN_OPTION_VALUE); return EQUAL_SIGN; }
    "!="|"<>" { yybegin(IN_OPTION_VALUE); return NOT_EQUAL_SIGN; }
}

<IN_OPTION_VALUE_TOP_STRING>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(optionStack, IN_OPTION); return LEFT_BRACE; }
    "}" { exitState(optionStack, IN_OPTION); return RIGHT_BRACE; }

    {TOP_STRING_TOKEN} { yybegin(EXPECT_NEXT_OPTION); return STRING_TOKEN; }
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
}

<EXPECT_NEXT_OPTION>{
    {BLANK} { processBlank(); return WHITE_SPACE; }
    "{" { enterState(optionStack, IN_OPTION); return LEFT_BRACE; }
    "}" { exitState(optionStack, IN_OPTION); return RIGHT_BRACE; }
}

[^] { return BAD_CHARACTER; }
