package icu.windea.pls.gameTypes.stellaris;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static icu.windea.pls.gameTypes.stellaris.StellarisFormatStringElementTypes.*;

%%

%{
  public StellarisFormatStringLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class StellarisFormatStringLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%state WAITING_FORMAT_REFERENCE

EOL=\R
WHITE_SPACE=\s+

STRING_TOKEN=[^\r\n<>]+
FORMAT_REFERENCE_TOKEN=[a-zA-Z0-9_]+

%%

<YYINITIAL> {
  "<" { yybegin(WAITING_FORMAT_REFERENCE); return LEFT_ANGLE_BRACKET; }
  ">" { return RIGHT_ANGLE_BRACKET; }
  {STRING_TOKEN} { return STRING_TOKEN; }
}
<WAITING_FORMAT_REFERENCE> {
  "<" { return LEFT_ANGLE_BRACKET; }
  ">" { yybegin(YYINITIAL); return RIGHT_ANGLE_BRACKET; }
  {FORMAT_REFERENCE_TOKEN} { return FORMAT_REFERENCE_TOKEN; }
}

[^] { return BAD_CHARACTER; }
