package icu.windea.pls.localisation.lexer;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.model.ParadoxGameType;
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.core.StdlibExtensionsKt.*;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;

%%

%{
    public _ParadoxLocalisationLexer() {
        this((java.io.Reader)null);
    }

    private IElementType checkRightQuote() {
        // double quote should be threat as string if it's not the last one of current line
        try {
            int i = zzCurrentPos + 1;
            int length = zzBuffer.length();
            while(i < length) {
                char c = zzBuffer.charAt(i);
				if(c == '\n' || c == '\r') break;
                if(c == '"') return STRING_TOKEN;
                i++;
            }
        } catch(Exception e) {
            // ignored
        }

        yybegin(IN_PROPERTY_END);
	    return RIGHT_QUOTE;
    }
%}

%public
%class _ParadoxLocalisationLexer
%implements FlexLexer
%function advance
%type IElementType

%s IN_LOCALE_COLON
%s IN_LOCALE_END
%s IN_PROPERTY_COLON
%s IN_PROPERTY_NUMBER
%s IN_PROPERTY_VALUE
%s IN_PROPERTY_END
%s CHECK_RIGHT_QUOTE

%unicode

EOL=\s*\R
BLANK=\s+
WHITE_SPACE=[\s&&[^\r\n]]+
COMMENT=#[^\r\n]*

LOCALE_TOKEN=[a-z_]+
PROPERTY_NUMBER=\d+
PROPERTY_KEY_TOKEN=[a-zA-Z0-9_.\-']+
PROPERTY_VALUE_TOKEN=[^\"\r\n]+ // it's unnecessary to escape double quotes in loc text in fact

%%

// core rules

<YYINITIAL> {
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }

    // 本地化文件中可以没有，或者有多个locale - 主要是为了兼容localisation/languages.yml
    // locale之前必须没有任何缩进
    // locale之后的冒号和换行符之间应当没有任何字符或者只有空白字符
    // 采用最简单的实现方式，尽管JFlex手册中说 "^" "$" 性能不佳
    ^ {LOCALE_TOKEN} ":" \s* $ {
        int n = 1;
        int l = yylength();
        while(Character.isWhitespace(yycharat(l - n))) {
            n++;
        }
        yypushback(n);
        yybegin(IN_LOCALE_COLON);
        return LOCALE_TOKEN;
    }
    {PROPERTY_KEY_TOKEN} { yybegin(IN_PROPERTY_COLON); return PROPERTY_KEY_TOKEN; }
}
<IN_LOCALE_COLON>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    ":" { yybegin(IN_LOCALE_END); return COLON; }
}
<IN_LOCALE_END>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
}
<IN_PROPERTY_COLON>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    ":" { yybegin(IN_PROPERTY_NUMBER); return COLON; }
}
<IN_PROPERTY_NUMBER>{
    {WHITE_SPACE} { yybegin(IN_PROPERTY_VALUE); return WHITE_SPACE; }
    {PROPERTY_NUMBER} { yybegin(IN_PROPERTY_VALUE); return PROPERTY_NUMBER; }
    \" { yybegin(IN_RICH_TEXT); return LEFT_QUOTE; }
}
<IN_PROPERTY_VALUE> {
    {WHITE_SPACE} { return WHITE_SPACE; }
    \" { yybegin(IN_RICH_TEXT); return LEFT_QUOTE; }
}
<IN_PROPERTY_END>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
    \" { return checkRightQuote(); }
}

{EOL} { yybegin(YYINITIAL); return WHITE_SPACE; }

[^] { return BAD_CHARACTER; }
