// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.


// Lexer for localisation text of Paradox Localisation.
// Notes:
// - Use trailing context for high-priority rules.
// - Use `stateStack` and `expectStack` to manage lexer-level states.
// - Use `ParadoxSyntaxConstraint` to check whether specific syntax is supported in current game type.

package icu.windea.pls.localisation.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.model.ParadoxGameType;
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;

%%

%{
    private ParadoxGameType gameType; // NOTE 3.0.2 used by constraint-based checks

    // stack for context states (states that need to fallback when exit some constructs)
    private IntArrayList stateStack = null;
    // stack for expected construct types (e.g., EXPECT_COLORFUL_TEXT)
    private IntArrayList expectStack = null;

    private static final int EXPECT_COLORFUL_TEXT = 1;
    private static final int EXPECT_PARAMETER = 2;
    private static final int EXPECT_ICON = 3;
    private static final int EXPECT_COMMAND = 4;
    private static final int EXPECT_TEXT_ICON = 5;
    private static final int EXPECT_TEXT_FORMAT = 6;
    private static final int EXPECT_STRING_VARIANT_TAG_PART = 7;
    private static final int EXPECT_TAG_SENSITIVE_TEXT = 8;
    private static final int EXPECT_TAGGED_PARAMETER = 9;
    private static final int EXPECT_TAG_PART = 10;
    private static final int EXPECT_CONTEXT_TAG_PART = 11;

    public _ParadoxLocalisationTextLexer() {
        this((java.io.Reader)null);
        this.gameType = null;
    }

    public _ParadoxLocalisationTextLexer(ParadoxGameType gameType) {
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
        // heuristic: always recovery atm
        return true;
    }

    private IElementType getFallbackToken() {
        // fallback to `TEXT_TOKEN`, if necessary
        return TEXT_TOKEN;
    }

    private boolean isExactWord(char c) {
        return c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }

    private boolean isColorfulText() {
        if (yylength() <= 1) return false;
        char c = yycharat(1);
        return isExactWord(c); // exact word after prefix
    }
%}

%public
%class _ParadoxLocalisationTextLexer
%implements FlexLexer
%function advance
%type IElementType

%s IN_COLORFUL_TEXT_CHECK
%s IN_COLOR_ID
%s IN_COLORFUL_TEXT

%s IN_PARAMETER
%s IN_PARAMETER_ARGUMENT
%s IN_SCRIPTED_VARIABLE_REFERENCE

%s IN_COMMAND
%s IN_COMMAND_TEXT
%s IN_COMMAND_ARGUMENT

%s IN_CONCEPT_NAME
%s IN_CONCEPT_AFTER_COMMA
%s IN_CONCEPT_TEXT

%s IN_ICON
%s IN_ICON_ARGUMENT

%s IN_TEXT_ICON

%s IN_TEXT_FORMAT
%s IN_TEXT_FORMAT_TEXT

%s IN_STRING_VARIANT
%s IN_STRING_VARIANT_TAG_PART

%s IN_TAG_SENSITIVE_TEXT
%s IN_TAGGED_PARAMETER

%s IN_TAG_PART
%s IN_CONTEXT_TAG_PART

%unicode

Blank = \s+

IdentifierChar = [A-Za-z0-9_]
IdentifierLeadChar = [A-Za-z_] // leading number is not allowed
IdentifierToken = {IdentifierLeadChar}{IdentifierChar}* // leading number is not allowed

InterpolationMarkerChar = [$|\[\]]
InterpolationLeadChar = [$\[]

ScriptedVariableToken = {IdentifierToken} // identifier

ParameterChar = {IdentifierChar}|[.\-'] // `.-'` is allowed additionally
ParameterToken = {ParameterChar}+ // leading number is allowed & `.-'` is allowed additionally

ArgumentChar = [^$§£\[\]\\\s] // `|` is allowed?
ArgumentToken = {ArgumentChar}+

ColorfulTextCheck = §.?
ColorToken = {IdentifierChar} // identifier char

CommandTextChar = [^\[\]|&:\r\n] // `[]` within single quotes are not allowed?
CommandTextBoundChar = [^\[\]|&:\s] // `[]` within single quotes are not allowed?
CommandTextToken = {CommandTextBoundChar}({CommandTextChar}*{CommandTextBoundChar})? // inner whitespaces are allowed

ConceptNameChar = {IdentifierChar}|[:] // `:` is allowed additionally
ConceptNameToken = {ConceptNameChar}+

IconChar = {IdentifierChar}|[\-/\\] // `-/\` is allowed additionally
IconToken = {IconChar}+ // leading number is allowed
IconWildcardLeadChar = {IconChar}|{InterpolationLeadChar}

TextIconChar = {IdentifierChar} // identifier
TextIconToken = {TextIconChar}+ // leading number is allowed
TextIconWildcardLeadChar = {TextIconChar}|{InterpolationLeadChar}

// `italic;color:green` form is allowed
TextFormatChar = {IdentifierChar}|[:'] // `:'` is allowed additionally
TextFormatToken = {TextFormatChar}+ // leading number is allowed
TextFormatWildcardLeadChar = {TextFormatChar}|{InterpolationLeadChar}

TextToken = ([^\$\[\]§£#@|&]|\\.)+
StringVariantTextToken = ([^:\$\[\]§£#@|&]|\\.)+ // exclude `:` additionally
TagSensitiveTextToken = ([^<\$\[\]§£#@|&]|\\.)+ // exclude `<` additionally

TagChar = {IdentifierChar} // identifier
TagToken = {TagChar}+ // leading number is allowed

ContextTagChar = {IdentifierChar} // identifier
ContextTagToken = {ContextTagChar}+ // leading number is allowed

%%

// common rules

<YYINITIAL, IN_COLORFUL_TEXT, IN_CONCEPT_TEXT, IN_TEXT_FORMAT_TEXT, IN_STRING_VARIANT, IN_TAG_SENSITIVE_TEXT> {
    "§" {
        enterState(yystate(), EXPECT_COLORFUL_TEXT);
        yypushback(yylength());
        yybegin(IN_COLORFUL_TEXT_CHECK);
    }
    "§!" { // dangling form is allowed here at syntax level
        exitState(EXPECT_COLORFUL_TEXT);
        return COLORFUL_TEXT_END;
    }

    "$" / \S*"$" { // heuristic: require no blank in `$...$`
        enterState(yystate(), EXPECT_PARAMETER);
        yybegin(IN_PARAMETER);
        return PARAMETER_START;
    }
    "$" { return getFallbackToken(); }

    "[[" { return getFallbackToken(); }
    "[" { // heuristic: require `[` is not escaped (double left brackets)
        enterState(yystate(), EXPECT_COMMAND);
        yybegin(IN_COMMAND);
        return LEFT_BRACKET;
    }
    "]" {
        int state = yystate();
        if (state == IN_CONCEPT_TEXT) {
            exitState(EXPECT_COMMAND);
            return RIGHT_BRACKET;
        } else if (state == IN_TAG_SENSITIVE_TEXT) {
            exitState(EXPECT_COMMAND);
            return RIGHT_BRACKET;
        }
        return getFallbackToken();
    }

    "£" / {IconWildcardLeadChar} { // require next character be a valid identifier character
        enterState(yystate(), EXPECT_ICON);
        yybegin(IN_ICON);
        return ICON_START;
    }
    "£" { return getFallbackToken(); }

    "@" / {TextIconWildcardLeadChar} { // require next character be a valid identifier character
        if (!ParadoxSyntaxConstraint.LocalisationTextIcon.testTarget(this)) return getFallbackToken();
        enterState(yystate(), EXPECT_TEXT_ICON);
        yybegin(IN_TEXT_ICON);
        return TEXT_ICON_START;
    }
    "@" { return getFallbackToken(); }

    "#" / {TextFormatWildcardLeadChar} { // require next character be a valid identifier character
        if (!ParadoxSyntaxConstraint.LocalisationTextFormat.testTarget(this)) return getFallbackToken();
        enterState(yystate(), EXPECT_TEXT_FORMAT);
        yybegin(IN_TEXT_FORMAT);
        return TEXT_FORMAT_START;
    }
    "#" { return getFallbackToken(); }
    "#!" { // dangling form is allowed here at syntax level
        if (!ParadoxSyntaxConstraint.LocalisationTextFormat.testTarget(this)) return getFallbackToken();
        exitState(EXPECT_TEXT_FORMAT);
        return TEXT_FORMAT_END;
    }
}
<YYINITIAL, IN_STRING_VARIANT, IN_TAG_SENSITIVE_TEXT> {
    "|||" {
        yybegin(IN_STRING_VARIANT);
        return STRING_VARIANT_PREFIX;
    }
    "|" { return getFallbackToken(); }
    "&!" {
        enterState(yystate(), EXPECT_TAG_PART);
        yybegin(IN_TAG_PART);
        return TAG_PART_PREFIX;
    }
    "&" { return getFallbackToken(); }
}
<YYINITIAL, IN_COLORFUL_TEXT, IN_CONCEPT_TEXT, IN_TEXT_FORMAT_TEXT> {
    {TextToken} { return TEXT_TOKEN; }
}

// localisation interpolation container rules

<IN_ICON, IN_ICON_ARGUMENT, IN_TEXT_ICON, IN_TEXT_FORMAT> {
    "$" / \S*"$" { // heuristic: require no blank in `$...$`
        enterState(yystate(), EXPECT_PARAMETER);
        yybegin(IN_PARAMETER);
        return PARAMETER_START;
    }
    "$" { return getFallbackToken(); }

    "[[" { return getFallbackToken(); }
    "[" { // heuristic: require `[` is not escaped (double left brackets)
        enterState(yystate(), EXPECT_COMMAND);
        yybegin(IN_COMMAND);
        return LEFT_BRACKET;
    }
    "]" {
        exitState(EXPECT_COMMAND);
        return RIGHT_BRACKET;
    }
}
<IN_PARAMETER> {
    "[[" { return getFallbackToken(); }
    "[" { // heuristic: require `[` is not escaped (double left brackets)
        enterState(yystate(), EXPECT_COMMAND);
        yybegin(IN_COMMAND);
        return LEFT_BRACKET;
    }
    "]" {
        exitState(EXPECT_COMMAND);
        return RIGHT_BRACKET;
    }
}
<IN_COMMAND_TEXT, IN_COMMAND_ARGUMENT, IN_CONCEPT_NAME> {
    "$" / \S*"$" { // heuristic: require no blank in `$...$`
        enterState(yystate(), EXPECT_PARAMETER);
        yybegin(IN_PARAMETER);
        return PARAMETER_START;
    }
    "$" { return getFallbackToken(); }
}

// localisation colorful text rules

<IN_COLORFUL_TEXT_CHECK> {
    {ColorfulTextCheck} {
        if (isColorfulText()) {
            yypushback(yylength() - 1);
            yybegin(IN_COLOR_ID);
            return COLORFUL_TEXT_START;
        } else {
            // enter IN_COLORFUL_TEXT directly for robustness
            yypushback(yylength() - 1);
            yybegin(IN_COLORFUL_TEXT);
            return COLORFUL_TEXT_START;
        }
    }
}
<IN_COLOR_ID> {
    {ColorToken} { yybegin(IN_COLORFUL_TEXT); return COLOR_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

// localisation parameter rules

<IN_PARAMETER, IN_PARAMETER_ARGUMENT, IN_SCRIPTED_VARIABLE_REFERENCE> {
    "$" {
        exitState(EXPECT_PARAMETER);
        return PARAMETER_END;
    }
}
<IN_PARAMETER> {
    "&" {
        enterState(yystate(), EXPECT_CONTEXT_TAG_PART);
        yybegin(IN_CONTEXT_TAG_PART);
        return CONTEXT_TAG_PART_PREFIX;
    }
    "|" { yybegin(IN_PARAMETER_ARGUMENT); return PIPE; }
    "@" { yybegin(IN_SCRIPTED_VARIABLE_REFERENCE); return AT; }
    {ParameterToken} { return PARAMETER_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}
<IN_PARAMETER_ARGUMENT> {
    {ArgumentToken} { return ARGUMENT_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}
<IN_SCRIPTED_VARIABLE_REFERENCE> {
    "|" { yybegin(IN_PARAMETER_ARGUMENT); return PIPE; }
    {ScriptedVariableToken} { return SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

// localisation command rules

<IN_COMMAND> {
    \S {
        if (yycharat(0) == '\'' && ParadoxSyntaxConstraint.LocalisationConceptCommand.testTarget(this)) {
            yybegin(IN_CONCEPT_NAME);
            return LEFT_SINGLE_QUOTE;
        }
        yypushback(1);
        yybegin(IN_COMMAND_TEXT);
    }
    {Blank} { return WHITE_SPACE; } // compatible with blank
}
<IN_COMMAND_TEXT, IN_COMMAND_ARGUMENT> {
    "]" {
        exitState(EXPECT_COMMAND);
        return RIGHT_BRACKET;
    }
}
<IN_COMMAND_TEXT> {
    "&" {
        enterState(yystate(), EXPECT_CONTEXT_TAG_PART);
        yybegin(IN_CONTEXT_TAG_PART);
        return CONTEXT_TAG_PART_PREFIX;
    }
    "::" {
        enterState(yystate(), EXPECT_TAG_SENSITIVE_TEXT);
        yybegin(IN_TAG_SENSITIVE_TEXT);
        return TAG_SENSITIVE_TEXT_PREFIX;
    }
    "|" { yybegin(IN_COMMAND_ARGUMENT); return PIPE; }
    ":" { return COMMAND_TEXT_TOKEN; }
    {CommandTextToken} { return COMMAND_TEXT_TOKEN; }
    {Blank} { return WHITE_SPACE; } // compatible with blank
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}
<IN_COMMAND_ARGUMENT> {
    {ArgumentToken} { return ARGUMENT_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

// [stellaris] localisation concept command rules (as special command rules)

<IN_CONCEPT_NAME> {
    "]" {
        exitState(EXPECT_COMMAND);
        return RIGHT_BRACKET;
    }
}
<IN_CONCEPT_NAME> {
    "'" { return RIGHT_SINGLE_QUOTE; }
    "," { yybegin(IN_CONCEPT_AFTER_COMMA); return COMMA; }
    {ConceptNameToken} { return CONCEPT_NAME_TOKEN; }
    {Blank} { return WHITE_SPACE; } // compatible with blank
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}
<IN_CONCEPT_AFTER_COMMA> {
    // enter text section
    {Blank} { yybegin(IN_CONCEPT_TEXT); return WHITE_SPACE; }
    // whitespace after COMMA may be absent, if so, treat as valid and still enter text section
    [^] { yypushback(yylength()); yybegin(IN_CONCEPT_TEXT); }
}

// localisation icon rules

<IN_ICON, IN_ICON_ARGUMENT> {
    "£" {
        exitState(EXPECT_ICON);
        return ICON_END;
    }
}
<IN_ICON> {
    "|" { yybegin(IN_ICON_ARGUMENT); return PIPE; }
    {IconToken} { return ICON_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}
<IN_ICON_ARGUMENT> {
    {ArgumentToken} { return ARGUMENT_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

// [ck3, vic3, eu5] localisation text icon rules

<IN_TEXT_ICON> {
    "!" {
        exitState(EXPECT_TEXT_ICON);
        return TEXT_ICON_END;
    }
}
<IN_TEXT_ICON> {
    {TextIconToken} { return TEXT_ICON_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

// [ck3, vic3, eu5] localisation text format rules

<IN_TEXT_FORMAT> {
    "#!" {
        exitState(EXPECT_TEXT_FORMAT);
        return TEXT_FORMAT_END;
    }
}
<IN_TEXT_FORMAT> {
    {TextFormatToken} { return TEXT_FORMAT_TOKEN; }
    // enter text section
    {Blank} { yybegin(IN_TEXT_FORMAT_TEXT); return WHITE_SPACE; }
    // whitespace after TEXT_FORMAT_TOKEN may be absent, if so, treat as valid and still enter text section
    [^] { yypushback(yylength()); yybegin(IN_TEXT_FORMAT_TEXT); }
}

// string variant set rules
// e.g., `|||B|||t1:C|||t2,t3:D` in `A|||B|||t1:C|||t2,t3:D`

<IN_STRING_VARIANT> {
    {Blank}?{TagToken} / ({TagToken}|{Blank}|,)*":" { // tag char is required before `:`
        enterState(yystate(), EXPECT_STRING_VARIANT_TAG_PART);
        yypushback(yylength());
        yybegin(IN_STRING_VARIANT_TAG_PART);
    }
    ":" { return getFallbackToken(); }
    // need to exclude `:` additionally (otherwise `t1,t2:` would be incorrectly recognized as TEXT_TOKEN)
    {StringVariantTextToken} { return TEXT_TOKEN; }
}
<IN_STRING_VARIANT_TAG_PART> {
    ":" {
        exitState(EXPECT_STRING_VARIANT_TAG_PART);
        return COLON;
    }
    "," { return COMMA; }
    {TagToken} { return TAG_TOKEN; }
    {Blank} { return WHITE_SPACE; } // allowed (heuristic)
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

// tag-sensitive text rules
// e.g., `::a <1>`
<IN_TAG_SENSITIVE_TEXT> {
    "<" / \S*">" { // heuristic: require no blank in `<...>`
        enterState(yystate(), EXPECT_TAGGED_PARAMETER);
        yybegin(IN_TAGGED_PARAMETER);
        return TAGGED_PARAMETER_START;
    }
    "<" { return getFallbackToken(); }
    // need to exclude `<` additionally (otherwise `<PARAM>` would be incorrectly recognized as TEXT_TOKEN)
    {TagSensitiveTextToken} { return TEXT_TOKEN; }
}
<IN_TAGGED_PARAMETER> {
    "$" {
        exitState(EXPECT_TAGGED_PARAMETER);
        return TAGGED_PARAMETER_END;
    }

    {ParameterToken} { return PARAMETER_TOKEN; }
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

// tag part rules
// e.g., `&!t1,t2` in `text&!t1,t2`

<IN_TAG_PART> {
    "," { return COMMA; }
    {TagToken} { return TAG_TOKEN; }
    {Blank} { return WHITE_SPACE; } // allowed (heuristic)
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

// context tag part rules
// e.g., `&t` in `$1&t$`
// e.g., `&t` in `[From.GetName&t]`

<IN_CONTEXT_TAG_PART> {
    {ContextTagToken} { return CONTEXT_TAG_TOKEN; }
    // {Blank} { return WHITE_SPACE; } // not allowed (heuristic)
    [^] { if (!exitStateForRecoveryIfNeeded()) return BAD_CHARACTER; } // recovery
}

// fallback

[^] { return BAD_CHARACTER; }
