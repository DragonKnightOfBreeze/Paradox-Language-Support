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
// TODO 3.0.2 refactor

%%

%{
    private ParadoxGameType gameType;

    // stack for context states (states that need to fallback when exit some constructs)
    private IntStack stateStack = null;
    // stack for expected construct types (e.g., EXPECT_BLOCK)
    private IntStack expectStack = null;

    private static final int EXPECT_BLOCK = 1;
    private static final int EXPECT_CONDITIONAL_BLOCK = 2;
    private static final int EXPECT_CONDITIONAL_BLOCK_EXPRESSION = 3;
    private static final int EXPECT_INLINE_MATH = 4;

    private static final int EXPECT_PROPERTY_KEY = 11;
    private static final int EXPECT_STRING = 12;
    private static final int EXPECT_SCRIPTED_VARIABLE_NAME = 13;
    private static final int EXPECT_SCRIPTED_VARIABLE_REFERENCE = 14;

    private static final int EXPECT_PARAMETER = 21;
    private static final int EXPECT_INLINE_CONDITIONAL_BLOCK = 22;
    private static final int EXPECT_INLINE_CONDITIONAL_BLOCK_EXPRESSION = 23;

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

    private boolean exitStateAtBadCharacter() {
        // exit state for bad character (as fallback)
        if (isNotExitStateChar()) return false;
        exitState();
        yypushback(yylength());
        return true;
    }

    private void exitStateForValue() {
        // double-exit
        exitState();
        exitState();
    }

    private boolean exitStateAtBadCharacterForValue() {
        // double-exit
        if (isNotExitStateChar()) return false;
        exitState();
        exitState();
        yypushback(yylength());
        return true;
    }

    private void beginStateAfterSeparator() {
        int state = yystate();
        if (state == IN_PROPERTY_KEY_UNQUOTED || state == IN_SCRIPTED_VARIABLE) {
            exitState();
        }
        if (state == IN_SCRIPTED_VARIABLE || state == IN_SCRIPTED_VARIABLE_NAME) {
            yybegin(IN_SCRIPTED_VARIABLE_VALUE);
        } else {
            yybegin(IN_PROPERTY_VALUE);
        }
    }

    private boolean isLeftQuoted() {
        char c = yycharat(0);
        return c == '"';
    }

    private boolean isNotExitStateChar() {
        // heuristic: exit when the character is a bound marker (`}` `]` `$` `{` `[`)
        // heuristic: exit when the character is blank, and it's not a valid token in context
        char c = yycharat(0);
        return c != '}' && c != ']' && c != '$' && c != '{' && c != '[' && !Character.isWhitespace(c);
    }
%}

%public
%class _ParadoxScriptLexer
%implements FlexLexer
%function advance
%type IElementType

%s IN_PROPERTY
%s IN_PROPERTY_VALUE
%s IN_PROPERTY_KEY_UNQUOTED
%s IN_PROPERTY_KEY_QUOTED
%s IN_STRING_UNQUOTED
%s IN_STRING_QUOTED

%s CHECK_SCRIPTED_VARIABLE
%s CHECK_SCRIPTED_VARIABLE_REFERENCE
%s IN_SCRIPTED_VARIABLE
%s IN_SCRIPTED_VARIABLE_VALUE
%s IN_SCRIPTED_VARIABLE_NAME
%s IN_SCRIPTED_VARIABLE_REFERENCE

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
//WHITE_SPACE=[\s&&[^\r\n]]+
BLANK=\s+

COMMENT=#[^\r\n]*

KEYWORD_YES = yes
KEYWORD_NO = no
KEYWORD_BOOLEAN = {KEYWORD_YES}|{KEYWORD_NO}

OP_UNARY_PLUS = "+"
OP_UNARY_MINUS = "-"
OP_EQUAL = "="
OP_NOT_EQUAL = "!="|"<>"
OP_LE = "<="
OP_GE = ">="
OP_LT = "<"
OP_GT = ">"
OP_SAFE_ASSIGN = "?="
OP_SAFE_CALL_ASSIGN = "?"{BLANK}"="
OP_NOT = "!"

QUOTE=\"
NUMBER_UNARY = {OP_UNARY_PLUS}|{OP_UNARY_MINUS}
SEPARATOR = {OP_EQUAL}
PROPERTY_SEPARATOR = {OP_EQUAL}|{OP_NOT_EQUAL}|{OP_LE}|{OP_GE}|{OP_LT}|{OP_GT}|{OP_SAFE_ASSIGN}|{OP_SAFE_CALL_ASSIGN} // order-sensitive

INT_NUMBER_TOKEN=[0-9]+ // leading zero is allowed
FLOAT_NUMBER_TOKEN=[0-9]*\.[0-9]+ // leading zero is allowed

BOOLEAN_TOKEN={KEYWORD_BOOLEAN} // `yes` or `no` (case-sensitive)
INT_TOKEN={NUMBER_UNARY}?{INT_NUMBER_TOKEN} // with optional unary operator
FLOAT_TOKEN={NUMBER_UNARY}?{FLOAT_NUMBER_TOKEN} // with optional unary operator

LITERAL_CHAR=[^#=<>{}\"\s$\[\]] // `@!?` are allowed
LITERAL_BOUND_CHAR=[^#=<>{}\"\s$\[\]@!?] // `@!?` are not allowed
LITERAL_TOKEN={LITERAL_BOUND_CHAR}({LITERAL_CHAR}*{LITERAL_BOUND_CHAR})? // boundary `@!?` are not allowed

IDENTIFIER_CHAR=[A-Za-z0-9_]
IDENTIFIER_LEAD_CHAR=[A-Za-z_] // leading number is not allowed
IDENTIFIER_TOKEN={IDENTIFIER_LEAD_CHAR}{IDENTIFIER_CHAR}* // leading number is not allowed

INTERPOLATION_MARKER_CHAR=[$|\[\]!?]
INTERPOLATION_LEAD_CHAR=[$\[]

LITERAL_WILDCARD_CHAR={LITERAL_CHAR}|{INTERPOLATION_MARKER_CHAR}
LITERAL_WILDCARD_BOUND_CHAR={LITERAL_BOUND_CHAR}|{INTERPOLATION_MARKER_CHAR}
LITERAL_WILDCARD_TOKEN={LITERAL_WILDCARD_BOUND_CHAR}({LITERAL_WILDCARD_CHAR}*{LITERAL_WILDCARD_BOUND_CHAR})?

IDENTIFIER_WILDCARD_CHAR={IDENTIFIER_CHAR}|{INTERPOLATION_MARKER_CHAR}
IDENTIFIER_WILDCARD_LEAD_CHAR={IDENTIFIER_LEAD_CHAR}|{INTERPOLATION_MARKER_CHAR} // leading number is not allowed
IDENTIFIER_WILDCARD_TOKEN={IDENTIFIER_WILDCARD_LEAD_CHAR}{IDENTIFIER_WILDCARD_CHAR}* // leading number is not allowed

PARAMETER_TOKEN={IDENTIFIER_TOKEN} // identifier

CONDITION_PARAMETER_TOKEN={IDENTIFIER_TOKEN} // identifier

ARGUMENT_CHAR=[^#=<>!?{}\\\s$\[\]] // `@` is allowed
ARGUMENT_TOKEN={ARGUMENT_CHAR}+ // compatible with leading '@'

PROPERTY_KEY_TOKEN_QUOTED=([^\"\\\r\n$\[]|\\.)+ // without surrounding quotes & exclude `$[`
PROPERTY_KEY_WILDCARD_QUOTED=([^\"\\\r\n]|\\.)+ // without surrounding quotes
PROPERTY_KEY_TOKEN_UNQUOTED={LITERAL_TOKEN} // literal
PROPERTY_KEY_WILDCARD_UNQUOTED={LITERAL_WILDCARD_TOKEN} // literal wildcard
PROPERTY_KEY_WILDCARD=({QUOTE}{PROPERTY_KEY_WILDCARD_QUOTED}|{PROPERTY_KEY_WILDCARD_UNQUOTED}){QUOTE}?

STRING_TOKEN_QUOTED=([^\"\\$\[]|\\.)+ // without surrounding quotes & can be multiline & exclude `$[`
STRING_WILDCARD_QUOTED=([^\"\\]|\\.)+ // without surrounding quotes & can be multiline
STRING_TOKEN_UNQUOTED={LITERAL_TOKEN} // literal
STRING_WILDCARD_UNQUOTED={LITERAL_WILDCARD_TOKEN} // literal wildcard
STRING_WILDCARD=({QUOTE}{STRING_WILDCARD_QUOTED}|{STRING_WILDCARD_UNQUOTED}){QUOTE}?

SCRIPTED_VARIABLE_TOKEN={IDENTIFIER_TOKEN} // identifier
SCRIPTED_VARIABLE_WILDCARD={IDENTIFIER_WILDCARD_TOKEN} // identifier wildcard

// #103 hsv360 (from vic3)
// #399 color types are case-insensitive
// TODO 3.0.2+ better syntax support: split into more specific tokens (distinct from normal keywords/identifiers and normal blocks)
COLOR_TYPE_RGB=[rR][gG][bB]
COLOR_TYPE_HSV=[hH][sS][vV]
COLOR_TYPE_HSV360=[hH][sS][vV]360
COLOR_TYPE_TOKEN={COLOR_TYPE_RGB}|{COLOR_TYPE_HSV}|{COLOR_TYPE_HSV360}
COLOR_ARGS_TOKEN="{"[\d.\s&&[^\r\n]]*"}" // lenient match
COLOR_TOKEN={COLOR_TYPE_TOKEN}{COLOR_ARGS_TOKEN}

INLINE_MATH_TOKEN=[^\r\n#{}\[\]]+ // lenient match

// lazy-scanning inline math expressions (see `ParadoxScript.InlineMath.flex`)

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
        yybegin(CHECK_SCRIPTED_VARIABLE);
        return AT;
    }
    "}" {
        exitState(EXPECT_BLOCK);
        return RIGHT_BRACE;
    }
    "]" {
        exitState(EXPECT_CONDITIONAL_BLOCK);
        return RIGHT_BRACKET;
    }

    {BLANK} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
}

// separator rules

// also for `IN_PROPERTY_KEY_UNQUOTED` and `IN_SCRIPTED_VARIABLE_NAME` (be compatible with no arouding whitespaces)
<YYINITIAL, IN_CONDITIONAL_BLOCK_BODY, IN_PROPERTY, IN_PROPERTY_KEY_UNQUOTED, IN_SCRIPTED_VARIABLE, IN_SCRIPTED_VARIABLE_NAME> {
    {OP_EQUAL} { beginStateAfterSeparator(); return EQUAL_SIGN; }
    {OP_NOT_EQUAL} { beginStateAfterSeparator(); return NOT_EQUAL_SIGN; }
    {OP_LE} { beginStateAfterSeparator(); return LE_SIGN; }
    {OP_GE} { beginStateAfterSeparator(); return GE_SIGN; }
    {OP_LT} { beginStateAfterSeparator(); return LT_SIGN; }
    {OP_GT} { beginStateAfterSeparator(); return GT_SIGN; }
    // #86 supported in ck3, vic3 and eu5 (preferred format: `k ?= v`)
    {OP_SAFE_ASSIGN} { beginStateAfterSeparator(); return SAFE_ASSIGN_SIGN; }
    // 2.1.10 #331 supported in stellaris 4.4 (preferred format: `k? = v`)
    {OP_SAFE_CALL_ASSIGN} { beginStateAfterSeparator(); return SAFE_CALL_ASSIGN_SIGN; }
}

// interpolation container rules

<IN_PROPERTY_KEY_UNQUOTED, IN_PROPERTY_KEY_QUOTED, IN_STRING_UNQUOTED, IN_STRING_QUOTED, IN_SCRIPTED_VARIABLE_NAME, IN_SCRIPTED_VARIABLE_REFERENCE, IN_INLINE_CONDITIONAL_BLOCK_BODY> {
    // 3.0.2 may be a command start marker of some injected/embedded localisation text, need lookahead
    // use tail context (high priority than normal form)
    "[" / {BLANK}?"[" {
        enterState(yystate(), EXPECT_INLINE_CONDITIONAL_BLOCK); // TODO check
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

// property and expression rules

<YYINITIAL, IN_CONDITIONAL_BLOCK_BODY, IN_PROPERTY_VALUE, IN_SCRIPTED_VARIABLE_VALUE> {
    "@["|"@\\[" {
        enterState(yystate(), EXPECT_INLINE_MATH);
        yybegin(IN_INLINE_MATH);
        return INLINE_MATH_START;
    }
}
<IN_PROPERTY_VALUE> {
    "@" {
        // enterState(yystate(), EXPECT_SCRIPTED_VARIABLE_REFERENCE); // commented out, incorrect
        yybegin(CHECK_SCRIPTED_VARIABLE_REFERENCE);
        return AT;
    }
}
<YYINITIAL, IN_CONDITIONAL_BLOCK_BODY, IN_PROPERTY_VALUE, IN_SCRIPTED_VARIABLE_VALUE> {
    {BOOLEAN_TOKEN} { exitState(); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { exitState(); return INT_TOKEN; }
    {FLOAT_TOKEN} { exitState(); return FLOAT_TOKEN; }
    {COLOR_TOKEN} { exitState(); return COLOR_TOKEN; }
}
<YYINITIAL, IN_CONDITIONAL_BLOCK_BODY, IN_PROPERTY_VALUE, IN_SCRIPTED_VARIABLE_VALUE> {
    // use tail context (high priority than normal form)
    {PROPERTY_KEY_WILDCARD} / {BLANK}?{PROPERTY_SEPARATOR} {
        yybegin(IN_PROPERTY);
        enterState(yystate(), EXPECT_PROPERTY_KEY);
        if (isLeftQuoted()) {
            yypushback(yylength() - 1);
            yybegin(IN_PROPERTY_KEY_QUOTED);
            return LEFT_QUOTE;
        } else {
            yypushback(yylength());
            yybegin(IN_PROPERTY_KEY_UNQUOTED);
        }
    }
    {STRING_WILDCARD} {
        if (isLeftQuoted()) {
            yypushback(yylength() - 1);
            yybegin(IN_STRING_QUOTED);
            enterState(yystate(), EXPECT_STRING);
            return LEFT_QUOTE;
        } else {
            yypushback(yylength());
            yybegin(IN_STRING_UNQUOTED);
            enterState(yystate(), EXPECT_STRING);
        }
    }
}
<IN_PROPERTY> {
    {BLANK} { return WHITE_SPACE; } // keep
    {COMMENT} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_PROPERTY_VALUE> {
    {BLANK} { return WHITE_SPACE; } // keep
    {COMMENT} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}

<IN_PROPERTY_KEY_UNQUOTED> {
    "["|"]" { return PROPERTY_KEY_TOKEN; } // fallback
    {PROPERTY_KEY_TOKEN_UNQUOTED} { return PROPERTY_KEY_TOKEN; }
    {BLANK} { exitState(); return WHITE_SPACE; } // keep
    {COMMENT} { exitState(); return COMMENT; } // keep
    {QUOTE} { exitState(); return RIGHT_QUOTE; }
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_PROPERTY_KEY_QUOTED> {
    {EOL} { exitState(); return WHITE_SPACE; }
    "["|"]" { return PROPERTY_KEY_TOKEN; } // fallback
    {PROPERTY_KEY_TOKEN_QUOTED} { return PROPERTY_KEY_TOKEN; }
    {QUOTE} { exitState(); return RIGHT_QUOTE; }
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_STRING_UNQUOTED> {
    "["|"]" { return STRING_TOKEN; } // fallback
    {STRING_TOKEN_UNQUOTED} { return STRING_TOKEN; }
    {BLANK} { exitStateForValue(); return WHITE_SPACE; } // keep
    {COMMENT} { exitStateForValue(); return COMMENT; } // keep
    {QUOTE} { exitStateForValue(); return RIGHT_QUOTE; }
    [^] { if (!exitStateAtBadCharacterForValue()) return BAD_CHARACTER; } // recovery
}
<IN_STRING_QUOTED> {
    // quoted multiline string is allowed (which will break futher scanning and parsing while closing quote is missing)
    // {EOL} { exitState(); return WHITE_SPACE; }
    "["|"]" { return STRING_TOKEN; } // fallback
    {STRING_TOKEN_QUOTED} { return STRING_TOKEN; }
    {QUOTE} { exitStateForValue(); return RIGHT_QUOTE; }
    [^] { if (!exitStateAtBadCharacterForValue()) return BAD_CHARACTER; } // recovery
}

// scripted variable rules

<CHECK_SCRIPTED_VARIABLE> {
    // use tail context (high priority than normal form)
    {SCRIPTED_VARIABLE_WILDCARD} / {BLANK}?{SEPARATOR} {
        yybegin(IN_SCRIPTED_VARIABLE);
        enterState(yystate(), EXPECT_SCRIPTED_VARIABLE_NAME);
        yypushback(yylength());
        yybegin(IN_SCRIPTED_VARIABLE_NAME);
    }
    {SCRIPTED_VARIABLE_WILDCARD} {
        yypushback(yylength());
        yybegin(IN_SCRIPTED_VARIABLE_REFERENCE);
        enterState(yystate(), EXPECT_SCRIPTED_VARIABLE_REFERENCE);
    }
}
<CHECK_SCRIPTED_VARIABLE_REFERENCE> {
    {SCRIPTED_VARIABLE_WILDCARD} {
        yypushback(yylength());
        yybegin(IN_SCRIPTED_VARIABLE_REFERENCE);
        enterState(yystate(), EXPECT_SCRIPTED_VARIABLE_REFERENCE);
    }
}
<IN_SCRIPTED_VARIABLE> {
    {BLANK} { return WHITE_SPACE; }
    {COMMENT} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_SCRIPTED_VARIABLE_VALUE> {
    {BLANK} { return WHITE_SPACE; }
    {COMMENT} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_SCRIPTED_VARIABLE_NAME> {
    {SCRIPTED_VARIABLE_TOKEN} { return SCRIPTED_VARIABLE_NAME_TOKEN; }
    {BLANK} { exitState(); return WHITE_SPACE; } // keep
    {COMMENT} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_SCRIPTED_VARIABLE_REFERENCE> {
    {SCRIPTED_VARIABLE_TOKEN} { return SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
    {BLANK} { exitStateForValue(); return WHITE_SPACE; } // keep
    {COMMENT} { exitStateForValue(); return COMMENT; } // keep
    [^] { if (!exitStateAtBadCharacterForValue()) return BAD_CHARACTER; } // recovery
}

// inline math rules

<IN_INLINE_MATH> {
    "]" {
        exitState(EXPECT_INLINE_MATH);
        return INLINE_MATH_END;
    }

    {INLINE_MATH_TOKEN} { return INLINE_MATH_TOKEN; }
    {BLANK} { return WHITE_SPACE; } // keep
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
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
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_PARAMETER_ARGUMENT> {
    {ARGUMENT_TOKEN} { return ARGUMENT_TOKEN; }
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}

// conditional block rules

// 2. change context when necessary (e.g., remove `IN_PROPERTY` from `stateStack` if meets blank)
<IN_CONDITIONAL_BLOCK> {
    "[" {
        yybegin(IN_CONDITIONAL_BLOCK_EXPRESSION);
        return NESTED_LEFT_BRACKET; }
    "]" {
        exitState(EXPECT_CONDITIONAL_BLOCK);
        return RIGHT_BRACKET;
    }

    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_CONDITIONAL_BLOCK_EXPRESSION> {
    "]" {
        yybegin(IN_CONDITIONAL_BLOCK_BODY);
        return NESTED_RIGHT_BRACKET;
    }

    {OP_NOT} { return NOT_SIGN; }
    {CONDITION_PARAMETER_TOKEN} { return CONDITION_PARAMETER_TOKEN; }
    {BLANK} { return WHITE_SPACE; }
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_CONDITIONAL_BLOCK_BODY> {
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
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

    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_INLINE_CONDITIONAL_BLOCK_EXPRESSION> {
    "]" {
        yybegin(IN_INLINE_CONDITIONAL_BLOCK_BODY);
        return NESTED_RIGHT_BRACKET;
    }

    {OP_NOT} { return NOT_SIGN; }
    {CONDITION_PARAMETER_TOKEN} { return CONDITION_PARAMETER_TOKEN; }
    // {BLANK} { return WHITE_SPACE; } // should not be allowed
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_INLINE_CONDITIONAL_BLOCK_BODY> {
    {ARGUMENT_TOKEN} { exitState();  return ARGUMENT_TOKEN; } // TODO 3.0.2 enter correct state?
    // {BLANK} { return WHITE_SPACE; } // should not be allowed
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}

[^] { return BAD_CHARACTER; }
