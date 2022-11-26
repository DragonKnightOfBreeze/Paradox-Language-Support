package icu.windea.pls.expression;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static icu.windea.pls.expression.psi.ParadoxExpressionElementTypes.*;

%%

%{
  public _ParadoxExpressionLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class ParadoxExpressionLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

PREFIX_TOKEN=[a-zA-Z_:]
IDENTIFIER_TOKEN=[a-zA-Z_]
BOOLEAN_TOKEN=(yes|no)
INT_TOKEN=[+-]?(0|[1-9][0-9]*)
FLOAT_TOKEN=[+-]?(0|[1-9][0-9]*)(\.[0-9]+)
STRING_TOKEN=[^|#@$={}\[\]\s\\"][^|$={}\[\]\s\\"]*

%%
<YYINITIAL> {
  {WHITE_SPACE}           { return WHITE_SPACE; }

  "."                     { return DOT; }
  "@"                     { return AT; }
  "|"                     { return PIPE; }

  {PREFIX_TOKEN}          { return PREFIX_TOKEN; }
  {IDENTIFIER_TOKEN}      { return IDENTIFIER_TOKEN; }
  {BOOLEAN_TOKEN}         { return BOOLEAN_TOKEN; }
  {INT_TOKEN}             { return INT_TOKEN; }
  {FLOAT_TOKEN}           { return FLOAT_TOKEN; }
  {STRING_TOKEN}          { return STRING_TOKEN; }

}

[^] { return BAD_CHARACTER; }
