package icu.windea.pls.localisation.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.model.ParadoxGameType;
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;

// Lexer for localisation text of Paradox Localisation.
// Notes:
// - Use `stateStack` and `expectStack` to manage lexer-level states.
// - Use `ParadoxSyntaxConstraint` to check whether specific syntax is supported in current game type.

%%

%{
    private ParadoxGameType gameType;

    // stack for context states (states that need to fallback when exit some constructs)
    private IntStack stateStack = null;
    // stack for expected construct types (e.g., EXPECT_COLORFUL_TEXT)
    private IntStack expectStack = null;

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
        // exit state to previous
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

    private boolean exitStateOnBadCharacter() {
        // exit state for bad character (as fallback)
        // heuristic: always exist
        exitState();
        yypushback(yylength());
        return true;
    }

    private boolean isExactWord(char c) {
        return c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') && (c >= '0' && c <= '9');
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

    private boolean isIcon() {
        if (yylength() <= 1) return false;
        char c = yycharat(1);
        return c == '[' || c == '$' || isExactWord(c); // exact word (or interpolation start marker) after prefix
    }

    private boolean isCommand() {
        if (yylength() <= 1) return false;
        char c = yycharat(yylength() - 1);
        return c != '['; // not left bracket after prefix (double left brackets -> escaped)
    }

    private boolean isTextIcon() {
        if (yylength() <= 1) return false;
        char c = yycharat(1);
        return c == '[' || c == '$' || isExactWord(c); // exact word (or interpolation start marker) after prefix
    }

    private boolean isTextFormat() {
        if (yylength() <= 1) return false;
        char c = yycharat(1);
        return c == '[' || c == '$' || isExactWord(c); // exact word (or interpolation start marker) after prefix
    }

    private void pushbackIfBlank() {
        int index = 0;
        while (index < yylength() && Character.isWhitespace(yycharat(yylength() - index - 1))) index++;
        if (index > 0) yypushback(index);
    }
%}

%public
%class _ParadoxLocalisationTextLexer
%implements FlexLexer
%function advance
%type IElementType

%s CHECK_COLORFUL_TEXT
%s IN_COLOR_ID
%s IN_COLORFUL_TEXT

%s CHECK_PARAMETER
%s IN_PARAMETER
%s IN_PARAMETER_ARGUMENT
%s IN_SCRIPTED_VARIABLE_REFERENCE

%s CHECK_ICON
%s IN_ICON
%s IN_ICON_ARGUMENT

%s CHECK_COMMAND
%s IN_COMMAND
%s IN_COMMAND_TEXT
%s IN_COMMAND_ARGUMENT

%s IN_CONCEPT_NAME
%s IN_CONCEPT_AFTER_COMMA
%s IN_CONCEPT_TEXT

%s CHECK_TEXT_ICON
%s IN_TEXT_ICON

%s CHECK_TEXT_FORMAT
%s IN_TEXT_FORMAT_ID
%s IN_TEXT_FORMAT_TEXT

%unicode

BLANK=\s+

TEXT_TOKEN=([^§£\$\[\]#@]|\\[\s\S])+

IDENTIFIER_CHAR=[A-Za-z0-9_]
IDENTIFIER_LEAD_CHAR=[A-Za-z_] // leading number is not allowed
IDENTIFIER_TOKEN={IDENTIFIER_LEAD_CHAR}{IDENTIFIER_CHAR}* // leading number is not allowed

ARGUMENT_CHAR=[^\"§£\$\[\]\\\s] // `|` is allowed?
ARGUMENT_TOKEN={ARGUMENT_CHAR}+

PARAMETER_CHECK=\$(\S*\$|.?) // no blank in $...$
PARAMETER_CHAR={IDENTIFIER_CHAR}|[.\-'] // `-'` is allowed additionally
PARAMETER_LEAD_CHAR={IDENTIFIER_LEAD_CHAR}|[.\-'] // leading number is not allowed & `-'` is allowed additionally
PARAMETER_TOKEN={PARAMETER_LEAD_CHAR}{PARAMETER_CHAR}* // leading number is not allowed & `-'` is allowed additionally

SCRIPTED_VARIABLE_TOKEN={IDENTIFIER_TOKEN} // identifier

COLORFUL_TEXT_CHECK=§.?
COLOR_TOKEN=\w

COMMAND_CHECK=\[.?
COMMAND_TEXT_CHAR=[^'\[\]\|\s] // `[]` within single quotes are not allowed?
COMMAND_TEXT_BOUND_CHAR=[^'\[\]\|\r\n] // `[]` within single quotes are not allowed?
COMMAND_TEXT_TOKEN={COMMAND_TEXT_BOUND_CHAR}({COMMAND_TEXT_CHAR}*{COMMAND_TEXT_BOUND_CHAR})? // inner whitespaces are allowed

CONCEPT_NAME_CHAR=[A-Za-z0-9_:] // `:` is allowed additionally
CONCEPT_NAME_TOKEN={CONCEPT_NAME_CHAR}+

ICON_CHECK=£.?
ICON_CHAR={IDENTIFIER_CHAR}|[\-/\\] // `-/\` is allowed additionally
ICON_TOKEN={ICON_CHAR}+ // leading number is allowed

TEXT_ICON_CHECK=@.?
TEXT_ICON_CHAR={IDENTIFIER_CHAR} // identifier
TEXT_ICON_TOKEN={TEXT_ICON_CHAR}+ // leading number is allowed

// `italic;color:green` form is allowed
TEXT_FORMAT_CHECK=#.?
TEXT_FORMAT_CHAR={IDENTIFIER_CHAR}|[:'] // `:'` is allowed additionally
TEXT_FORMAT_TOKEN={TEXT_FORMAT_CHAR}+ // leading number is allowed

%%

<YYINITIAL, IN_COLORFUL_TEXT, IN_CONCEPT_TEXT, IN_TEXT_FORMAT_TEXT> {
    "§" {
        enterState(yystate(), EXPECT_COLORFUL_TEXT);
        yypushback(yylength());
        yybegin(CHECK_COLORFUL_TEXT);
    }
    "$" {
        enterState(yystate(), EXPECT_PARAMETER);
        yypushback(yylength());
        yybegin(CHECK_PARAMETER);
    }
    "[" {
        enterState(yystate(), EXPECT_COMMAND);
        yypushback(yylength());
        yybegin(CHECK_COMMAND);
    }
    "£" {
        enterState(yystate(), EXPECT_ICON);
        yypushback(yylength());
        yybegin(CHECK_ICON);
    }
    "@" {
        if (!ParadoxSyntaxConstraint.LocalisationTextIcon.testTarget(this)) return TEXT_TOKEN;
        enterState(yystate(), EXPECT_TEXT_ICON);
        yypushback(yylength());
        yybegin(CHECK_TEXT_ICON);
    }
    "#" {
        if (!ParadoxSyntaxConstraint.LocalisationTextFormat.testTarget(this)) return TEXT_TOKEN;
        enterState(yystate(), EXPECT_TEXT_FORMAT);
        yypushback(yylength());
        yybegin(CHECK_TEXT_FORMAT);
    }
    "§!" {
        exitState(EXPECT_COLORFUL_TEXT);
        return COLORFUL_TEXT_END;
    }
    "]" {
        if (yystate() != IN_CONCEPT_TEXT) return TEXT_TOKEN;
        exitState(EXPECT_COMMAND);
        return RIGHT_BRACKET;
    }
    "#!" {
        if (!ParadoxSyntaxConstraint.LocalisationTextFormat.testTarget(this)) return TEXT_TOKEN;
        exitState(EXPECT_TEXT_FORMAT);
        return TEXT_FORMAT_END;
    }

    {TEXT_TOKEN} { return TEXT_TOKEN; }
}

// localisation interpolation container rules

<IN_ICON, IN_ICON_ARGUMENT, IN_TEXT_ICON, IN_TEXT_FORMAT_ID> {
    "$" {
        enterState(yystate(), EXPECT_PARAMETER);
        yypushback(yylength());
        yybegin(CHECK_PARAMETER);
    }
    "[" {
        enterState(yystate(), EXPECT_COMMAND);
        yypushback(yylength());
        yybegin(CHECK_COMMAND);
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
        yybegin(CHECK_COMMAND);
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
        yybegin(CHECK_PARAMETER);
    }
}

// localisation colorful text rules

<CHECK_COLORFUL_TEXT> {
    {COLORFUL_TEXT_CHECK} {
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
    {COLOR_TOKEN} { yybegin(IN_COLORFUL_TEXT); return COLOR_TOKEN; }
    [^] { if (!exitStateOnBadCharacter()) return BAD_CHARACTER; } // recovery
}

// localisation parameter rules

<CHECK_PARAMETER> {
    {PARAMETER_CHECK} {
        if (isParameter()) {
            yypushback(yylength() - 1);
            yybegin(IN_PARAMETER);
            return PARAMETER_START;
        } else {
            exitState();
            yypushback(yylength() - 1);
            return TEXT_TOKEN;
        }
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
    {PARAMETER_TOKEN} { return PARAMETER_TOKEN; }
    [^] { if (!exitStateOnBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_PARAMETER_ARGUMENT> {
    {ARGUMENT_TOKEN} { return ARGUMENT_TOKEN; }
    [^] { if (!exitStateOnBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_SCRIPTED_VARIABLE_REFERENCE> {
    "|" { yybegin(IN_PARAMETER_ARGUMENT); return PIPE; }
    {SCRIPTED_VARIABLE_TOKEN} { return SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
    [^] { if (!exitStateOnBadCharacter()) return BAD_CHARACTER; } // recovery
}

// localisation command rules

<CHECK_COMMAND> {
    {COMMAND_CHECK} {
        if (isCommand()) {
            yypushback(yylength() - 1);
            yybegin(IN_COMMAND);
            return LEFT_BRACKET;
        } else {
            exitState();
            return TEXT_TOKEN;
        }
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
    {BLANK} { return WHITE_SPACE; } // compatible with blank
}
<IN_COMMAND_TEXT, IN_COMMAND_ARGUMENT> {
    "]" {
        exitState(EXPECT_COMMAND);
        return RIGHT_BRACKET;
    }
}
<IN_COMMAND_TEXT> {
    "|" { yybegin(IN_COMMAND_ARGUMENT); return PIPE; }
    {COMMAND_TEXT_TOKEN} { pushbackIfBlank(); return COMMAND_TEXT_TOKEN; } // trailing blank should be pushbacked
    {BLANK} { return WHITE_SPACE; } // compatible with blank
    [^] { if (!exitStateOnBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_COMMAND_ARGUMENT> {
    {ARGUMENT_TOKEN} { return ARGUMENT_TOKEN; }
    [^] { if (!exitStateOnBadCharacter()) return BAD_CHARACTER; } // recovery
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
    {CONCEPT_NAME_TOKEN} { return CONCEPT_NAME_TOKEN; }
    {BLANK} { return WHITE_SPACE; } // compatible with blank
    [^] { if (!exitStateOnBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_CONCEPT_AFTER_COMMA> {
    // enter text section
    {BLANK} { yybegin(IN_CONCEPT_TEXT); return WHITE_SPACE; }
    // whitespace after COMMA may be absent, if so, treat as valid and still enter text section
    [^] { yypushback(yylength()); yybegin(IN_CONCEPT_TEXT); }
}

// localisation icon rules

<CHECK_ICON> {
    {ICON_CHECK} {
        if (isIcon()) {
            yypushback(yylength() - 1);
            yybegin(IN_ICON);
            return ICON_START;
        } else {
            exitState();
            yypushback(yylength() - 1);
            return TEXT_TOKEN;
        }
    }
}
<IN_ICON, IN_ICON_ARGUMENT> {
    "£" {
        exitState(EXPECT_ICON);
        return ICON_END;
    }
}
<IN_ICON> {
    "|" { yybegin(IN_ICON_ARGUMENT); return PIPE; }
    {ICON_TOKEN} { return ICON_TOKEN; }
    [^] { if (!exitStateOnBadCharacter()) return BAD_CHARACTER; } // recovery
}
<IN_ICON_ARGUMENT> {
    {ARGUMENT_TOKEN} { return ARGUMENT_TOKEN; }
    [^] { if (!exitStateOnBadCharacter()) return BAD_CHARACTER; } // recovery
}

// [ck3, vic3, eu5] localisation text icon rules

<CHECK_TEXT_ICON> {
    {TEXT_ICON_CHECK} {
        if (isTextIcon()) {
            yypushback(yylength() - 1);
            yybegin(IN_TEXT_ICON);
            return TEXT_ICON_START;
        } else {
            exitState();
            yypushback(yylength() - 1);
            return TEXT_TOKEN;
        }
    }
}
<IN_TEXT_ICON> {
    "!" {
        exitState(EXPECT_TEXT_ICON);
        return TEXT_ICON_END;
    }
}
<IN_TEXT_ICON> {
    {TEXT_ICON_TOKEN} { return TEXT_ICON_TOKEN; }
    [^] { if (!exitStateOnBadCharacter()) return BAD_CHARACTER; } // recovery
}

// [ck3, vic3, eu5] localisation text format rules

<CHECK_TEXT_FORMAT> {
    {TEXT_FORMAT_CHECK} {
        if (isTextFormat()) {
            yypushback(yylength() - 1);
            yybegin(IN_TEXT_FORMAT_ID);
            return TEXT_FORMAT_START;
        } else {
            exitState();
            yypushback(yylength() - 1);
            return TEXT_TOKEN;
        }
    }
}
<IN_TEXT_FORMAT_ID> {
    "#!" {
        exitState(EXPECT_TEXT_FORMAT);
        return TEXT_FORMAT_END;
    }
}
<IN_TEXT_FORMAT_ID> {
    {TEXT_FORMAT_TOKEN} { return TEXT_FORMAT_TOKEN; }
    // enter text section
    {BLANK} { yybegin(IN_TEXT_FORMAT_TEXT); return WHITE_SPACE; }
    // whitespace after TEXT_FORMAT_TOKEN may be absent, if so, treat as valid and still enter text section
    [^] { yypushback(yylength()); yybegin(IN_TEXT_FORMAT_TEXT); }
}

// fallback

[^] { return BAD_CHARACTER; }
