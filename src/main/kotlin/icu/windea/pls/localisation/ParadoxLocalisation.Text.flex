package icu.windea.pls.localisation.lexer;

import java.util.*;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.model.ParadoxGameType;
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.core.StdlibExtensionsKt.*;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;

%%

%{
    private ParadoxGameType gameType;

    private Deque<Integer> nextStateByDepthStack = new ArrayDeque<>();
    private Deque<Integer> nextStateStack = new ArrayDeque<>();

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

    private boolean isStateByDepth(int state) {
        return state == YYINITIAL || state == IN_COLORFUL_TEXT || state == IN_CONCEPT_TEXT;
    }

    private void setNextStateByDepth(int nextState) {
        if (!isStateByDepth(nextState)) {
            nextState = nextStateByDepthStack.isEmpty() ? YYINITIAL : IN_COLORFUL_TEXT;
        }
        nextStateByDepthStack.addLast(nextState);
    }

    private void beginNextStateByDepth() {
        if (nextStateByDepthStack.isEmpty()) {
            yybegin(YYINITIAL);
            return;
        }
        int nextState = nextStateByDepthStack.removeLast();
        yybegin(nextState);
    }

    private void setNextState(int nextState) {
        nextStateStack.addLast(nextState);
    }

    private void beginNextState() {
        if (nextStateStack.isEmpty()) {
            yybegin(YYINITIAL);
            return;
        }
        int nextState = nextStateStack.removeLast();
        yybegin(nextState);
    }

    private boolean isReference() {
        if (yylength() <= 1) return false;
        return yycharat(yylength() - 1) == '$';
    }

    private IElementType checkReference() {
        yypushback(yylength() - 1);
        if (isReference()) {
            yybegin(IN_REFERENCE);
            return PROPERTY_REFERENCE_START;
        } else {
            beginNextState();
            return STRING_TOKEN;
        }
    }

    private boolean isColorfulText() {
        if (yylength() <= 1) return false;
        return isExactLetter(yycharat(1));
    }

    private IElementType checkColorfulText() {
        yypushback(yylength() - 1);
        if (isColorfulText()) {
            yybegin(IN_COLOR_ID);
            return COLORFUL_TEXT_START;
        } else {
            beginNextStateByDepth();
            return STRING_TOKEN;
        }
    }

    private boolean isCommand() {
        if (yylength() <= 1) return false;
        return yycharat(yylength() - 1) == ']';
    }

    private IElementType checkCommand() {
        yypushback(yylength() - 1);
        if (isCommand()) {
            yybegin(IN_COMMAND);
            return COMMAND_START;
        } else {
            beginNextState();
            return STRING_TOKEN;
        }
    }

    private boolean isIcon() {
        if (yylength() <= 1) return false;
        char c = yycharat(1);
        return isExactLetter(c) || isExactDigit(c) || c == '_' || c == '$';
    }

    private IElementType checkIcon() {
        yypushback(yylength() - 1);
        if (isIcon()) {
            yybegin(IN_ICON);
            return ICON_START;
        } else {
            beginNextState();
            return STRING_TOKEN;
        }
    }

    private boolean isConceptQuoted() {
        return yycharat(0) == '\'' && ParadoxSyntaxConstraint.LocalisationConceptQuoted.supports(this);
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

%s CHECK_REFERENCE
%s IN_REFERENCE
%s IN_REFERENCE_PARAMETER_TOKEN

%s IN_SCRIPTED_VARIABLE_REFERENCE

%s CHECK_COMMAND
%s IN_COMMAND
%s IN_COMMAND_TEXT

%s CHECK_ICON
%s IN_ICON
%s IN_ICON_FRAME

%s IN_CONCEPT_NAME
%s IN_CONCEPT_TEXT

%unicode

STRING_TOKEN=.+ // fallback

COLORFUL_TEXT_CHECK=§.?
COLOR_TOKEN=[a-zA-Z0-9]

REFERENCE_CHECK=\$\S*\$? //no blank in $...$
REFERENCE_TOKEN=[a-zA-Z0-9_.\-']+
REFERENCE_PARAMETER_TOKEN=[^\"$£§\[\r\n\\]+

SCRIPTED_VARIABLE_TOKEN=[a-zA-Z_][a-zA-Z0-9_]*

COMMAND_CHECK=\[.?
COMMAND_TEXT_TOKEN=[^\r\n\[\]]+

ICON_CHECK=£.?
ICON_TOKEN=[a-zA-Z0-9\-_\\/]+
ICON_FRAME=[1-9][0-9]* // positive integer

CONCEPT_NAME_TOKEN=[a-zA-Z0-9_:]+

%%

<YYINITIAL, IN_COLORFUL_TEXT, IN_CONCEPT_TEXT> {
    "§" { setNextStateByDepth(yystate()); yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextStateByDepth(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }
    "£" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_ICON); }
}

// localisation colorful text rules

<CHECK_COLORFUL_TEXT> {
    {COLORFUL_TEXT_CHECK} { return checkColorfulText(); }
}
<IN_COLOR_ID>{
    "§" { setNextStateByDepth(yystate()); yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextStateByDepth(); return COLORFUL_TEXT_END; }

    {COLOR_TOKEN} { yybegin(IN_COLORFUL_TEXT); return COLOR_TOKEN; }
    [^] { yypushback(yylength()); beginNextStateByDepth(); }
}

// localisation reference rules

<CHECK_REFERENCE> {
    {REFERENCE_CHECK} { return checkReference(); }
}
<IN_REFERENCE>{
    "§" { setNextStateByDepth(yystate()); yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextStateByDepth(); return COLORFUL_TEXT_END; }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }

    "$" { setNextState(yystate()); return PROPERTY_REFERENCE_END; }
    "|" { yybegin(IN_REFERENCE_PARAMETER_TOKEN); return PIPE; }
    "@" { yybegin(IN_SCRIPTED_VARIABLE_REFERENCE); return AT; }
    {REFERENCE_TOKEN} { return PROPERTY_REFERENCE_TOKEN; }
}
<IN_REFERENCE_PARAMETER_TOKEN>{
    "§" { setNextStateByDepth(yystate()); yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextStateByDepth(); return COLORFUL_TEXT_END; }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }

    "$" { beginNextState(); return PROPERTY_REFERENCE_END; }
    {REFERENCE_PARAMETER_TOKEN} { return PROPERTY_REFERENCE_PARAMETER_TOKEN; }
}

// scripted variable reference rules (in references)

<IN_SCRIPTED_VARIABLE_REFERENCE>{
    "$" { beginNextState(); return PROPERTY_REFERENCE_END; }
    "|" { yybegin(IN_REFERENCE_PARAMETER_TOKEN); return PIPE; }
    {SCRIPTED_VARIABLE_TOKEN} { return SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
}

// localisation command rules

<CHECK_COMMAND> {
    {COMMAND_CHECK} { return checkCommand(); }
}
<IN_COMMAND>{
    . {
        if(isConceptQuoted()) {
            yybegin(IN_CONCEPT_NAME);
            return LEFT_SINGLE_QUOTE;
        }
        yypushback(1);
        yybegin(IN_COMMAND_TEXT);
    }
}
<IN_COMMAND_TEXT>{
    "§" { setNextStateByDepth(yystate()); yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextStateByDepth(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }

    "]" { beginNextState(); return COMMAND_END; }
    {COMMAND_TEXT_TOKEN} { return COMMAND_TEXT_TOKEN; }
}

// localisation icon rules

<CHECK_ICON>{
    {ICON_CHECK} { return checkIcon(); }
}
<IN_ICON>{
    "§" { setNextStateByDepth(yystate()); yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextStateByDepth(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }

    "£" { beginNextState(); return ICON_END; }
    "|" { yybegin(IN_ICON_FRAME); return PIPE; }
    {ICON_TOKEN} { return ICON_TOKEN; }
}
<IN_ICON_FRAME>{
    "§" { setNextStateByDepth(yystate()); yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextStateByDepth(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }

    "£" { beginNextState(); return ICON_END; }
    {ICON_FRAME} { return ICON_FRAME; }
}

// [stellaris] localisation concept rules (as special commands)

<IN_CONCEPT_NAME> {
    "§" { setNextStateByDepth(yystate()); yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextStateByDepth(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }

    "]" { beginNextState(); return COMMAND_END; }
    "'" { return RIGHT_SINGLE_QUOTE; }
    "," { yybegin(IN_CONCEPT_TEXT); return COMMA; }
    {CONCEPT_NAME_TOKEN} { return CONCEPT_NAME_TOKEN; }
}
<IN_CONCEPT_TEXT> {
    "]" { beginNextStateByDepth(); return COMMAND_END; }
}

// [ck3, vic3] localisation formatting rules

// TODO 1.4.0

// [ck3, vic3] localisation text icon rules

// TODO 1.4.0

[^] { return BAD_CHARACTER; }
