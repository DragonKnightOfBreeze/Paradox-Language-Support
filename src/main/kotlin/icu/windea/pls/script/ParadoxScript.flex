// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

// Lexer for Paradox Script.
// Notes:
// - Use trailing context for high-priority rules.
// - Use `stateStack` and `expectStack` to manage lexer-level states.

package icu.windea.pls.script.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.model.ParadoxGameType;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;

%%

%{
    private ParadoxGameType gameType;

    // stack for context states (states that need to fallback when exit some constructs)
    private IntArrayList stateStack = null;
    // stack for expected construct types (e.g., EXPECT_BLOCK)
    private IntArrayList expectStack = null;

    private static final int EXPECT_BLOCK = 1;
    private static final int EXPECT_PROPERTY_KEY = 2;
    private static final int EXPECT_STRING = 3;
    private static final int EXPECT_SCRIPTED_VARIABLE_CHECK = 4;
    private static final int EXPECT_SCRIPTED_VARIABLE_NAME = 5;
    private static final int EXPECT_SCRIPTED_VARIABLE_REFERENCE = 6;
    private static final int EXPECT_INLINE_MATH = 7;
    private static final int EXPECT_PARAMETER = 8;
    private static final int EXPECT_CONDITIONAL_BLOCK = 9;
    private static final int EXPECT_CONDITIONAL_BLOCK_EXPRESSION = 10;
    private static final int EXPECT_CONDITIONAL = 11;
    private static final int EXPECT_INLINE_CONDITIONAL = 12;

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
        if (stateStack == null || stateStack.isEmpty() || expectStack == null || expectStack.isEmpty()) {
            yybegin(YYINITIAL);
            return;
        }
        if (expectStack.topInt() != expect) return;
        int nextState = stateStack.popInt();
        expectStack.popInt();
        yybegin(nextState);
    }

    private void exitStateForRecovery() {
        // used for recovery
        if (stateStack == null || stateStack.isEmpty() || expectStack == null || expectStack.isEmpty()) {
            yybegin(YYINITIAL);
            return;
        }

        // compatible with conditional blocks
        if (beginStateInConditionalBodyToNormalForm()) return;

        int nextState = stateStack.popInt();
        expectStack.popInt();
        yybegin(nextState);
    }

    private boolean exitStateForRecoveryIfNeeded() {
        // used for final recovery
        if (!needExitStateForRecovery()) return false;
        exitStateForRecovery();
        yypushback(yylength());
        return true;
    }

    private boolean needExitStateForRecovery() {
        // heuristic: recovery when the character is a boundary marker (`}]${[`)
        // heuristic: recovery when the character is blank (and it's not a valid token in previous context)
        char c = yycharat(0);
        if (c == '}' || c == ']' || c == '$' || c == '{' || c == '[') return true;
        if (Character.isWhitespace(c)) return true;
        return false;
    }

    private void beginStateAfterSeparator() {
        // compatible with conditional blocks
        if (beginStateInConditionalBodyToNormalForm()) return;

        int state = yystate();
        if (state == IN_SCRIPTED_VARIABLE_NAME) {
            exitState(EXPECT_SCRIPTED_VARIABLE_NAME); // exist state if necessary
            yybegin(IN_SCRIPTED_VARIABLE_VALUE);
        } else {
            exitState(EXPECT_PROPERTY_KEY); // exist state if necessary
            yybegin(IN_PROPERTY_VALUE);
        }
    }

    private void beginStateAfterValue() {
        yybegin(YYINITIAL);
    }

    private boolean beginStateInConditionalBody() {
        // 3.0.2 recovery and inherit context from context stack
        // peek state X (i = 0) and then enter state X and the corresponding expect
        // where X may be YYINITIAL, IN_PROPERTY_KEY_UNQUOTED, IN_PROPERTY_KEY_QUOTED, etc.
        // which represents that current context is in some member container or interpolation container

        // Example:
        // key = [[PARAM]text]
        //              ^ here
        //
        // Example flow:
        // YYINITIAL
        // -> enter (YYINITIAL, EXPECT_PROPERTY_KEY) -> begin IN_PROPERTY_KEY_UNQUOTED
        // -> enter (IN_PROPERTY_KEY_UNQUOTED, EXPECT_CONDITIONAL_BLOCK) -> begin IN_CONDITIONAL_BLOCK
        // -> begin IN_CONDITIONAL_BLOCK_EXPRESSION
        // -> meet `]`
        // -> peek state X (i = 0)
        // -> enter (IN_PROPERTY_KEY_UNQUOTED, EXPECT_INLINE_CONDITIONAL) -> begin IN_PROPERTY_KEY_UNQUOTED

        if (stateStack == null || stateStack.isEmpty() || expectStack == null || expectStack.isEmpty()) {
            yybegin(YYINITIAL);
            return false;
        }
        int nextState = stateStack.peekInt(0);
        int nextExpect = switch (nextState) {
            case IN_PROPERTY_KEY_UNQUOTED, IN_PROPERTY_KEY_QUOTED -> EXPECT_INLINE_CONDITIONAL;
            case IN_STRING_UNQUOTED, IN_STRING_QUOTED -> EXPECT_INLINE_CONDITIONAL;
            case IN_SCRIPTED_VARIABLE_NAME -> EXPECT_INLINE_CONDITIONAL;
            case IN_SCRIPTED_VARIABLE_REFERENCE -> EXPECT_INLINE_CONDITIONAL;
            default -> EXPECT_CONDITIONAL;
        };
        enterState(nextState, nextExpect);
        yybegin(nextState);
        return true;
    }

    private boolean beginStateInConditionalBlockForClosing() {
        // 3.0.2 close conditional block, if needed
        // if current expect is matched (e.g., EXPECT_CONDITIONAL), close and exit to outer state (e.g., YYINITIAL)
        // if not, just return false instead

        // Example:
        // key = [[PARAM]text]
        //                   ^ here
        //
        // Example flow (simplified):
        // -> enter (YYINITIAL, EXPECT_PROPERTY_KEY)
        // -> enter (IN_PROPERTY_KEY_UNQUOTED, EXPECT_CONDITIONAL_BLOCK)
        // -> enter (IN_PROPERTY_KEY_UNQUOTED, EXPECT_INLINE_CONDITIONAL)
        // -> meet `]`
        // -> exit (IN_PROPERTY_KEY_UNQUOTED, EXPECT_INLINE_CONDITIONAL), (IN_PROPERTY_KEY_UNQUOTED, EXPECT_CONDITIONAL_BLOCK)
        // -> begin IN_PROPERTY_KEY_UNQUOTED

        // Example:
        // key = [[PARAM] text ]
        //                     ^ here
        //
        // Example flow (simplified):
        // -> enter (YYINITIAL, EXPECT_CONDITIONAL_BLOCK)
        // -> enter (YYINITIAL, EXPECT_CONDITIONAL)
        // -> meet `]`
        // -> exit (YYINITIAL, EXPECT_CONDITIONAL), (YYINITIAL, EXPECT_CONDITIONAL_BLOCK)
        // -> begin YYINITIAL (since state stack is empty)

        // Example:
        // key = [[PARAM] text]
        //                    ^ here
        //
        // Example flow (simplified):
        // -> enter (YYINITIAL, EXPECT_CONDITIONAL_BLOCK)
        // -> enter (YYINITIAL, EXPECT_CONDITIONAL)
        // -> enter (YYINITIAL, EXPECT_STRING)
        // -> meet `]`
        // -> exit (YYINITIAL, EXPECT_STRING), (YYINITIAL, EXPECT_CONDITIONAL), (YYINITIAL, EXPECT_CONDITIONAL_BLOCK)
        // -> begin YYINITIAL (since state stack is empty)

        if (stateStack == null || stateStack.isEmpty() || expectStack == null || expectStack.isEmpty()) {
            yybegin(YYINITIAL);
            return false;
        }
        int expect0 = (stateStack.size() >= 2 && expectStack.size() >= 2) ? expectStack.peekInt(0) : -1;
        if (expect0 == EXPECT_INLINE_CONDITIONAL || expect0 == EXPECT_CONDITIONAL) {
            stateStack.popInt();
            expectStack.popInt();
            int state = stateStack.popInt();
            expectStack.popInt();
            yybegin(state);
            return true;
        }
        int expect1 = (stateStack.size() >= 3 && expectStack.size() >= 3) ? expectStack.peekInt(1) : -1;
        if (expect1 == EXPECT_CONDITIONAL) {
            stateStack.popInt();
            expectStack.popInt();
            stateStack.popInt();
            expectStack.popInt();
            int state = stateStack.popInt();
            expectStack.popInt();
            yybegin(state);
            return true;
        }
        return false;
    }

    private boolean beginStateInConditionalBodyToNormalForm() {
        // 3.0.2 manipulate context stack to change conditional block from inline form to normal form, if needed

        // Example:
        // key = [[PARAM] text]
        //               ^ here
        // [[PARAM]key=value]
        //            ^ here
        //
        // Example flow (simplified):
        // -> enter (YYINITIAL, EXPECT_PROPERTY_KEY)
        // -> enter (IN_PROPERTY_KEY_UNQUOTED, EXPECT_CONDITIONAL_BLOCK)
        // -> enter (IN_PROPERTY_KEY_UNQUOTED, EXPECT_INLINE_CONDITIONAL)
        // -> meet whitespace
        // -> replace (YYINITIAL, EXPECT_PROPERTY_KEY), (IN_PROPERTY_KEY_UNQUOTED, EXPECT_CONDITIONAL_BLOCK), (IN_PROPERTY_KEY_UNQUOTED, EXPECT_INLINE_CONDITIONAL)
        //    with (YYINITIAL, EXPECT_CONDITIONAL_BLOCK), (YYINITIAL, EXPECT_CONDITIONAL)
        // -> begin YYINITIAL

        if (stateStack == null || stateStack.isEmpty() || expectStack == null || expectStack.isEmpty()) {
            yybegin(YYINITIAL);
            return false;
        }
        int expectToCheck = expectStack.peekInt(0);
        if (expectToCheck != EXPECT_INLINE_CONDITIONAL) {
            return false;
        }
        int size = stateStack.size();
        for (int i = size - 1; i >= 0; i--) {
            if ((size - i) % 3 == 0) {
                stateStack.removeInt(i);
                expectStack.removeInt(i);
                continue;
            }
            int s = stateStack.getInt(i);
            int e = expectStack.getInt(i);
            if (s == IN_PROPERTY_KEY_UNQUOTED || s == IN_STRING_UNQUOTED || s == IN_SCRIPTED_VARIABLE_NAME || s == IN_SCRIPTED_VARIABLE_REFERENCE) {
                stateStack.set(i, YYINITIAL);
            }
            if (e == EXPECT_INLINE_CONDITIONAL) {
                expectStack.set(i, EXPECT_CONDITIONAL);
            }
        }
        yybegin(YYINITIAL);
        return true;
    }

    private IElementType getFallbackToken() {
        // fallback to corresponding literal/identifier token, based on text, if necessary
        int state = yystate();
        if (state == IN_PROPERTY_KEY_UNQUOTED || state == IN_PROPERTY_KEY_QUOTED) {
            // fallback to normal literal
            return PROPERTY_KEY_TOKEN;
        } else if (state == IN_STRING_UNQUOTED || state == IN_STRING_QUOTED) {
            // fallback to normal literal
            return STRING_TOKEN;
        } else if (state == IN_SCRIPTED_VARIABLE_NAME) {
            // likely unexpected, but there may be other interpolation constructs
            return SCRIPTED_VARIABLE_NAME_TOKEN;
        } else if (state == IN_SCRIPTED_VARIABLE_REFERENCE) {
            // likely unexpected, but there may be other interpolation constructs
            return SCRIPTED_VARIABLE_REFERENCE_TOKEN;
        } else {
            // final fallback (only STRING_TOKEN, never PROPERTY_KEY_TOKEN, atm)
            return STRING_TOKEN;
        }
    }

    private boolean isLeftQuoted() {
        char c = yycharat(0);
        return c == '"';
    }
%}

%public
%class _ParadoxScriptLexer
%implements FlexLexer
%function advance
%type IElementType

%s IN_PROPERTY_VALUE
%s IN_PROPERTY_KEY_UNQUOTED
%s IN_PROPERTY_KEY_QUOTED
%s IN_STRING_UNQUOTED
%s IN_STRING_QUOTED

%s IN_SCRIPTED_VARIABLE_CHECK
%s IN_SCRIPTED_VARIABLE_NAME
%s IN_SCRIPTED_VARIABLE_VALUE
%s IN_SCRIPTED_VARIABLE_REFERENCE_CHECK
%s IN_SCRIPTED_VARIABLE_REFERENCE

%s IN_INLINE_MATH
%s IN_PARAMETER
%s IN_PARAMETER_ARGUMENT
%s IN_CONDITIONAL_BLOCK
%s IN_CONDITIONAL_BLOCK_EXPRESSION

%unicode

Eol = \s*\R\s*
// WhiteSpace = [\s&&[^\r\n]]+
Blank = \s+

Comment = #[^\r\n]*

KeywordYes = yes
KeywordNo = no
KeywordBoolean = {KeywordYes}|{KeywordNo}

OpUnaryPlus = "+"
OpUnaryMinus = "-"
OpNot = "!"

OpEqual = "="
OpNotEqual = "!="|"<>"
OpLe = "<="
OpGe = ">="
OpLt = "<"
OpGt = ">"
OpSafeAssign = "?="
OpSafeCallAssign = "?"{Blank}"="

Quote = \"
NumberUnary = {OpUnaryPlus}|{OpUnaryMinus}
Separator = {OpEqual}
PropertySeparator = {OpEqual}|{OpNotEqual}|{OpLe}|{OpGe}|{OpLt}|{OpGt}|{OpSafeAssign}|{OpSafeCallAssign} // order-sensitive

IntNumberToken = [0-9]+ // leading zero is allowed
FloatNumberToken = [0-9]*\.[0-9]+ // leading zero is allowed

BooleanToken = {KeywordBoolean} // `yes` or `no` (case-sensitive)
IntToken = {NumberUnary}?{IntNumberToken} // with optional unary operator
FloatToken = {NumberUnary}?{FloatNumberToken} // with optional unary operator

LiteralChar = [^#=<>{}\"\s$\[\]] // exclude `$[]` & `@!?` are allowed
LiteralBoundChar = [^#=<>{}\"\s$\[\]@!?] // exclude `$[]` & `@!?` are not allowed
LiteralToken = {LiteralBoundChar}({LiteralChar}*{LiteralBoundChar})? // exclude `$[]` & boundary `@!?` are not allowed

IdentifierChar = [A-Za-z0-9_]
IdentifierLeadChar = [A-Za-z_] // leading number is not allowed
IdentifierToken = {IdentifierLeadChar}{IdentifierChar}* // leading number is not allowed

InterpolationMarkerChar = [$|\[\]!?]
InterpolationBoundChar = [$\[\]]
InterpolationLeadChar = [$\[]

LiteralWildcardChar = {LiteralChar}|{InterpolationMarkerChar}
LiteralWildcardBoundChar = {LiteralBoundChar}|{InterpolationBoundChar}
LiteralWildcardToken = {LiteralWildcardBoundChar}({LiteralWildcardChar}*{LiteralWildcardBoundChar})?

IdentifierWildcardChar = {IdentifierChar}|{InterpolationMarkerChar}
IdentifierWildcardLeadChar = {IdentifierLeadChar}|{InterpolationLeadChar} // leading number is not allowed
IdentifierWildcardToken = {IdentifierWildcardLeadChar}{IdentifierWildcardChar}* // leading number is not allowed

ParameterChar = {IdentifierChar}
ParameterToken = {ParameterChar}+ // leading number is allowed

ArgumentChar = [^#=<>!?{}\\\s$\[\]] // exclude `$[]` & `@` is allowed
ArgumentToken = {ArgumentChar}+ // exclude `$[]` & compatible with leading '@'

PropertyKeyTokenQuoted = ([^\"\\\r\n$\[\]]|\\.)+ // without surrounding quotes & exclude `$[]`
PropertyKeyTokenUnquoted = {LiteralToken} // literal
PropertyKeyWildcardQuoted = ([^\"\\\r\n]|\\.)+ // without surrounding quotes
PropertyKeyWildcardUnquoted = {LiteralWildcardToken} // literal wildcard
PropertyKeyContent = ({Quote}{PropertyKeyWildcardQuoted}?|{PropertyKeyWildcardUnquoted}){Quote}?

StringTokenQuoted = ([^\"\\$\[\]]|\\.)+ // without surrounding quotes & can be multiline & exclude `$[]`
StringTokenUnquoted = {LiteralToken} // literal
StringWildcardQuoted = ([^\"\\]|\\.)+ // without surrounding quotes & can be multiline
StringWildcardUnquoted = {LiteralWildcardToken} // literal wildcard
StringContent = ({Quote}{StringWildcardQuoted}?|{StringWildcardUnquoted}){Quote}?

ScriptedVariableToken = {IdentifierToken} // identifier
ScriptedVariableContent = {IdentifierWildcardToken} // identifier wildcard

// #103 hsv360 (from vic3)
// #399 color types are case-insensitive
// TODO 3.0.2+ better syntax support: split into more specific tokens (distinct from normal keywords/identifiers and normal blocks)
ColorTypeRgb = [rR][gG][bB]
ColorTypeHsv = [hH][sS][vV]
ColorTypeHsv360 = [hH][sS][vV]360
ColorTypeToken = {ColorTypeRgb}|{ColorTypeHsv}|{ColorTypeHsv360}
ColorArgsToken = "{"[\d.\s&&[^\r\n]]*"}" // lenient match
ColorToken = {ColorTypeToken}{Blank}?{ColorArgsToken}

InlineMathToken = [^\r\n#{}\[\]]+ // lenient match

// lazy-scanning inline math expressions (see `ParadoxScript.InlineMath.flex`)

%%

// common rules

<YYINITIAL, IN_PROPERTY_VALUE, IN_SCRIPTED_VARIABLE_VALUE> {
    "{" {
        enterState(YYINITIAL, EXPECT_BLOCK); // enter YYINITIAL directly
        return LEFT_BRACE;
    }
    "}" {
        exitState(EXPECT_BLOCK);
        return RIGHT_BRACE;
    }

    // 3.0.2 comment out since the form should not be distinguished during scanning (but prefer the inline form)
    // "[" / {Blank}?"[" {
    //     enterState(YYINITIAL, EXPECT_CONDITIONAL_BLOCK); // enter YYINITIAL directly
    //     yybegin(IN_INLINE_CONDITIONAL_BLOCK);
    //     return LEFT_BRACKET;
    // }
    // "[" { return getFallbackToken(); }
    "]" {
        if (!beginStateInConditionalBlockForClosing()) return getFallbackToken();
        return RIGHT_BRACKET;
    }

    "@" {
        enterState(YYINITIAL, EXPECT_SCRIPTED_VARIABLE_CHECK); // enter YYINITIAL directly
        yybegin(IN_SCRIPTED_VARIABLE_CHECK);
        return AT;
    }

    {Blank} { return WHITE_SPACE; } // allowed
    {Comment} { return COMMENT; } // allowed
}
<YYINITIAL, IN_PROPERTY_KEY_UNQUOTED, IN_SCRIPTED_VARIABLE_NAME> {
    // 3.0.2 all separators are allowed for properties and scripted variables at syntax level (but may not valid in actual)
    {OpEqual} { beginStateAfterSeparator(); return EQUAL_SIGN; }
    {OpNotEqual} { beginStateAfterSeparator(); return NOT_EQUAL_SIGN; }
    {OpLe} { beginStateAfterSeparator(); return LE_SIGN; }
    {OpGe} { beginStateAfterSeparator(); return GE_SIGN; }
    {OpLt} { beginStateAfterSeparator(); return LT_SIGN; }
    {OpGt} { beginStateAfterSeparator(); return GT_SIGN; }
    // #86 supported in ck3, vic3 and eu5 (preferred format: `k ?= v`)
    {OpSafeAssign} { beginStateAfterSeparator(); return SAFE_ASSIGN_SIGN; }
    // 2.1.10 #331 supported in stellaris 4.4 (preferred format: `k? = v`)
    {OpSafeCallAssign} { beginStateAfterSeparator(); return SAFE_CALL_ASSIGN_SIGN; }
}

// interpolation container rules

<IN_PROPERTY_KEY_UNQUOTED, IN_PROPERTY_KEY_QUOTED, IN_STRING_UNQUOTED, IN_STRING_QUOTED, IN_SCRIPTED_VARIABLE_NAME, IN_SCRIPTED_VARIABLE_REFERENCE> {
    // 3.0.2 `[` may also be the start of other constructs at syntax or semantic level (e.g., localisation commands), if not nested
    "[" / {Blank}?"[" {
        enterState(yystate(), EXPECT_CONDITIONAL_BLOCK);
        yybegin(IN_CONDITIONAL_BLOCK);
        return LEFT_BRACKET;
    }
    "[" { return getFallbackToken(); }
    "]" {
        if (!beginStateInConditionalBlockForClosing()) return getFallbackToken();
        return RIGHT_BRACKET;
    }

    "$" {
        enterState(yystate(), EXPECT_PARAMETER);
        yybegin(IN_PARAMETER);
        return PARAMETER_START;
    }
}

// property and expression rules

<YYINITIAL, IN_PROPERTY_VALUE, IN_SCRIPTED_VARIABLE_VALUE> {
    "@["|"@\\[" { // `@[` or `@\[`
        enterState(YYINITIAL, EXPECT_INLINE_MATH); // enter YYINITIAL directly
        yybegin(IN_INLINE_MATH);
        return INLINE_MATH_START;
    }
}
<IN_PROPERTY_VALUE> {
    "@" {
        enterState(YYINITIAL, EXPECT_SCRIPTED_VARIABLE_CHECK); // enter YYINITIAL directly
        yybegin(IN_SCRIPTED_VARIABLE_REFERENCE_CHECK);
        return AT;
    }
}
<YYINITIAL, IN_PROPERTY_VALUE, IN_SCRIPTED_VARIABLE_VALUE> {
    {BooleanToken} { beginStateAfterValue(); return BOOLEAN_TOKEN; }
    {IntToken} { beginStateAfterValue(); return INT_TOKEN; }
    {FloatToken} { beginStateAfterValue(); return FLOAT_TOKEN; }
    {ColorToken} { beginStateAfterValue(); return COLOR_TOKEN; }
}
<YYINITIAL, IN_PROPERTY_VALUE, IN_SCRIPTED_VARIABLE_VALUE> {
    {PropertyKeyContent} / {Blank}?{PropertySeparator} {
        enterState(YYINITIAL, EXPECT_PROPERTY_KEY); // enter YYINITIAL directly
        if (isLeftQuoted()) {
            yypushback(yylength() - 1);
            yybegin(IN_PROPERTY_KEY_QUOTED);
            return PROPERTY_KEY_TOKEN;
        } else {
            yypushback(yylength());
            yybegin(IN_PROPERTY_KEY_UNQUOTED);
        }
    }
    {StringContent} {
        enterState(YYINITIAL, EXPECT_STRING); // enter YYINITIAL directly
        if (isLeftQuoted()) {
            yypushback(yylength() - 1);
            yybegin(IN_STRING_QUOTED);
            return STRING_TOKEN;
        } else {
            yypushback(yylength());
            yybegin(IN_STRING_UNQUOTED);
        }
    }
}
<IN_PROPERTY_VALUE> {
    {Blank} { return WHITE_SPACE; } // allowed
    {Comment} { exitStateForRecovery(); return COMMENT; } // recovery
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

<IN_PROPERTY_KEY_UNQUOTED> {
    {PropertyKeyTokenUnquoted} { return PROPERTY_KEY_TOKEN; }
    {Quote} { exitStateForRecovery(); return PROPERTY_KEY_TOKEN; }
    {Blank} { exitStateForRecovery(); return WHITE_SPACE; }
    {Comment} { exitStateForRecovery(); return COMMENT; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; }
}
<IN_PROPERTY_KEY_QUOTED> {
    {Eol} { exitStateForRecovery(); return WHITE_SPACE; } // break and recovery
    {PropertyKeyTokenQuoted} { return PROPERTY_KEY_TOKEN; }
    {Quote} { exitStateForRecovery(); return PROPERTY_KEY_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}
<IN_STRING_UNQUOTED> {
    {StringTokenUnquoted} { return STRING_TOKEN; }
    {Quote} { exitStateForRecovery(); return STRING_TOKEN; }
    {Blank} { exitStateForRecovery(); return WHITE_SPACE; }
    {Comment} { exitStateForRecovery(); return COMMENT; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; }
}
<IN_STRING_QUOTED> {
    // quoted multiline string is allowed (which will break futher scanning and parsing while closing quote is missing)
    // {EOL} { exitStateForRecovery(); return WHITE_SPACE; }
    {StringTokenQuoted} { return STRING_TOKEN; }
    {Quote} { exitStateForRecovery(); return STRING_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

// scripted variable rules

<IN_SCRIPTED_VARIABLE_CHECK> {
    {ScriptedVariableContent} / {Blank}?{Separator} {
        exitState(EXPECT_SCRIPTED_VARIABLE_CHECK); // exit state if neccesary (or need double-exit later)
        enterState(yystate(), EXPECT_SCRIPTED_VARIABLE_NAME);
        yypushback(yylength());
        yybegin(IN_SCRIPTED_VARIABLE_NAME);
    }
    {ScriptedVariableContent} {
        exitState(EXPECT_SCRIPTED_VARIABLE_CHECK); // exit state if neccesary (or need double-exit later)
        enterState(yystate(), EXPECT_SCRIPTED_VARIABLE_REFERENCE);
        yypushback(yylength());
        yybegin(IN_SCRIPTED_VARIABLE_REFERENCE);
    }
    [^] { exitStateForRecovery(); yypushback(yylength()); } // recovery (always, to be compatible with, e.g., `@=var` form)
}
<IN_SCRIPTED_VARIABLE_NAME> {
    {ScriptedVariableToken} { return SCRIPTED_VARIABLE_NAME_TOKEN; }
    {Blank} { exitStateForRecovery(); return WHITE_SPACE; } // recovery
    {Comment} { exitStateForRecovery(); return COMMENT; } // recovery
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}
<IN_SCRIPTED_VARIABLE_VALUE> {
    {Blank} { return WHITE_SPACE; } // allowed
    {Comment} { exitStateForRecovery(); return COMMENT; } // recovery
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

<IN_SCRIPTED_VARIABLE_REFERENCE_CHECK> {
    {ScriptedVariableContent} {
        exitState(EXPECT_SCRIPTED_VARIABLE_CHECK); // exit state if neccesary (or need double-exit later)
        enterState(yystate(), EXPECT_SCRIPTED_VARIABLE_REFERENCE);
        yypushback(yylength());
        yybegin(IN_SCRIPTED_VARIABLE_REFERENCE);
    }
    [^] { exitStateForRecovery(); yypushback(yylength()); } // recovery (always, to be compatible with, e.g., `@@` form)
}
<IN_SCRIPTED_VARIABLE_REFERENCE> {
    {ScriptedVariableToken} { return SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
    {Blank} { exitStateForRecovery(); return WHITE_SPACE; } // recovery
    {Comment} { exitStateForRecovery(); return COMMENT; } // recovery
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

// inline math rules

<IN_INLINE_MATH> {
    "]" {
        exitState(EXPECT_INLINE_MATH);
        return INLINE_MATH_END;
    }

    {InlineMathToken} { return INLINE_MATH_TOKEN; }
    {Blank} { return WHITE_SPACE; } // allowed
    // {Comment} { return COMMENT; } // not allowed (heuristic)
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
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
    {ParameterToken} { return PARAMETER_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}
<IN_PARAMETER_ARGUMENT> {
    {ArgumentToken} { return ARGUMENT_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

// conditional block rules

// 3.0.2 the form should not be distinguished during scanning (but prefer the inline form)
<IN_CONDITIONAL_BLOCK> {
    "[" { // nested
        yybegin(IN_CONDITIONAL_BLOCK_EXPRESSION);
        return NESTED_LEFT_BRACKET; }
    "]" {
        exitState(EXPECT_CONDITIONAL_BLOCK);
        return RIGHT_BRACKET;
    }

    {Blank} { return WHITE_SPACE; } // allowed
    {Comment} { return COMMENT; } // allowed (heuristic)
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}
<IN_CONDITIONAL_BLOCK_EXPRESSION> {
    "]" { // nested
        beginStateInConditionalBody();
        return NESTED_RIGHT_BRACKET;
    }

    {OpNot} { return NOT_SIGN; }
    {ParameterToken} { return CONDITION_PARAMETER_TOKEN; }
    {Blank} { return WHITE_SPACE; } // allowed
    {Comment} { return COMMENT; } // allowed (heuristic)
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

[^] { return BAD_CHARACTER; }
