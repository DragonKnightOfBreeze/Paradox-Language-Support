package icu.windea.pls.script.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.model.ParadoxGameType;
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.core.StdlibExtensionsKt.*;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;

// Lexer for Paradox Script.

%%

%{
    private ParadoxGameType gameType;

    // stack for context states (states that need to fallback when exit some constructs)
    private IntStack stateStack = null;
    // stack for expected construct types (e.g., EXPECT_BLOCK)
    private IntStack expectStack = null;

    private static final int EXPECT_ROOT_BLOCK = 1;
    private static final int EXPECT_BLOCK = 2;
    private static final int EXPECT_CONDITIONAL_BLOCK = 3;
    private static final int EXPECT_CONDITIONAL_BLOCK_BODY = 4;
    private static final int EXPECT_SCRIPTED_VARIABLE = 5;
    private static final int EXPECT_INLINE_MATH = 6;

    private static final int EXPECT_PROPERTY_KEY = 11;
    private static final int EXPECT_STRING = 12;
    private static final int EXPECT_SCRIPTED_VARIABLE_NAME = 13;
    private static final int EXPECT_SCRIPTED_VARIABLE_REFERENCE = 14;

    private static final int EXPECT_PARAMETER = 21;
    private static final int EXPECT_INLINE_CONDITIONAL_BLOCK = 22;
    private static final int EXPECT_INLINE_CONDITIONAL_BLOCK_BODY = 23;

    public _ParadoxScriptLexer() {
        this((java.io.Reader)null);
        this.gameType = null;
    }

    public _ParadoxScriptLexer(ParadoxGameType gameType) {
        this((java.io.Reader)null);
        this.gameType = gameType;
    }

    public ParadoxGameType getGameType() {
        return this.gameType;
    }

    private void enterState(int state, int expect) {
        // enter state to `expect`
        if (stateStack == null) {
            stateStack = new IntArrayList();
        }
        if (expectStack == null) {
            expectStack = new IntArrayList();
        }
        stateStack.push(state);
        expectStack.push(expect);
        yybegin(state);
    }

    private void exitState(int expect) {
        // exit state to previous only if it matches `expect`
        if (stateStack == null || stateStack.isEmpty()) {
            yybegin(YYINITIAL);
            return;
        }
        if (expectStack == null || expectStack.isEmpty()) {
            yybegin(YYINITIAL);
            return;
        }
        int currentExpect = expectStack.topInt();
        if (currentExpect != expect) return;
        expectStack.popInt();
        int currentState = stateStack.popInt();
        yybegin(currentState);
    }

    private void exitState() {
        // exit state to previous only if it matches `expect`
        if (stateStack == null || stateStack.isEmpty()) {
            yybegin(YYINITIAL);
            return;
        }
        if (expectStack == null || expectStack.isEmpty()) {
            yybegin(YYINITIAL);
            return;
        }
        expectStack.popInt();
        int currentState = stateStack.popInt();
        yybegin(currentState);
    }

    private boolean exitStateForUnexpected() {
        // exit state for unexpcted tokens (bad character)
        // heuristic: exit when the character is a right bound marker (`}` `]` `$` `{` `[`)
        // heuristic: exit when the character is blank, and it's not a valid token in context
        char c = yycharat(0);
        if (c != '}' && c != ']' && c != '$' && c != '{' && c != '[' && !Character.isWhitespace(c)) return false;
        exitState();
        yypushback(yylength());
        return true;
    }

    private boolean isLeftQuoted() {
        return yycharat(0) == '"';
    }
%}

%public
%class _ParadoxScriptLexer
%implements FlexLexer
%function advance
%type IElementType

%s CHECK_SCRIPTED_VARIABLE
%s IN_SCRIPTED_VARIABLE_NAME
%s IN_SCRIPTED_VARIABLE_VALUE
%s CHECK_SCRIPTED_VARIABLE_REFERENCE
%s IN_SCRIPTED_VARIABLE_REFERENCE

%s IN_PROPERTY_OR_VALUE
%s IN_PROPERTY_VALUE
%s IN_PROPERTY_KEY_UNQUOTED
%s IN_PROPERTY_KEY_QUOTED
%s IN_STRING_UNQUOTED
%s IN_STRING_QUOTED

%s IN_PARAMETER
%s IN_PARAMETER_ARGUMENT

%s IN_CONDITIONAL_BLOCK
%s IN_CONDITIONAL_BLOCK_EXPRESSION
%s IN_CONDITIONAL_BLOCK_BODY
%s IN_INLINE_CONDITIONAL_BLOCK
%s IN_INLINE_CONDITIONAL_BLOCK_EXPRESSION
%s IN_INLINE_CONDITIONAL_BLOCK_BODY

%s IN_INLINE_MATH

%unicode

EOL=\s*\R\s*
WHITE_SPACE=[\s&&[^\r\n]]+
BLANK=\s+
COMMENT=#[^\r\n]*

SCRIPTED_VARIABLE_NAME_CHECK=[A-Za-z_$\[][^@#={}\"\s]* // leading number is not permitted
SCRIPTED_VARIABLE_NAME_TRAILING=\s*=
SCRIPTED_VARIABLE_NAME_TOKEN=[A-Za-z0-9_]+ // leading number is not permitted

PROPERTY_KEY_CHECK={PROPERTY_KEY_CHECK_UNQUOTED}|{PROPERTY_KEY_CHECK_QUOTED}
PROPERTY_KEY_CHECK_UNQUOTED=[^@#=<>!?{}\"\s][^#=<>!?{}\"\s]*\"?
PROPERTY_KEY_CHECK_QUOTED=\"([^\"\\\r\n]|\\.)*\"?
PROPERTY_KEY_TRAILING=\s*[=<>!?]
PROPERTY_KEY_TOKEN_UNQUOTED=[^@#=<>!?{}\"\s$\[\]][^#=<>!?{}\"\s$\[\]]*\"?
PROPERTY_KEY_TOKEN_QUOTED=([^\"\\\r\n$\[\]]|\\[\s\S])+ // without surrounding quotes

STRING_CHECK={STRING_CHECK_UNQUOTED}|{STRING_CHECK_QUOTED}
STRING_CHECK_UNQUOTED=[^@#=<>!?{}\"\s][^#=<>!?{}\"\s]*\"?
STRING_CHECK_QUOTED=\"([^\"\\]|\\[\s\S])*\"?
STRING_TOKEN_UNQUOTED=[^@#=<>!?{}\"\s$\[\]][^#=<>!?{}\"\s$\[\]]*\"?
STRING_TOKEN_QUOTED=([^\"\\$\[\]]|\\[\s\S])+ // without surrounding quotes & line breaks are permitted

BOOLEAN_TOKEN=(yes)|(no)
INT_NUMBER_TOKEN=[0-9]+ // leading zero is permitted
INT_TOKEN=[+-]?{INT_NUMBER_TOKEN}
FLOAT_NUMBER_TOKEN=[0-9]*(\.[0-9]+) // leading zero is permitted
FLOAT_TOKEN=[+-]?{FLOAT_NUMBER_TOKEN}
COLOR_TOKEN=(rgb|hsv|hsv360)[ \t]*\{[\d.\s&&[^\r\n]]*} // #103 hsv360 (from vic3)

PARAMETER_TOKEN=[A-Za-z_][A-Za-z0-9_]* // leading number is not permitted for parameter names
ARGUMENT_TOKEN=[^#$=<>!?{}\[\]\s]+ // compatible with leading '@'

INLINE_MATH_TOKEN=[^\r\n#{}\[\]]+

%%

// block rules

<YYINITIAL, IN_CONDITIONAL_BLOCK_BODY> {
    "{" {
        enterState(yystate(), EXPECT_BLOCK);
        return LEFT_BRACE;
    }
    "[" {
        enterState(yystate(), EXPECT_CONDITIONAL_BLOCK);
        yybegin(IN_CONDITIONAL_BLOCK);
        return LEFT_BRACKET;
    }
    "@" {
        enterState(yystate(), EXPECT_SCRIPTED_VARIABLE);
        yybegin(CHECK_SCRIPTED_VARIABLE);
        return AT;
    }
    "}" {
        exitState(EXPECT_ROOT_BLOCK);
        return RIGHT_BRACE;
    }
    "]" {
        exitState(EXPECT_CONDITIONAL_BLOCK);
        return RIGHT_BRACKET;
    }

    {BLANK} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
}

// interpolation container rules

<IN_PROPERTY_KEY_UNQUOTED, IN_PROPERTY_KEY_QUOTED, IN_STRING_UNQUOTED, IN_STRING_QUOTED, IN_SCRIPTED_VARIABLE_NAME, IN_SCRIPTED_VARIABLE_REFERENCE> {
    "[" {
        enterState(yystate(), EXPECT_INLINE_CONDITIONAL_BLOCK);
        yybegin(IN_INLINE_CONDITIONAL_BLOCK);
        return LEFT_BRACKET;
    }
    "$" {
        enterState(yystate(), EXPECT_PARAMETER);
        yybegin(IN_PARAMETER);
        return PARAMETER_START;
    }
    "]" {
        exitState(EXPECT_INLINE_CONDITIONAL_BLOCK);
        return RIGHT_BRACKET;
    }
}

// separator rules

<YYINITIAL, IN_PROPERTY_OR_VALUE, IN_PROPERTY_KEY_UNQUOTED, CHECK_SCRIPTED_VARIABLE, IN_SCRIPTED_VARIABLE_NAME> {
    "=" { yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN; }
    "!="|"<>" {  yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN; }
    "<" { yybegin(IN_PROPERTY_VALUE); return LT_SIGN; }
    ">" { yybegin(IN_PROPERTY_VALUE); return GT_SIGN; }
    "<=" { yybegin(IN_PROPERTY_VALUE); return LE_SIGN; }
    ">=" { yybegin(IN_PROPERTY_VALUE); return GE_SIGN; }
}
<YYINITIAL, IN_PROPERTY_OR_VALUE, IN_PROPERTY_KEY_UNQUOTED> {
    // #86 supported in ck3, vic3 and eu5 (preferred format: `k ?= v`)
    "?=" { yybegin(IN_PROPERTY_VALUE); return SAFE_ASSIGN_SIGN; }
    // 2.1.10 #331 supported in stellaris 4.4 (preferred format: `k? = v`)
    \?\s+= { yybegin(IN_PROPERTY_VALUE); return SAFE_CALL_ASSIGN_SIGN; }
}

// member and expression rules

<IN_PROPERTY_OR_VALUE> {
    "@" { yybegin(CHECK_SCRIPTED_VARIABLE); return AT; }
    {BLANK} { return WHITE_SPACE; } // keep
    {COMMENT} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}
<IN_PROPERTY_VALUE> {
    "@" { yybegin(CHECK_SCRIPTED_VARIABLE_REFERENCE); return AT; }
    {BLANK} { return WHITE_SPACE; } // keep
    {COMMENT} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}

<YYINITIAL, IN_CONDITIONAL_BLOCK_BODY, IN_PROPERTY_OR_VALUE, IN_PROPERTY_VALUE, IN_SCRIPTED_VARIABLE_VALUE> {
    "@["|"@\\[" {
        enterState(yystate(), EXPECT_INLINE_MATH);
        yybegin(IN_INLINE_MATH);
        return INLINE_MATH_START;
    }
    {BOOLEAN_TOKEN} { return BOOLEAN_TOKEN; }
    {INT_TOKEN} { return INT_TOKEN; }
    {FLOAT_TOKEN} { return FLOAT_TOKEN; }
}
<YYINITIAL, IN_CONDITIONAL_BLOCK_BODY, IN_PROPERTY_OR_VALUE, IN_PROPERTY_VALUE, IN_SCRIPTED_VARIABLE_VALUE> {
    {COLOR_TOKEN} { return COLOR_TOKEN; }
    {PROPERTY_KEY_CHECK} / {PROPERTY_KEY_TRAILING} {
        if (isLeftQuoted()) {
            yypushback(yylength() - 1);
            yybegin(IN_PROPERTY_KEY_QUOTED);
            return LEFT_QUOTE;
        } else {
            yypushback(yylength());
            yybegin(IN_PROPERTY_KEY_UNQUOTED);
        }
    }
    {STRING_CHECK} {
        if (isLeftQuoted()) {
            yypushback(yylength() - 1);
            yybegin(IN_STRING_QUOTED);
            return LEFT_QUOTE;
        } else {
            yypushback(yylength());
            yybegin(IN_STRING_UNQUOTED);
        }
    }
}

<IN_PROPERTY_KEY_UNQUOTED> {
    {PROPERTY_KEY_TOKEN_UNQUOTED} { return PROPERTY_KEY_TOKEN; }
    {BLANK} { exitState(); return WHITE_SPACE; } // keep
    {COMMENT} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}
<IN_PROPERTY_KEY_QUOTED> {
    {EOL} { exitState(); return WHITE_SPACE; }
    {PROPERTY_KEY_TOKEN_QUOTED} { return PROPERTY_KEY_TOKEN; }
    \" { exitState(); return RIGHT_QUOTE; }
}
<IN_STRING_UNQUOTED> {
    {STRING_TOKEN_UNQUOTED} { return STRING_TOKEN; }
    {BLANK} { exitState(); return WHITE_SPACE; } // keep
    {COMMENT} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}
<IN_STRING_QUOTED> {
    // {EOL} { exitState(); return WHITE_SPACE; } // quoted multiline string is permited
    {STRING_TOKEN_QUOTED} { return STRING_TOKEN; }
    \" { exitState(); return RIGHT_QUOTE; }
}

// scripted variable rules

<CHECK_SCRIPTED_VARIABLE> {
    {SCRIPTED_VARIABLE_NAME_CHECK} / {SCRIPTED_VARIABLE_NAME_TRAILING} {
        enterState(yystate(), EXPECT_SCRIPTED_VARIABLE_NAME);
        yypushback(yylength());
        yybegin(IN_SCRIPTED_VARIABLE_NAME);
    }
    {SCRIPTED_VARIABLE_NAME_CHECK} {
        enterState(yystate(), EXPECT_SCRIPTED_VARIABLE_REFERENCE);
        yypushback(yylength());
        yybegin(IN_SCRIPTED_VARIABLE_REFERENCE);
    }
}
<IN_SCRIPTED_VARIABLE_NAME> {
    {SCRIPTED_VARIABLE_NAME_TOKEN} { return SCRIPTED_VARIABLE_NAME_TOKEN; }
    {BLANK} { return WHITE_SPACE; }
    {COMMENT} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}
<IN_SCRIPTED_VARIABLE_VALUE> {
    {BLANK} { return WHITE_SPACE; }
    {COMMENT} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}
<CHECK_SCRIPTED_VARIABLE_REFERENCE> {
    {SCRIPTED_VARIABLE_NAME_CHECK} {
        enterState(yystate(), EXPECT_SCRIPTED_VARIABLE_REFERENCE);
        yypushback(yylength());
        yybegin(IN_SCRIPTED_VARIABLE_REFERENCE);
    }
}
<IN_SCRIPTED_VARIABLE_REFERENCE> {
    {SCRIPTED_VARIABLE_NAME_TOKEN} { return SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
    {BLANK} { return WHITE_SPACE; }
    {COMMENT} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}

// parameter rules

<IN_PARAMETER, IN_PARAMETER_ARGUMENT> {
    "$" {
        exitState(EXPECT_PARAMETER);
        return PARAMETER_END;
    }
}
<IN_PARAMETER> {
    "|" { yybegin(IN_PARAMETER_ARGUMENT); return PIPE; }
    {PARAMETER_TOKEN} { return PARAMETER_TOKEN; }
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}
<IN_PARAMETER_ARGUMENT> {
    {ARGUMENT_TOKEN} { return ARGUMENT_TOKEN; }
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}

// conditional block rules

<IN_CONDITIONAL_BLOCK> {
    "[" {
        yybegin(IN_CONDITIONAL_BLOCK_EXPRESSION);
        return NESTED_LEFT_BRACKET; }
    "]" {
        exitState(EXPECT_CONDITIONAL_BLOCK);
        return RIGHT_BRACKET;
    }

    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}
<IN_CONDITIONAL_BLOCK_EXPRESSION> {
    "]" {
        enterState(yystate(), EXPECT_CONDITIONAL_BLOCK_BODY);
        yybegin(IN_CONDITIONAL_BLOCK_BODY);
        return NESTED_RIGHT_BRACKET;
    }

    "!" { return NOT_SIGN; }
    {PARAMETER_TOKEN} { return CONDITION_PARAMETER_TOKEN; }
    {BLANK} { return WHITE_SPACE; }
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}
<IN_CONDITIONAL_BLOCK_BODY> {
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}
<IN_INLINE_CONDITIONAL_BLOCK> {
    "[" {
        yybegin(IN_INLINE_CONDITIONAL_BLOCK_EXPRESSION);
        return NESTED_LEFT_BRACKET;
    }
    "]" {
        exitState(EXPECT_INLINE_CONDITIONAL_BLOCK);
        return RIGHT_BRACKET;
    }

    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}
<IN_INLINE_CONDITIONAL_BLOCK_EXPRESSION> {
    "]" {
        enterState(yystate(), EXPECT_INLINE_CONDITIONAL_BLOCK_BODY);
        yybegin(IN_INLINE_CONDITIONAL_BLOCK_BODY);
        return NESTED_RIGHT_BRACKET;
    }

    "!" { return NOT_SIGN; }
    {PARAMETER_TOKEN} { return CONDITION_PARAMETER_TOKEN; }
    // {BLANK} { return WHITE_SPACE; } // should not be permited
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}
<IN_INLINE_CONDITIONAL_BLOCK_BODY> {
    "[" {
        enterState(yystate(), EXPECT_INLINE_CONDITIONAL_BLOCK);
        yybegin(IN_INLINE_CONDITIONAL_BLOCK);
        return LEFT_BRACKET;
    }
    "]" {
        exitState(EXPECT_INLINE_CONDITIONAL_BLOCK);
        return RIGHT_BRACKET;
    }
    // {BLANK} { return WHITE_SPACE; } // should not be permited
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}

// inline math rules

<IN_INLINE_MATH> {
    "]" {
        exitState(EXPECT_INLINE_MATH);
        return INLINE_MATH_END;
    }

    {INLINE_MATH_TOKEN} { return INLINE_MATH_TOKEN; }
    {BLANK} { return WHITE_SPACE; } // keep
    [^] { if (!exitStateForUnexpected()) return BAD_CHARACTER; } // recovery
}

[^] { return BAD_CHARACTER; }
