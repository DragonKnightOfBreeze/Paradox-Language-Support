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
    private ParadoxGameType gameType;

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

    private boolean isParameter() {
        if (yylength() <= 1) return false;
        char c = yycharat(yylength() - 1);
        return c == '$'; // parameter end marker at end
    }

    private boolean isCommand() {
        if (yylength() <= 1) return false;
        char c = yycharat(yylength() - 1);
        return c != '['; // not left bracket after prefix (double left brackets -> escaped)
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

%s IN_PARAMETER_CHECK
%s IN_PARAMETER
%s IN_PARAMETER_ARGUMENT
%s IN_SCRIPTED_VARIABLE_REFERENCE

%s IN_COMMAND_CHECK
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

%unicode

Blank = \s+

IdentifierChar = [A-Za-z0-9_]
IdentifierLeadChar = [A-Za-z_] // leading number is not allowed
IdentifierToken = {IdentifierLeadChar}{IdentifierChar}* // leading number is not allowed

InterpolationMarkerChar = [$|\[\]]
InterpolationLeadChar = [$\[]

ScriptedVariableToken = {IdentifierToken} // identifier

ParameterCheck = \$(\S*\$|.?) // no blank in $...$
ParameterChar = {IdentifierChar}|[.\-'] // `.-'` is allowed additionally
ParameterToken = {ParameterChar}+ // leading number is allowed & `.-'` is allowed additionally

ArgumentChar = [^$§£\[\]\\\s] // `|` is allowed?
ArgumentToken = {ArgumentChar}+

ColorfulTextCheck = §.?
ColorToken = {IdentifierChar} // identifier char

CommandCheck = \[.?
CommandTextChar = [^\[\]\|\r\n] // `[]` within single quotes are not allowed?
CommandTextBoundChar = [^\[\]\|\s] // `[]` within single quotes are not allowed?
CommandTextToken = {CommandTextBoundChar}({CommandTextChar}*{CommandTextBoundChar})? // inner whitespaces are allowed

ConceptNameChar = [A-Za-z0-9_:] // `:` is allowed additionally
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

TextToken = ([^§£\$\[\]#@]|\\[\s\S])+

%%

// common rules

<YYINITIAL, IN_COLORFUL_TEXT, IN_CONCEPT_TEXT, IN_TEXT_FORMAT_TEXT> {
    "§" {
        enterState(yystate(), EXPECT_COLORFUL_TEXT);
        yypushback(yylength());
        yybegin(IN_COLORFUL_TEXT_CHECK);
    }
    "§!" { // dangling form is allowed here at syntax level
        exitState(EXPECT_COLORFUL_TEXT);
        return COLORFUL_TEXT_END;
    }

    "$" {
        enterState(yystate(), EXPECT_PARAMETER);
        yypushback(yylength());
        yybegin(IN_PARAMETER_CHECK);
    }

    "[" {
        enterState(yystate(), EXPECT_COMMAND);
        yypushback(yylength());
        yybegin(IN_COMMAND_CHECK);
    }
    "]" {
        if (yystate() != IN_CONCEPT_TEXT) return getFallbackToken();
        exitState(EXPECT_COMMAND);
        return RIGHT_BRACKET;
    }

    "£" / {IconWildcardLeadChar} {
        enterState(yystate(), EXPECT_ICON);
        yybegin(IN_ICON);
        return ICON_START;
    }
    "£" {
        return getFallbackToken();
    }

    "@" / {TextIconWildcardLeadChar} {
        if (!ParadoxSyntaxConstraint.LocalisationTextIcon.testTarget(this)) return getFallbackToken();
        enterState(yystate(), EXPECT_TEXT_ICON);
        yybegin(IN_TEXT_ICON);
        return TEXT_ICON_START;
    }
    "@" {
        return getFallbackToken();
    }

    "#" / {TextFormatWildcardLeadChar} {
        if (!ParadoxSyntaxConstraint.LocalisationTextFormat.testTarget(this)) return getFallbackToken();
        enterState(yystate(), EXPECT_TEXT_FORMAT);
        yybegin(IN_TEXT_FORMAT);
        return TEXT_FORMAT_START;
    }
    "#" {
        return getFallbackToken();
    }
    "#!" { // dangling form is allowed here at syntax level
        if (!ParadoxSyntaxConstraint.LocalisationTextFormat.testTarget(this)) return getFallbackToken();
        exitState(EXPECT_TEXT_FORMAT);
        return TEXT_FORMAT_END;
    }

    {TextToken} { return TEXT_TOKEN; }
}

// localisation interpolation container rules

<IN_ICON, IN_ICON_ARGUMENT, IN_TEXT_ICON, IN_TEXT_FORMAT> {
    "$" {
        enterState(yystate(), EXPECT_PARAMETER);
        yypushback(yylength());
        yybegin(IN_PARAMETER_CHECK);
    }

    "[" {
        enterState(yystate(), EXPECT_COMMAND);
        yypushback(yylength());
        yybegin(IN_COMMAND_CHECK);
    }
    "]" {
        exitState(EXPECT_COMMAND);
        return RIGHT_BRACKET;
    }
}
<IN_PARAMETER> {
    "[" {
        enterState(yystate(), EXPECT_COMMAND);
        yypushback(yylength());
        yybegin(IN_COMMAND_CHECK);
    }
    "]" {
        exitState(EXPECT_COMMAND);
        return RIGHT_BRACKET;
    }
}
<IN_COMMAND_TEXT, IN_COMMAND_ARGUMENT, IN_CONCEPT_NAME> {
    "$" {
        enterState(yystate(), EXPECT_PARAMETER);
        yypushback(yylength());
        yybegin(IN_PARAMETER_CHECK);
    }
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

<IN_PARAMETER_CHECK> {
    {ParameterCheck} {
        if (isParameter()) {
            yypushback(yylength() - 1);
            yybegin(IN_PARAMETER);
            return PARAMETER_START;
        }
        exitState(EXPECT_PARAMETER);
        yypushback(yylength() - 1);
        return getFallbackToken();
    }
}
<IN_PARAMETER, IN_PARAMETER_ARGUMENT, IN_SCRIPTED_VARIABLE_REFERENCE> {
    "$" {
        exitState(EXPECT_PARAMETER);
        return PARAMETER_END;
    }
}
<IN_PARAMETER> {
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

<IN_COMMAND_CHECK> {
    {CommandCheck} {
        if (isCommand()) {
            yypushback(yylength() - 1);
            yybegin(IN_COMMAND);
            return LEFT_BRACKET;
        }
        exitState(EXPECT_COMMAND);
        return getFallbackToken();
    }
}
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
    "|" { yybegin(IN_COMMAND_ARGUMENT); return PIPE; }
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

// fallback

[^] { return BAD_CHARACTER; }
