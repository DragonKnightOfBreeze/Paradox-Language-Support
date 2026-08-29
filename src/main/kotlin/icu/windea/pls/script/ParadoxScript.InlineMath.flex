package icu.windea.pls.script.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import java.util.*;
import java.util.concurrent.atomic.*;
import icu.windea.pls.model.ParadoxGameType;
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;

// Lexer for inline math of Paradox Script.

%%

%{
    // state for abs operator signs (LABS_SIGN or RABS_SIGN)
    private boolean absSignState = true;

    public _ParadoxScriptInlineMathLexer() {
        this((java.io.Reader)null);
    }
%}

%public
%class _ParadoxScriptInlineMathLexer
%implements FlexLexer
%function advance
%type IElementType

%s IN_PARAMETER
%s IN_PARAMETER_ARGUMENT
%s IN_PARAMETER_ARGUMENT_END

%unicode

BLANK=\s+

INT_NUMBER_TOKEN=[0-9]+ // leading zero is allowed
FLOAT_NUMBER_TOKEN=[0-9]*\.[0-9]+ // leading zero is allowed

IDENTIFIER_CHAR=[A-Za-z0-9_]
IDENTIFIER_LEAD_CHAR=[A-Za-z_] // leading number is not allowed
IDENTIFIER_TOKEN={IDENTIFIER_LEAD_CHAR}{IDENTIFIER_CHAR}* // leading number is not allowed

ARGUMENT_CHAR=[^#$=<>!?{}\[\]\\\s] // compatible with leading '@'
ARGUMENT_TOKEN={ARGUMENT_CHAR}+

PARAMETER_CHAR={IDENTIFIER_CHAR}|[.\-'] // `-'` is allowed additionally
PARAMETER_LEAD_CHAR={IDENTIFIER_LEAD_CHAR}|[.\-'] // `-'` is allowed additionally
PARAMETER_TOKEN={PARAMETER_LEAD_CHAR}{PARAMETER_CHAR}* // leading number is not allowed

PARAMETER_TOKEN={IDENTIFIER_TOKEN} // identifier

SCRIPTED_VARIABLE_TOKEN={IDENTIFIER_TOKEN} // identifier

%%

<YYINITIAL> {
    "|" {
        if (absSignState) {
            absSignState = false;
            return LABS_SIGN;
        } else {
            absSignState = true;
            return RABS_SIGN;
        }
    }
    "(" { return LP_SIGN; }
    ")" { return RP_SIGN; }
    "+" { return PLUS_SIGN; }
    "-" { return MINUS_SIGN; }
    "*" { return TIMES_SIGN; }
    "/" { return DIV_SIGN; }
    "%" { return MOD_SIGN; }
    "$" { yybegin(IN_PARAMETER); return PARAMETER_START; }
    "@" { return AT; } // leading `@` is allowed (on syntax level)
    {INT_NUMBER_TOKEN} { return INT_NUMBER_TOKEN; }
    {FLOAT_NUMBER_TOKEN} { return FLOAT_NUMBER_TOKEN; }
    {SCRIPTED_VARIABLE_TOKEN} { return SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
    {BLANK} { return WHITE_SPACE; }
}
<IN_PARAMETER> {
    "|" { yybegin(IN_PARAMETER_ARGUMENT); return PIPE; }
    "$" { yybegin(YYINITIAL); return PARAMETER_END; }
    {PARAMETER_TOKEN} { return PARAMETER_TOKEN; }
    {BLANK} { yybegin(YYINITIAL); return WHITE_SPACE; }
}
<IN_PARAMETER_ARGUMENT> {
    "$" { yybegin(YYINITIAL); return PARAMETER_END; }
    {ARGUMENT_TOKEN} { yybegin(IN_PARAMETER_ARGUMENT_END); return ARGUMENT_TOKEN; }
    {BLANK} { yybegin(YYINITIAL); return WHITE_SPACE; }
}
<IN_PARAMETER_ARGUMENT_END> {
    "$" { yybegin(YYINITIAL); return PARAMETER_END; }
    {BLANK} { yybegin(YYINITIAL); return WHITE_SPACE; }
}

[^] { return BAD_CHARACTER; }
