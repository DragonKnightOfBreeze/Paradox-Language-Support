package icu.windea.pls.expression.psi;


import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.core.StdlibExtensionsKt.*;
import static icu.windea.pls.expression.psi.ParadoxExpressionElementTypes.*;

%%

%public
%class ParadoxExpressionLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%state WAITING_EXPRESSION_TOKEN

%{	
    public ParadoxExpressionLexer() {
        this((java.io.Reader)null);
    }
%}

EOL=\s*\R
WHITE_SPACE=[\s&&[^\r\n]]+
EXPRESSION_PREFIX=\w+:
EXPRESSION_TOKEN=.+

%%

//core rules

<YYINITIAL> {
  {EOL} {return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE; }
  {EXPRESSION_PREFIX} { yybegin(WAITING_EXPRESSION_TOKEN); return EXPRESSION_PREFIX; }
}
<WAITING_EXPRESSION_TOKEN> {
  {EOL} {return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE; }
  {EXPRESSION_TOKEN} { return EXPRESSION_TOKEN; }
}

[^] {return BAD_CHARACTER; }
