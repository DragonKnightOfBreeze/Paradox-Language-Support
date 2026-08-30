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

LiteralChar = [^#=<>{}\"\s$\[\]] // `@!?` are allowed
LiteralBoundChar = [^#=<>{}\"\s$\[\]@!?] // `@!?` are not allowed
LiteralToken = {LiteralBoundChar}({LiteralChar}*{LiteralBoundChar})? // boundary `@!?` are not allowed

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

ArgumentChar = [^#=<>!?{}\\\s$\[\]] // `@` is allowed
ArgumentToken = {ArgumentChar}+ // compatible with leading '@'

PropertyKeyTokenQuoted = ([^\"\\\r\n$\[]|\\.)+ // without surrounding quotes & exclude `$[`
PropertyKeyTokenUnquoted = {LiteralToken} // literal
PropertyKeyWildcardQuoted = ([^\"\\\r\n]|\\.)+ // without surrounding quotes
PropertyKeyWildcardUnquoted = {LiteralWildcardToken} // literal wildcard
PropertyKeyContent = ({Quote}{PropertyKeyWildcardQuoted}|{PropertyKeyWildcardUnquoted}){Quote}?

StringTokenQuoted = ([^\"\\$\[]|\\.)+ // without surrounding quotes & can be multiline & exclude `$[`
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

    {Blank} { return WHITE_SPACE; }
    {Comment} { return COMMENT; }
}

// separator rules

// also for `IN_PROPERTY_KEY_UNQUOTED` and `IN_SCRIPTED_VARIABLE_NAME` (be compatible with no arouding whitespaces)
<YYINITIAL, IN_CONDITIONAL_BLOCK_BODY, IN_PROPERTY, IN_PROPERTY_KEY_UNQUOTED, IN_SCRIPTED_VARIABLE, IN_SCRIPTED_VARIABLE_NAME> {
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

<IN_PROPERTY_KEY_UNQUOTED, IN_PROPERTY_KEY_QUOTED, IN_STRING_UNQUOTED, IN_STRING_QUOTED, IN_SCRIPTED_VARIABLE_NAME, IN_SCRIPTED_VARIABLE_REFERENCE, IN_INLINE_CONDITIONAL_BLOCK_BODY> {
    // 3.0.2 may be a command start marker of some injected/embedded localisation text, need lookahead
    // use trailing context (high priority than normal form)
    "[" / {Blank}?"[" {
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
    {BooleanToken} { exitState(); return BOOLEAN_TOKEN; }
    {IntToken} { exitState(); return INT_TOKEN; }
    {FloatToken} { exitState(); return FLOAT_TOKEN; }
    {ColorToken} { exitState(); return COLOR_TOKEN; }
}
<YYINITIAL, IN_CONDITIONAL_BLOCK_BODY, IN_PROPERTY_VALUE, IN_SCRIPTED_VARIABLE_VALUE> {
    // use trailing context (high priority than normal form)
    {PropertyKeyContent} / {Blank}?{PropertySeparator} {
        yybegin(IN_PROPERTY);
        enterState(yystate(), EXPECT_PROPERTY_KEY);
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
        if (isLeftQuoted()) {
            yypushback(yylength() - 1);
            yybegin(IN_STRING_QUOTED);
            enterState(yystate(), EXPECT_STRING);
            return STRING_TOKEN;
        } else {
            yypushback(yylength());
            yybegin(IN_STRING_UNQUOTED);
            enterState(yystate(), EXPECT_STRING);
        }
    }
}
<IN_PROPERTY> {
    {Blank} { return WHITE_SPACE; } // keep
    {Comment} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_PROPERTY_VALUE> {
    {Blank} { return WHITE_SPACE; } // keep
    {Comment} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}

<IN_PROPERTY_KEY_UNQUOTED> {
    "["|"]" { return PROPERTY_KEY_TOKEN; } // fallback
    {PropertyKeyTokenUnquoted} { return PROPERTY_KEY_TOKEN; }
    {Blank} { exitState(); return WHITE_SPACE; } // keep
    {Comment} { exitState(); return COMMENT; } // keep
    {Quote} { exitState(); return PROPERTY_KEY_TOKEN; }
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_PROPERTY_KEY_QUOTED> {
    {Eol} { exitState(); return WHITE_SPACE; }
    "["|"]" { return PROPERTY_KEY_TOKEN; } // fallback
    {PropertyKeyTokenQuoted} { return PROPERTY_KEY_TOKEN; }
    {Quote} { exitState(); return PROPERTY_KEY_TOKEN; }
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_STRING_UNQUOTED> {
    "["|"]" { return STRING_TOKEN; } // fallback
    {StringTokenUnquoted} { return STRING_TOKEN; }
    {Blank} { exitStateForValue(); return WHITE_SPACE; } // keep
    {Comment} { exitStateForValue(); return COMMENT; } // keep
    {Quote} { exitStateForValue(); return STRING_TOKEN; }
    [^] { if (!exitStateAtBadCharacterForValue()) return BAD_CHARACTER; } // recovery
}
<IN_STRING_QUOTED> {
    // quoted multiline string is allowed (which will break futher scanning and parsing while closing quote is missing)
    // {EOL} { exitState(); return WHITE_SPACE; }
    "["|"]" { return STRING_TOKEN; } // fallback
    {StringTokenQuoted} { return STRING_TOKEN; }
    {Quote} { exitStateForValue(); return STRING_TOKEN; }
    [^] { if (!exitStateAtBadCharacterForValue()) return BAD_CHARACTER; } // recovery
}

// scripted variable rules

<CHECK_SCRIPTED_VARIABLE> {
    // use trailing context (high priority than normal form)
    {ScriptedVariableContent} / {Blank}?{Separator} {
        yybegin(IN_SCRIPTED_VARIABLE);
        enterState(yystate(), EXPECT_SCRIPTED_VARIABLE_NAME);
        yypushback(yylength());
        yybegin(IN_SCRIPTED_VARIABLE_NAME);
    }
    {ScriptedVariableContent} {
        yypushback(yylength());
        yybegin(IN_SCRIPTED_VARIABLE_REFERENCE);
        enterState(yystate(), EXPECT_SCRIPTED_VARIABLE_REFERENCE);
    }
}
<CHECK_SCRIPTED_VARIABLE_REFERENCE> {
    {ScriptedVariableContent} {
        yypushback(yylength());
        yybegin(IN_SCRIPTED_VARIABLE_REFERENCE);
        enterState(yystate(), EXPECT_SCRIPTED_VARIABLE_REFERENCE);
    }
}
<IN_SCRIPTED_VARIABLE> {
    {Blank} { return WHITE_SPACE; }
    {Comment} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_SCRIPTED_VARIABLE_VALUE> {
    {Blank} { return WHITE_SPACE; }
    {Comment} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_SCRIPTED_VARIABLE_NAME> {
    {ScriptedVariableToken} { return SCRIPTED_VARIABLE_NAME_TOKEN; }
    {Blank} { exitState(); return WHITE_SPACE; } // keep
    {Comment} { exitState(); return COMMENT; } // keep
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_SCRIPTED_VARIABLE_REFERENCE> {
    {ScriptedVariableToken} { return SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
    {Blank} { exitStateForValue(); return WHITE_SPACE; } // keep
    {Comment} { exitStateForValue(); return COMMENT; } // keep
    [^] { if (!exitStateAtBadCharacterForValue()) return BAD_CHARACTER; } // recovery
}

// inline math rules

<IN_INLINE_MATH> {
    "]" {
        exitState(EXPECT_INLINE_MATH);
        return INLINE_MATH_END;
    }

    {InlineMathToken} { return INLINE_MATH_TOKEN; }
    {Blank} { return WHITE_SPACE; } // keep
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
    {ParameterToken} { return PARAMETER_TOKEN; }
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_PARAMETER_ARGUMENT> {
    {ArgumentToken} { return ARGUMENT_TOKEN; }
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

    {OpNot} { return NOT_SIGN; }
    {ConditionParameterToken} { return CONDITION_PARAMETER_TOKEN; }
    {Blank} { return WHITE_SPACE; }
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

    {OpNot} { return NOT_SIGN; }
    {ConditionParameterToken} { return CONDITION_PARAMETER_TOKEN; }
    // {BLANK} { return WHITE_SPACE; } // should not be allowed
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_INLINE_CONDITIONAL_BLOCK_BODY> {
    {ArgumentToken} { exitState();  return ARGUMENT_TOKEN; } // TODO 3.0.2 enter correct state?
    // {BLANK} { return WHITE_SPACE; } // should not be allowed
    [^] { if (!exitStateAtBadCharacter()) return BAD_CHARACTER; } // recovery
}

[^] { return BAD_CHARACTER; }
