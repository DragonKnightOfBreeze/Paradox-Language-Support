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

    //TODO 1.4.2+ 这里的状态栈处理可能有些问题，不过除非对于存在语法错误的场景，否则问题应当不大
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
            yybegin(IN_PARAMETER);
            return PARAMETER_START;
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
            return LEFT_BRACKET;
        } else {
            beginNextState();
            return STRING_TOKEN;
        }
    }

    private boolean isIcon() {
        if (yylength() <= 1) return false;
        char c = yycharat(1);
        return c == '[' || c == '$' || isExactWord(c);
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

    private boolean isTextFormat() {
        if (yylength() <= 1) return false;
        char c = yycharat(1);
        return c == '[' || c == '$' || isExactWord(c);
    }

    private IElementType checkTextFormat() {
        if (isTextFormat()) {
            yypushback(yylength() - 1);
            yybegin(IN_TEXT_FORMAT_ID);
            return TEXT_FORMAT_START;
        } else {
            yypushback(yylength() - 1);
            beginNextState();
            return STRING_TOKEN;
        }
    }

    private boolean isTextIcon() {
        if (yylength() <= 1) return false;
        char c = yycharat(1);
        return c == '[' || c == '$' || isExactWord(c);
    }

    private IElementType checkTextIcon() {
        if (isTextIcon()) {
            yypushback(yylength() - 1);
            yybegin(IN_TEXT_ICON);
            return TEXT_ICON_START;
        } else {
            yypushback(yylength() - 1);
            beginNextState();
            return STRING_TOKEN;
        }
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
%s IN_PARAMETER
%s IN_PARAMETER_ARGUMENT

%s IN_SCRIPTED_VARIABLE_REFERENCE

%s CHECK_COMMAND
%s IN_COMMAND
%s IN_COMMAND_TEXT
%s IN_COMMAND_ARGUMENT

%s CHECK_ICON
%s IN_ICON
%s IN_ICON_ARGUMENT

%s IN_CONCEPT_NAME
%s IN_CONCEPT_BLANK
%s IN_CONCEPT_TEXT

%s CHECK_TEXT_FORMAT
%s IN_TEXT_FORMAT_ID
%s IN_TEXT_FORMAT_TEXT

%s CHECK_TEXT_ICON
%s IN_TEXT_ICON

%unicode

BLANK=\s+

PLAIN_TEXT_TOKEN=[^§\$\[\]£#@]+
ARGUMENT_TOKEN=[^\"§\$\[\]\r\n\\]+ // pipe is allowed?

COLORFUL_TEXT_CHECK=§.?
COLOR_TOKEN=\w
REFERENCE_CHECK=\$(\S*\$|.?) // no blank in $...$
REFERENCE_TOKEN=[a-zA-Z0-9_.\-']+

SCRIPTED_VARIABLE_TOKEN=[a-zA-Z_][a-zA-Z0-9_]*

COMMAND_CHECK=\[.?
COMMAND_TEXT_TOKEN=([^\r\n\'\[\]]+)|('([^'\\\r\n]|\\[\s\S])*'?)

ICON_CHECK=£.?
ICON_TOKEN=[a-zA-Z0-9\-_\\/]+

CONCEPT_NAME_TOKEN=[a-zA-Z0-9_:]+

TEXT_FORMAT_CHECK=#.?
TEXT_FORMAT_TOKEN=[\w:;]+ // "italic;color:green" is allowed

TEXT_ICON_CHECK=@.?
TEXT_ICON_TOKEN=\w+

%%

<YYINITIAL, IN_COLORFUL_TEXT, IN_CONCEPT_TEXT, IN_TEXT_FORMAT_TEXT> {
    "§" { yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextState(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }
    "£" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_ICON); }
    "#" {
        if (!ParadoxSyntaxConstraint.LocalisationTextFormat.supports(this)) return STRING_TOKEN;
        setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_TEXT_FORMAT);
    }
    "#!" {
        if (!ParadoxSyntaxConstraint.LocalisationTextFormat.supports(this)) return STRING_TOKEN;
        beginNextState(); return TEXT_FORMAT_END;
    }
    "@" {
        if (!ParadoxSyntaxConstraint.LocalisationTextIcon.supports(this)) return STRING_TOKEN;
        setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_TEXT_ICON);
    }
    {PLAIN_TEXT_TOKEN} { return STRING_TOKEN; }
    "]" {
        if (yystate() != IN_CONCEPT_TEXT) return STRING_TOKEN;
        beginNextState(); return RIGHT_BRACKET;
    }
}

// localisation colorful text rules

<CHECK_COLORFUL_TEXT> {
    {COLORFUL_TEXT_CHECK} { return checkColorfulText(); }
}
<IN_COLOR_ID>{
    "§" { yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextState(); return COLORFUL_TEXT_END; }

    {COLOR_TOKEN} { yybegin(IN_COLORFUL_TEXT); return COLOR_TOKEN; }
    [^] { yypushback(yylength()); beginNextState(); }
}

// localisation reference rules

<CHECK_REFERENCE> {
    {REFERENCE_CHECK} { return checkReference(); }
}
<IN_PARAMETER>{
    "§" { yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextState(); return COLORFUL_TEXT_END; }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }

    "$" { beginNextState(); return PARAMETER_END; }
    "|" { yybegin(IN_PARAMETER_ARGUMENT); return PIPE; }
    "@" { yybegin(IN_SCRIPTED_VARIABLE_REFERENCE); return AT; }
    {REFERENCE_TOKEN} { return PARAMETER_TOKEN; }
}
<IN_PARAMETER_ARGUMENT>{
    "§" { yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextState(); return COLORFUL_TEXT_END; }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }

    "$" { beginNextState(); return PARAMETER_END; }
    {ARGUMENT_TOKEN} { return ARGUMENT_TOKEN; }
}

// scripted variable reference rules (in references)

<IN_SCRIPTED_VARIABLE_REFERENCE>{
    "$" { beginNextState(); return PARAMETER_END; }
    "|" { yybegin(IN_PARAMETER_ARGUMENT); return PIPE; }
    {SCRIPTED_VARIABLE_TOKEN} { return SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
}

// localisation command rules

<CHECK_COMMAND> {
    {COMMAND_CHECK} { return checkCommand(); }
}
<IN_COMMAND>{
    . {
                if(yycharat(0) == '\'' && ParadoxSyntaxConstraint.LocalisationConceptCommand.supports(this)) {
                    yybegin(IN_CONCEPT_NAME);
                    return LEFT_SINGLE_QUOTE;
                }
                yypushback(1);
                yybegin(IN_COMMAND_TEXT);
            }
}
<IN_COMMAND_TEXT>{
    "§" { yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextState(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }

    "]" { beginNextState(); return RIGHT_BRACKET; }
    "|" { yybegin(IN_COMMAND_ARGUMENT); return PIPE; }
    {COMMAND_TEXT_TOKEN} { return COMMAND_TEXT_TOKEN; }
}
<IN_COMMAND_ARGUMENT>{
    "§" { yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextState(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }

    "]" { beginNextState(); return RIGHT_BRACKET; }
    {ARGUMENT_TOKEN} { return ARGUMENT_TOKEN; }
}

// localisation icon rules

<CHECK_ICON>{
    {ICON_CHECK} { return checkIcon(); }
}
<IN_ICON>{
    "§" { yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextState(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }

    "£" { beginNextState(); return ICON_END; }
    "|" { yybegin(IN_ICON_ARGUMENT); return PIPE; }
    {ICON_TOKEN} { return ICON_TOKEN; }
}
<IN_ICON_ARGUMENT>{
    "§" { yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextState(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }

    "£" { beginNextState(); return ICON_END; }
    {ARGUMENT_TOKEN} { return ARGUMENT_TOKEN; }
}

// [stellaris] localisation concept command rules (as special command rules)

<IN_CONCEPT_NAME> {
    "§" { yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextState(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }

    "]" { beginNextState(); return RIGHT_BRACKET; }
    "'" { return RIGHT_SINGLE_QUOTE; }
    "," { yybegin(IN_CONCEPT_BLANK); return COMMA; }
    {CONCEPT_NAME_TOKEN} { return CONCEPT_NAME_TOKEN; }
}
<IN_CONCEPT_BLANK> {
    "§!" { beginNextState(); return COLORFUL_TEXT_END; }

    {BLANK} { setNextState(IN_CONCEPT_TEXT); yybegin(IN_CONCEPT_TEXT); return WHITE_SPACE; }
    // there may not be a whitespace after COMMA (and such situation will be treat as valid)
    [^] { yypushback(yylength()); setNextState(IN_CONCEPT_TEXT); yybegin(IN_CONCEPT_TEXT); }
}

// [ck3, vic3] localisation text format rules

<CHECK_TEXT_FORMAT>{
    {TEXT_FORMAT_CHECK} { return checkTextFormat(); }
}
<IN_TEXT_FORMAT_ID> {
    "§" { yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextState(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yylength()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }
    "[" { setNextState(yylength()); yypushback(yylength()); yybegin(CHECK_COMMAND); }

    "#" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_TEXT_FORMAT); }
    "#!" { beginNextState(); return TEXT_FORMAT_END; }
    {TEXT_FORMAT_TOKEN} { return TEXT_FORMAT_TOKEN; }

    {BLANK} { setNextState(IN_TEXT_FORMAT_TEXT); yybegin(IN_TEXT_FORMAT_TEXT); return WHITE_SPACE; }
    // there may not be a whitespace after TEXT_FORMAT_TOKEN (and such situation should be invalid)
    [^] { yypushback(yylength()); setNextState(IN_TEXT_FORMAT_TEXT); yybegin(IN_TEXT_FORMAT_TEXT); }
}

// [ck3, vic3] localisation text icon rules

<CHECK_TEXT_ICON>{
    {TEXT_ICON_CHECK} { return checkTextIcon(); }
}
<IN_TEXT_ICON>{
    "§" { yypushback(yylength()); yybegin(CHECK_COLORFUL_TEXT); }
    "§!" { beginNextState(); return COLORFUL_TEXT_END; }
    "$" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_REFERENCE); }
    "[" { setNextState(yystate()); yypushback(yylength()); yybegin(CHECK_COMMAND); }

    "!" { beginNextState(); return TEXT_ICON_END; }
    {TEXT_ICON_TOKEN} { return TEXT_ICON_TOKEN; }
}

[^] { return BAD_CHARACTER; }
