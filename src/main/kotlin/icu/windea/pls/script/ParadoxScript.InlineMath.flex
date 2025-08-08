package icu.windea.pls.script.lexer;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import java.util.*;
import java.util.concurrent.atomic.*;
import icu.windea.pls.model.ParadoxGameType;
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;

%%

%{
    private boolean leftAbsSign = true;

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

INT_NUMBER_TOKEN=[0-9]+ // leading zero is permitted
FLOAT_NUMBER_TOKEN=[0-9]*(\.[0-9]+) // leading zero is permitted
SCRIPTED_VARIABLE_NAME_TOKEN=[a-zA-Z0-9_]+ // leading number is not permitted
PARAMETER_TOKEN=[a-zA-Z_][a-zA-Z0-9_]* // leading number is not permitted
ARGUMENT_TOKEN=[^#$=<>?{}\[\]\s]+ // compatible with leading '@'

%%

<YYINITIAL> {
    "|" {
        if (leftAbsSign) {
            leftAbsSign = false;
            return LABS_SIGN;
        } else {
            leftAbsSign = true;
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
    {INT_NUMBER_TOKEN} { return INT_NUMBER_TOKEN; }
    {FLOAT_NUMBER_TOKEN} { return FLOAT_NUMBER_TOKEN; }
    {SCRIPTED_VARIABLE_NAME_TOKEN} { return INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
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
