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

%unicode

Blank = \s+

IntNumberToken = [0-9]+ // leading zero is allowed
FloatNumberToken = [0-9]*\.[0-9]+ // leading zero is allowed

IdentifierChar = [A-Za-z0-9_]
IdentifierLeadChar = [A-Za-z_] // leading number is not allowed
IdentifierToken = {IdentifierLeadChar}{IdentifierChar}* // leading number is not allowed

ParameterToken = {IdentifierToken} // identifier

ArgumentChar = [^# = <>!?{}\\\s$\[\]] // `@` is allowed
ArgumentToken = {ArgumentChar}+ // compatible with leading '@'

ScriptedVariableToken = {IdentifierToken} // identifier

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
    "@" { return AT; } // leading `@` is allowed here at syntax level (but not valid in actual)
    {IntNumberToken} { return INT_NUMBER_TOKEN; }
    {FloatNumberToken} { return FLOAT_NUMBER_TOKEN; }
    {ScriptedVariableToken} { return SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
    {Blank} { return WHITE_SPACE; }
}

<IN_PARAMETER, IN_PARAMETER_ARGUMENT> {
    "$" { yybegin(YYINITIAL); return PARAMETER_END; }
}
<IN_PARAMETER> {
    "|" { yybegin(IN_PARAMETER_ARGUMENT); return PIPE; }
    {ParameterToken} { return PARAMETER_TOKEN; }
    {Blank} { yybegin(YYINITIAL); return WHITE_SPACE; }
}
<IN_PARAMETER_ARGUMENT> {
    {ArgumentToken} { return ARGUMENT_TOKEN; }
    {Blank} { yybegin(YYINITIAL); return WHITE_SPACE; }
}

[^] { return BAD_CHARACTER; }
