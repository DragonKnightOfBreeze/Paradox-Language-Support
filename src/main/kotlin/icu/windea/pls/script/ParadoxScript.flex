package icu.windea.pls.script.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.model.ParadoxGameType;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;

// Lexer for Paradox Script.

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
    private static final int EXPECT_NESTED = 11;
    private static final int EXPECT_NESTED_PROPERTY_KEY = 12;
    private static final int EXPECT_NESTED_STRING = 13;

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
        // enter state
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
        // used for recovery
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

    private void exitStateForRecovery() {
        // used for recovery
        if (stateStack == null || stateStack.isEmpty()) {
            yybegin(YYINITIAL);
            return;
        }
        if (expectStack == null || expectStack.isEmpty()) {
            yybegin(YYINITIAL);
            return;
        }
        if (exitStateForRecoveryInConditionalBlockBody()) return;
        expectStack.popInt();
        int currentState = stateStack.popInt();
        yybegin(currentState);
    }

    private boolean exitStateForRecoveryInConditionalBlockBody() {
        // 3.0.2 manipulate context stack to change conditional block from inline form to normal form

        // Example flow:
        // YYINITIAL
        // -> enter YYINITIAL, EXPECT_PROPERTY_KEY -> begin IN_PROPERTY_KEY_UNQUOTED
        // -> enter IN_PROPERTY_KEY_UNQUOTED, EXPECT_CONDITIONAL_BLOCK -> begin IN_CONDITIONAL_BLOCK
        // -> begin IN_CONDITIONAL_BLOCK_EXPRESSION
        // -> enter IN_PROPERTY_KEY_UNQUOTED, EXPECT_PROPERTY_KEY_NESTED -> begin IN_PROPERTY_KEY_UNQUOTED
        // -> meet whitespace
        // -> exit -> exit -> enter YYINITIAL, EXPECT_CONDITIONAL_BLOCK -> enter YYINITIAL, EXPECT_NESTED
        // -> begin YYINITIAL

        int currentExpect = expectStack.peekInt(0);
        if (currentExpect != EXPECT_NESTED_PROPERTY_KEY && currentExpect != EXPECT_NESTED_STRING) return false;
        int size = stateStack.size();
        for (int i = size - 1; i >= 0; i--) {
            if ((size - i) % 3 == 0) {
                stateStack.removeInt(i);
                expectStack.removeInt(i);
                continue;
            }
            int state = stateStack.getInt(i);
            int expect = expectStack.getInt(i);
            if (state == IN_PROPERTY_KEY_UNQUOTED || state == IN_STRING_UNQUOTED) {
                stateStack.set(i, YYINITIAL);
            }
            if (expect == EXPECT_NESTED_PROPERTY_KEY || expect == EXPECT_NESTED_STRING) {
                expectStack.set(i, EXPECT_NESTED);
            }
        }
        yybegin(YYINITIAL);
        return true;
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
        int state = yystate();
        if (state == IN_SCRIPTED_VARIABLE_NAME) {
            exitState(EXPECT_SCRIPTED_VARIABLE_NAME); // exist state if necessary
            yybegin(IN_SCRIPTED_VARIABLE_VALUE);
        } else {
            exitState(EXPECT_PROPERTY_KEY); // exist state if necessary
            yybegin(IN_PROPERTY_VALUE);
        }
    }

    private void beginStateInConditionalBlockBody() {
        // 3.0.2 recovery and inherit context from context stack
        // peek state X (i = 0) and then enter state X and the corresponding expect
        // where X may be YYINITIAL, IN_PROPERTY_KEY_UNQUOTED, IN_PROPERTY_KEY_QUOTED, etc.
        // which represents that current context is in some member container or interpolation container

        // Example flow:
        // YYINITIAL
        // -> enter YYINITIAL, EXPECT_PROPERTY_KEY -> begin IN_PROPERTY_KEY_UNQUOTED
        // -> enter IN_PROPERTY_KEY_UNQUOTED, EXPECT_CONDITIONAL_BLOCK -> begin IN_CONDITIONAL_BLOCK
        // -> begin IN_CONDITIONAL_BLOCK_EXPRESSION
        // -> peek state X (i = 0)
        // -> enter IN_PROPERTY_KEY_UNQUOTED, EXPECT_PROPERTY_KEY_NESTED -> begin IN_PROPERTY_KEY_UNQUOTED

        if (stateStack == null || stateStack.isEmpty()) {
            yybegin(YYINITIAL);
            return;
        }
        int nextState = stateStack.peekInt(0);
        int nextExpect = switch (nextState) {
            case IN_PROPERTY_KEY_UNQUOTED, IN_PROPERTY_KEY_QUOTED -> EXPECT_NESTED_PROPERTY_KEY;
            case IN_STRING_UNQUOTED, IN_STRING_QUOTED -> EXPECT_NESTED_STRING;
            default -> EXPECT_NESTED;
        };
        enterState(nextState, nextExpect);
        yybegin(nextState);
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
InterpolationLeadChar = [$\[]

LiteralWildcardChar = {LiteralChar}|{InterpolationMarkerChar}
LiteralWildcardBoundChar = {LiteralBoundChar}|{InterpolationMarkerChar}
LiteralWildcardToken = {LiteralWildcardBoundChar}({LiteralWildcardChar}*{LiteralWildcardBoundChar})?

IdentifierWildcardChar = {IdentifierChar}|{InterpolationMarkerChar}
IdentifierWildcardLeadChar = {IdentifierLeadChar}|{InterpolationMarkerChar} // leading number is not allowed
IdentifierWildcardToken = {IdentifierWildcardLeadChar}{IdentifierWildcardChar}* // leading number is not allowed

ParameterToken = {IdentifierToken} // identifier

ConditionParameterToken = {IdentifierToken} // identifier

ArgumentChar = [^#=<>!?{}\\\s$\[\]] // exclude `$[]` & `@` is allowed
ArgumentToken = {ArgumentChar}+ // exclude `$[]` & compatible with leading '@'

PropertyKeyTokenQuoted = ([^\"\\\r\n$\[\]]|\\.)+ // without surrounding quotes & exclude `$[]`
PropertyKeyTokenUnquoted = {LiteralToken} // literal
PropertyKeyWildcardQuoted = ([^\"\\\r\n]|\\.)+ // without surrounding quotes
PropertyKeyWildcardUnquoted = {LiteralWildcardToken} // literal wildcard
PropertyKeyContent = ({Quote}{PropertyKeyWildcardQuoted}|{PropertyKeyWildcardUnquoted}){Quote}?

StringTokenQuoted = ([^\"\\$\[\]]|\\.)+ // without surrounding quotes & can be multiline & exclude `$[]`
StringTokenUnquoted = {LiteralToken} // literal
StringWildcardQuoted = ([^\"\\]|\\.)+ // without surrounding quotes & can be multiline
StringWildcardUnquoted = {LiteralWildcardToken} // literal wildcard
StringContent = ({Quote}{StringWildcardQuoted}|{StringWildcardUnquoted}){Quote}?

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

<YYINITIAL> {
    "{" {
        enterState(YYINITIAL, EXPECT_BLOCK); // enter YYINITIAL directly
        return LEFT_BRACE;
    }
    // 3.0.2 comment out since the form should not be distinguished during scanning (but prefer the inline form)
    // // 3.0.2 may be a command start marker of some injected/embedded localisation text, need lookahead
    // // use trailing context (higher priority)
    // "[" / {Blank}?"[" {
    //     enterState(YYINITIAL, EXPECT_CONDITIONAL_BLOCK); // enter YYINITIAL directly
    //     yybegin(IN_INLINE_CONDITIONAL_BLOCK);
    //     return LEFT_BRACKET;
    // }
    "@" {
        enterState(YYINITIAL, EXPECT_SCRIPTED_VARIABLE_CHECK); // enter YYINITIAL directly
        yybegin(IN_SCRIPTED_VARIABLE_CHECK);
        return AT;
    }
    "}" {
        exitState(EXPECT_BLOCK);
        return RIGHT_BRACE;
    }
    "]" {
        // 3.0.2 need double-exit here (X, Y -> X, EXPECT_CONDITIONAL_BLOCK) // TODO
        exitState();
        exitState(EXPECT_CONDITIONAL_BLOCK);
        return RIGHT_BRACKET;
    }

    {Blank} { return WHITE_SPACE; } // allowed
    {Comment} { return COMMENT; }
}
<YYINITIAL, IN_PROPERTY_KEY_UNQUOTED, IN_SCRIPTED_VARIABLE_NAME> {
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
    // 3.0.2 may be a command start marker of some injected/embedded localisation text, need lookahead
    // use trailing context (higher priority)
    "[" / {Blank}?"[" {
        enterState(yystate(), EXPECT_CONDITIONAL_BLOCK);
        yybegin(IN_CONDITIONAL_BLOCK);
        return LEFT_BRACKET;
    }
    "$" {
        enterState(yystate(), EXPECT_PARAMETER);
        yybegin(IN_PARAMETER);
        return PARAMETER_START;
    }
    "]" {
        // 3.0.2 need double-exit here (X, Y -> X, EXPECT_CONDITIONAL_BLOCK) // TODO
        exitState();
        exitState(EXPECT_CONDITIONAL_BLOCK);
        return RIGHT_BRACKET;
    }
}

// property and expression rules

<YYINITIAL, IN_PROPERTY_VALUE, IN_SCRIPTED_VARIABLE_VALUE> {
    "@["|"@\\[" {
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
    {BooleanToken} { yybegin(YYINITIAL); return BOOLEAN_TOKEN; }
    {IntToken} { yybegin(YYINITIAL); return INT_TOKEN; }
    {FloatToken} { yybegin(YYINITIAL); return FLOAT_TOKEN; }
    {ColorToken} { yybegin(YYINITIAL); return COLOR_TOKEN; }
}
<YYINITIAL, IN_PROPERTY_VALUE, IN_SCRIPTED_VARIABLE_VALUE> {
    // use trailing context (higher priority)
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
    "["|"]" { return PROPERTY_KEY_TOKEN; } // fallback
    {PropertyKeyTokenUnquoted} { return PROPERTY_KEY_TOKEN; }
    {Quote} { exitStateForRecovery(); return PROPERTY_KEY_TOKEN; }
    {Blank} { exitStateForRecovery(); return WHITE_SPACE; } // recovery TODO
    {Comment} { exitStateForRecovery(); return COMMENT; } // recovery TODO
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery TODO
}
<IN_PROPERTY_KEY_QUOTED> {
    {Eol} { exitStateForRecovery(); return WHITE_SPACE; } // break and recovery
    "["|"]" { return PROPERTY_KEY_TOKEN; } // fallback
    {PropertyKeyTokenQuoted} { return PROPERTY_KEY_TOKEN; }
    {Quote} { exitStateForRecovery(); return PROPERTY_KEY_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}
<IN_STRING_UNQUOTED> {
    "["|"]" { return STRING_TOKEN; } // fallback
    {StringTokenUnquoted} { return STRING_TOKEN; }
    {Quote} { exitStateForRecovery(); return STRING_TOKEN; }
    {Blank} { exitStateForRecovery(); return WHITE_SPACE; } // recovery TODO
    {Comment} { exitStateForRecovery(); return COMMENT; } // recovery TODO
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery TODO
}
<IN_STRING_QUOTED> {
    // quoted multiline string is allowed (which will break futher scanning and parsing while closing quote is missing)
    // {EOL} { exitStateForRecovery(); return WHITE_SPACE; }
    "["|"]" { return STRING_TOKEN; } // fallback
    {StringTokenQuoted} { return STRING_TOKEN; }
    {Quote} { exitStateForRecovery(); return STRING_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

// scripted variable rules

<IN_SCRIPTED_VARIABLE_CHECK> {
    // use trailing context (higher priority)
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
    "[" {
        yybegin(IN_CONDITIONAL_BLOCK_EXPRESSION);
        return NESTED_LEFT_BRACKET; }
    "]" {
        exitState(EXPECT_CONDITIONAL_BLOCK);
        return RIGHT_BRACKET;
    }

    {Blank} { return WHITE_SPACE; } // allowed
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}
<IN_CONDITIONAL_BLOCK_EXPRESSION> {
    "]" {
        beginStateInConditionalBlockBody();
        return NESTED_RIGHT_BRACKET;
    }

    {OpNot} { return NOT_SIGN; }
    {ConditionParameterToken} { return CONDITION_PARAMETER_TOKEN; }
    {Blank} { return WHITE_SPACE; } // allowed
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

[^] { return BAD_CHARACTER; }
