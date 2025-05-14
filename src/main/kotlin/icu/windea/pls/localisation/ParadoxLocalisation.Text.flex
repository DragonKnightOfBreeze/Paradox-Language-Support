package icu.windea.pls.localisation.lexer;

import java.util.*;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.model.ParadoxGameType;
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint;
import it.unimi.dsi.fastutil.ints.*;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.core.StdlibExtensionsKt.*;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;

%%

%{
    private ParadoxGameType gameType;

    private IntStack nextStateByDepthStack = null;
    private IntStack nextStateStack = null;

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
        if (nextStateByDepthStack == null) {
            nextStateByDepthStack = new IntArrayList();
        }
        if (!isStateByDepth(nextState)) {
            nextState = nextStateByDepthStack.isEmpty() ? YYINITIAL : IN_COLORFUL_TEXT;
        }
        nextStateByDepthStack.push(nextState);
    }

    private void beginNextStateByDepth() {
        if (nextStateByDepthStack == null || nextStateByDepthStack.isEmpty()) {
            yybegin(YYINITIAL);
            return;
        }
        int nextState = nextStateByDepthStack.popInt();
        yybegin(nextState);
    }

    private void setNextState(int nextState) {
        if (nextStateStack == null) {
            nextStateStack = new IntArrayList();
        }
        nextStateStack.push(nextState);
    }

    private void beginNextState() {
        if (nextStateStack == null || nextStateStack.isEmpty()) {
            yybegin(YYINITIAL);
            return;
        }
        int nextState = nextStateStack.popInt();
        yybegin(nextState);
    }

    private boolean isReference() {
        if (yylength() <= 1) return false;
        return yycharat(yylength() - 1) == '$';
    }

    private IElementType checkReference() {
        if (isReference()) {
            yypushback(yylength() - 1);
            yybegin(IN_REFERENCE);
            return PROPERTY_REFERENCE_START;
        } else {
            yypushback(yylength() - 1);
            beginNextState();
            return STRING_TOKEN;
        }
    }

    private boolean isColorfulText() {
        if (yylength() <= 1) return false;
        return isExactWord(yycharat(1)); // exact letter after prefix
    }

    private IElementType checkColorfulText() {
        if (isColorfulText()) {
            yypushback(yylength() - 1);
            yybegin(IN_COLOR_ID);
            return COLORFUL_TEXT_START;
        } else {
            // skip into IN_COLORFUL_TEXT, rather than fallback
            yypushback(yylength() - 1);
            yybegin(IN_COLORFUL_TEXT);
            return COLORFUL_TEXT_START;
        }
    }

    private boolean isCommand() {
        if (yylength() <= 1) return false;
        return yycharat(yylength() - 1) != '['; // not double quote
    }

    private IElementType checkCommand() {
        if (isCommand()) {
            yypushback(yylength() - 1);
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
        return c == '$' || isExactWord(c);
    }

    private IElementType checkIcon() {
        if (isIcon()) {
            yypushback(yylength() - 1);
            yybegin(IN_ICON);
            return ICON_START;
        } else {
            yypushback(yylength() - 1);
            beginNextState();
            return STRING_TOKEN;
        }
    }

    private boolean isConceptQuoted() {
        return yycharat(0) == '\'' && ParadoxSyntaxConstraint.LocalisationConceptQuoted.supports(this);
    }



    private IElementType checkBlank() {
        if (yystate() != YYINITIAL && yystate() != IN_COLORFUL_TEXT) {
            return WHITE_SPACE;
        }
        return STRING_TOKEN;
    }

    private IElementType checkConceptTextEnd() {
        if(yystate() == IN_CONCEPT_TEXT) {
            beginNextStateByDepth();
            return COMMAND_END;
        }
        return STRING_TOKEN;
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
%s IN_REFERENCE_ARGUMENT

%s IN_SCRIPTED_VARIABLE_REFERENCE

%s CHECK_COMMAND
%s IN_COMMAND
%s IN_COMMAND_TEXT
%s IN_COMMAND_ARGUMENT

%s CHECK_ICON
%s IN_ICON
%s IN_ICON_ARGUMENT

%s IN_CONCEPT_NAME
%s IN_CONCEPT_TEXT

%s CHECK_TEXT_FORMAT
%s IN_TEXT_FORMAT_ID
%s IN_TEXT_FORMAT_TEXT

%unicode

BLANK=\s+

PLAIN_TEXT_TOKEN=[^\s§\$\[\]£}][^§\$\[\]£]*

COLORFUL_TEXT_CHECK=§.?
COLOR_TOKEN=\w
REFERENCE_CHECK=\$(\S*\$|.?) //no blank in $...$
REFERENCE_TOKEN=[a-zA-Z0-9_.\-']+
REFERENCE_ARGUMENT_TOKEN=[^\"§\$\[\]\r\n\\]+

SCRIPTED_VARIABLE_TOKEN=[a-zA-Z_][a-zA-Z0-9_]*

COMMAND_CHECK=\[.?
COMMAND_TEXT_TOKEN=[^\r\n\[\]\|]+
COMMAND_ARGUMENT_TOKEN=[^\"§\$\[\]\r\n\\]+

ICON_CHECK=£.?
ICON_TOKEN=[a-zA-Z0-9\-_\\/]+
ICON_ARGUMENT_TOKEN=[1-9][0-9]* // positive integer

CONCEPT_NAME_TOKEN=[a-zA-Z0-9_:]+

%%

<YYINITIAL, IN_COLORFUL_TEXT, IN_CONCEPT_TEXT> {
    "§" { setNextStateByDepth(yystate()); yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextStateByDepth(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }
    "£" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_ICON); }
    {PLAIN_TEXT_TOKEN} { return STRING_TOKEN; }
    {BLANK} { return checkBlank(); }
    "]" { return checkConceptTextEnd(); }
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

    "$" { beginNextState(); return PROPERTY_REFERENCE_END; }
    "|" { yybegin(IN_REFERENCE_ARGUMENT); return PIPE; }
    "@" { yybegin(IN_SCRIPTED_VARIABLE_REFERENCE); return AT; }
    {REFERENCE_TOKEN} { return PROPERTY_REFERENCE_TOKEN; }
}
<IN_REFERENCE_ARGUMENT>{
    "§" { setNextStateByDepth(yystate()); yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextStateByDepth(); return COLORFUL_TEXT_END; }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }

    "$" { beginNextState(); return PROPERTY_REFERENCE_END; }
    {REFERENCE_ARGUMENT_TOKEN} { return PROPERTY_REFERENCE_ARGUMENT_TOKEN; }
}

// scripted variable reference rules (in references)

<IN_SCRIPTED_VARIABLE_REFERENCE>{
    "$" { beginNextState(); return PROPERTY_REFERENCE_END; }
    "|" { yybegin(IN_REFERENCE_ARGUMENT); return PIPE; }
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
    "|" { yybegin(IN_COMMAND_ARGUMENT); return PIPE; }
    {COMMAND_TEXT_TOKEN} { return COMMAND_TEXT_TOKEN; }
}
<IN_COMMAND_ARGUMENT>{
    "§" { setNextStateByDepth(yystate()); yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextStateByDepth(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }

    "]" { beginNextState(); return COMMAND_END; }
    {COMMAND_ARGUMENT_TOKEN} { return COMMAND_ARGUMENT_TOKEN; }
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
    "|" { yybegin(IN_ICON_ARGUMENT); return PIPE; }
    {ICON_TOKEN} { return ICON_TOKEN; }
}
<IN_ICON_ARGUMENT>{
    "§" { setNextStateByDepth(yystate()); yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextStateByDepth(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }

    "£" { beginNextState(); return ICON_END; }
    {ICON_ARGUMENT_TOKEN} { return ICON_ARGUMENT_TOKEN; }
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

// [ck3, vic3] localisation text format rules

// TODO 1.4.0

// [ck3, vic3] localisation text icon rules

// TODO 1.4.0

[^] { return BAD_CHARACTER; }
