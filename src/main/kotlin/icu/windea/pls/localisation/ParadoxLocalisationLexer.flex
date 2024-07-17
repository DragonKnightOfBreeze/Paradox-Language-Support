package icu.windea.pls.localisation.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.core.StdlibExtensionsKt.*;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;

%%

%{    
    private int depth = 0;
    private boolean inConceptText = false;
    private CommandLocation commandLocation = CommandLocation.NORMAL;
    private ReferenceLocation referenceLocation = ReferenceLocation.NORMAL;
    
    public _ParadoxLocalisationLexer() {
        this((java.io.Reader)null);
    }
    
    private void increaseDepth(){
        depth++;
    }
    
    private void decreaseDepth(){
        if(depth > 0) depth--;
    }
    
    private int nextStateForText(){
      return depth <= 0 ? IN_RICH_TEXT : IN_COLORFUL_TEXT;
    }
    
    private enum CommandLocation {
        NORMAL, REFERENCE, ICON;
    }
    
    private int nextStateForCommand(){
        return switch(commandLocation) {
            case NORMAL -> nextStateForText();
            case REFERENCE -> IN_PROPERTY_REFERENCE;
            case ICON -> IN_ICON;
        };
    }
    
    private enum ReferenceLocation {
        NORMAL, ICON, ICON_FRAME, COMMAND, CONCEPT_NAME;
    }
    
    private int nextStateForPropertyReference(){
        return switch(referenceLocation) {
            case NORMAL -> nextStateForText();
            case ICON -> IN_ICON_ID_FINISHED;
            case ICON_FRAME -> IN_ICON_FRAME_FINISHED;
            case COMMAND -> IN_COMMAND_SCOPE_OR_FIELD;
			case CONCEPT_NAME -> IN_CONCEPT;
        };
    }
    
    private boolean isReferenceStart(){
        if(yylength() <= 1) return false;
        return true;
    }
    
    private boolean isIconStart(){
        if(yylength() <= 1) return false;
        char c = yycharat(1);
        return isExactLetter(c) || isExactDigit(c) || c == '_' || c == '$';
    }
    
    private boolean isCommandStart(){
        if(yylength() <= 1) return false;
        return yycharat(yylength()-1) == ']';
    }
    
    private boolean isColorfulTextStart(){
        if(yylength() <= 1) return false;
        return isExactLetter(yycharat(1));
    }
    
    private IElementType checkRightQuote() {
        //NOTE double quote should be threat as a string if it's not the last one of current line
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
            //ignored
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
%s IN_RICH_TEXT
%s IN_PROPERTY_REFERENCE
%s IN_PROPERTY_REFERENCE_PARAMETER_TOKEN
%s IN_SCRIPTED_VARIABLE_REFERENCE_NAME
%s IN_ICON
%s IN_ICON_ID_FINISHED
%s IN_ICON_FRAME
%s IN_ICON_FRAME_FINISHED
%s IN_COMMAND
%s IN_COMMAND_SCOPE_OR_FIELD
%s IN_CONCEPT
%s IN_CONCEPT_TEXT
%s IN_COLOR_ID
%s IN_COLORFUL_TEXT

%s CHECK_PROPERTY_REFERENCE_START
%s CHECK_ICON_START
%s CHECK_COMMAND_START
%s IN_CHECK_COLORFUL_TEXT_START
%s CHECK_RIGHT_QUOTE

%unicode

EOL=\s*\R
BLANK=\s+
WHITE_SPACE=[\s&&[^\r\n]]+
COMMENT=#[^\r\n]*
END_OF_LINE_COMMENT=#[^\r\n]*

CHECK_PROPERTY_REFERENCE_START=\$([a-zA-Z0-9_.\-'@]?|{CHECK_COMMAND_START})
CHECK_ICON_START=£.?
CHECK_COLORFUL_TEXT_START=§.?

LOCALE_TOKEN=[a-z_]+
NUMBER=\d+
PROPERTY_KEY_CHAR=[a-zA-Z0-9_.\-']
PROPERTY_KEY_TOKEN={PROPERTY_KEY_CHAR}+
PROPERTY_REFERENCE_TOKEN={PROPERTY_KEY_CHAR}+
PROPERTY_REFERENCE_PARAMETER_TOKEN=[^\"$£§\[\r\n\\]+
SCRIPTED_VARIABLE_ID=[a-zA-Z_][a-zA-Z0-9_]*
ICON_TOKEN=[a-zA-Z0-9\-_\\/]+
ICON_FRAME=[1-9][0-9]* // positive integer
COLOR_TOKEN=[a-zA-Z0-9]
STRING_TOKEN=([^\"$£§\[\]\r\n\\]|\\.|\[\[)+  //it's unnecessary to escape double quotes in loc text in fact

CHECK_COMMAND_START=\[[^\r\n\]]*.?
COMMAND_SCOPE_ID_WITH_SUFFIX=[^\r\n.\[\]]+\.
COMMAND_FIELD_ID_WITH_SUFFIX=[^\r\n.\[\]]+\]
CONCEPT_NAME=[a-zA-Z0-9_:]+

%%

//core rules

<YYINITIAL> {
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; } //这里可以有注释
    ^ {LOCALE_TOKEN} ":" \s* $ {
        //本地化文件中可以没有，或者有多个locale - 主要是为了兼容localisation/languages.yml
        //locale之前必须没有任何缩进
        //locale之后的冒号和换行符之间应当没有任何字符或者只有空白字符
        //采用最简单的实现方式，尽管JFlex手册中说 "^" "$" 性能不佳
        int n = 1;
        int l = yylength();
        while(Character.isWhitespace(yycharat(l - n))) {
            n++;
        }
        yypushback(n);
        yybegin(IN_LOCALE_COLON);
        return LOCALE_TOKEN;
    }
    {PROPERTY_KEY_TOKEN} {
        yybegin(IN_PROPERTY_COLON);
        return PROPERTY_KEY_TOKEN;
    }
}
<IN_LOCALE_COLON>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    ":" { yybegin(IN_LOCALE_END); return COLON; }
}
<IN_LOCALE_END>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    {END_OF_LINE_COMMENT} { return COMMENT; }
}
<IN_PROPERTY_COLON>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    ":" { yybegin(IN_PROPERTY_NUMBER); return COLON; }
}
<IN_PROPERTY_NUMBER>{
    {WHITE_SPACE} { yybegin(IN_PROPERTY_VALUE); return WHITE_SPACE; }
    {NUMBER} { yybegin(IN_PROPERTY_VALUE); return PROPERTY_NUMBER; }
    \" { yybegin(IN_RICH_TEXT); return LEFT_QUOTE; }
}
<IN_PROPERTY_VALUE> {
    {WHITE_SPACE} { return WHITE_SPACE; }
    \" { yybegin(IN_RICH_TEXT); return LEFT_QUOTE; }
}
<IN_RICH_TEXT>{
    \" { return checkRightQuote(); }
    "$" { referenceLocation=ReferenceLocation.NORMAL; yypushback(yylength()); yybegin(CHECK_PROPERTY_REFERENCE_START); }
    "£" { yypushback(yylength()); yybegin(CHECK_ICON_START); }
    "[" { increaseDepth(); commandLocation=CommandLocation.NORMAL; yybegin(IN_COMMAND); return COMMAND_START; }
    "§" { yypushback(yylength()); yybegin(IN_CHECK_COLORFUL_TEXT_START); }
    "§!" { decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END; } //允许多余的重置颜色标记
}

//reference rules

<CHECK_PROPERTY_REFERENCE_START>{
    {CHECK_PROPERTY_REFERENCE_START} {
        //特殊处理
        //如果匹配到的字符串长度大于1，且"$"后面的字符可以被识别为PROPERTY_REFERENCE_TOKEN或者command，或者是@，则认为代表属性引用的开始
        boolean isReferenceStart = isReferenceStart();
        yypushback(yylength()-1);
        if(isReferenceStart){
            yybegin(IN_PROPERTY_REFERENCE);
            return PROPERTY_REFERENCE_START;
        } else {
            yybegin(nextStateForText());
            return STRING_TOKEN;
        }
    }
}
<IN_PROPERTY_REFERENCE>{
    {WHITE_SPACE} { yybegin(nextStateForText()); return WHITE_SPACE; }
    \" { return checkRightQuote(); }
    "$" { yybegin(nextStateForPropertyReference()); return PROPERTY_REFERENCE_END; }
    "[" { increaseDepth(); commandLocation=CommandLocation.REFERENCE; yybegin(IN_COMMAND); return COMMAND_START; }
    "|" { yybegin(IN_PROPERTY_REFERENCE_PARAMETER_TOKEN); return PIPE; }
    "§" { yypushback(yylength()); yybegin(IN_CHECK_COLORFUL_TEXT_START); }
    "§!" { decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END; }
    "@" { yybegin(IN_SCRIPTED_VARIABLE_REFERENCE_NAME); return AT; }
    {PROPERTY_REFERENCE_TOKEN} { return PROPERTY_REFERENCE_TOKEN; }
}
<IN_PROPERTY_REFERENCE_PARAMETER_TOKEN>{
    {WHITE_SPACE} { yybegin(nextStateForText()); return WHITE_SPACE; } 
    \" { return checkRightQuote(); }
    "$" { yybegin(nextStateForPropertyReference()); return PROPERTY_REFERENCE_END; }
    "§" { yypushback(yylength()); yybegin(IN_CHECK_COLORFUL_TEXT_START); }
    "§!" { decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END; }
    {PROPERTY_REFERENCE_PARAMETER_TOKEN} { return PROPERTY_REFERENCE_PARAMETER_TOKEN; }
}
<IN_SCRIPTED_VARIABLE_REFERENCE_NAME>{
    {WHITE_SPACE} { yybegin(nextStateForText()); return WHITE_SPACE; }
    \" { return checkRightQuote(); }
    "$" { yybegin(nextStateForPropertyReference()); return PROPERTY_REFERENCE_END; }
    "[" { increaseDepth(); commandLocation=CommandLocation.REFERENCE; yybegin(IN_COMMAND); return COMMAND_START; }
    "|" { yybegin(IN_PROPERTY_REFERENCE_PARAMETER_TOKEN); return PIPE; }
    "§" { yypushback(yylength()); yybegin(IN_CHECK_COLORFUL_TEXT_START); }
    "§!" { decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END; }
    {SCRIPTED_VARIABLE_ID} { return SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
}

//icon rules

<CHECK_ICON_START>{
    {CHECK_ICON_START} {
        //特殊处理
        //如果匹配到的字符串的第2个字符存在且为字母、数字或下划线或者$，则认为代表图标的开始
        //否则认为是常规字符串
        boolean isIconStart = isIconStart();
        yypushback(yylength()-1);
        if(isIconStart){
            yybegin(IN_ICON);
            return ICON_START;
        }else{
            yybegin(nextStateForText());
            return STRING_TOKEN;
        }
    }
}
<IN_ICON>{
    {WHITE_SPACE} { yybegin(nextStateForText()); return WHITE_SPACE; }
    \" { return checkRightQuote(); }
    "£" { yybegin(nextStateForText()); return ICON_END; }
    "$" { referenceLocation=ReferenceLocation.ICON; yypushback(yylength()); yybegin(CHECK_PROPERTY_REFERENCE_START); }
    "[" { increaseDepth(); commandLocation=CommandLocation.ICON; yybegin(IN_COMMAND); return COMMAND_START; }
    "|" { yybegin(IN_ICON_FRAME); return PIPE; }
    "§" { yypushback(yylength()); yybegin(IN_CHECK_COLORFUL_TEXT_START); }
    "§!" { decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END; }
    {ICON_TOKEN} { yybegin(IN_ICON_ID_FINISHED); return ICON_TOKEN; }
}
<IN_ICON_ID_FINISHED>{
    {WHITE_SPACE} { yybegin(nextStateForText()); return WHITE_SPACE; }
    \" { return checkRightQuote(); }
    "£" { yybegin(nextStateForText()); return ICON_END; }
    "|" { yybegin(IN_ICON_FRAME); return PIPE; }
    "§" { yypushback(yylength()); yybegin(IN_CHECK_COLORFUL_TEXT_START); }
    "§!" { decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END; }
}
<IN_ICON_FRAME>{
    {WHITE_SPACE} { yybegin(nextStateForText()); return WHITE_SPACE; }
    \" { return checkRightQuote(); }
    "$" { referenceLocation=ReferenceLocation.ICON_FRAME; yypushback(yylength()); yybegin(CHECK_PROPERTY_REFERENCE_START); }
    "£" { yybegin(nextStateForText()); return ICON_END; }
    "§" { yypushback(yylength()); yybegin(IN_CHECK_COLORFUL_TEXT_START); }
    "§!" { decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END; }
    {ICON_FRAME} { yybegin(IN_ICON_FRAME_FINISHED); return ICON_FRAME; }
}
<IN_ICON_FRAME_FINISHED>{
    {WHITE_SPACE} { yybegin(nextStateForText()); return WHITE_SPACE; }
    \" { return checkRightQuote(); }
    "£" { yybegin(nextStateForText()); return ICON_END; }
    "§" { yypushback(yylength()); yybegin(IN_CHECK_COLORFUL_TEXT_START); }
    "§!" { decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END; }
}
 
//command rules
 
<IN_COMMAND>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    . {
        if(yycharat(0) == '\'') {
            yybegin(IN_CONCEPT);
            return LEFT_SINGLE_QUOTE;
        } else {
            yypushback(1);
            yybegin(IN_COMMAND_SCOPE_OR_FIELD);
        }
    }
}
<IN_COMMAND_SCOPE_OR_FIELD>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    \" { return checkRightQuote(); }
    "]" { decreaseDepth(); yybegin(nextStateForCommand()); return COMMAND_END; }
    "$" { referenceLocation=ReferenceLocation.COMMAND; yypushback(yylength()); yybegin(CHECK_PROPERTY_REFERENCE_START); }
    "§" { yypushback(yylength()); yybegin(IN_CHECK_COLORFUL_TEXT_START); }
    "§!" { decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END; }
    "." { yybegin(IN_COMMAND_SCOPE_OR_FIELD); return DOT; }
    {COMMAND_SCOPE_ID_WITH_SUFFIX} { yypushback(1); return COMMAND_SCOPE_TOKEN; }
    {COMMAND_FIELD_ID_WITH_SUFFIX} { yypushback(1); return COMMAND_FIELD_TOKEN; }
}
<IN_CONCEPT> {
    {WHITE_SPACE} { return WHITE_SPACE; }
    \" { return checkRightQuote(); }
    "]" { decreaseDepth(); yybegin(nextStateForCommand()); return COMMAND_END; }
    "$" { referenceLocation=ReferenceLocation.CONCEPT_NAME; yypushback(yylength()); yybegin(CHECK_PROPERTY_REFERENCE_START); }
    "§" { yypushback(yylength()); yybegin(IN_CHECK_COLORFUL_TEXT_START); }
    "§!" { decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END; }
    "'" { return RIGHT_SINGLE_QUOTE; }
    {CONCEPT_NAME} { return CONCEPT_NAME_TOKEN; }
    "," { inConceptText=true; yybegin(IN_CONCEPT_TEXT); return COMMA; }
}
<IN_CONCEPT_TEXT> {
    {WHITE_SPACE} { return WHITE_SPACE; }
    \" { return checkRightQuote(); }
    "]" { decreaseDepth(); yybegin(nextStateForCommand()); return COMMAND_END; }
    "$" { referenceLocation=ReferenceLocation.NORMAL; yypushback(yylength()); yybegin(CHECK_PROPERTY_REFERENCE_START); }
    "£" { yypushback(yylength()); yybegin(CHECK_ICON_START); }
    "[" { increaseDepth(); commandLocation=CommandLocation.NORMAL; yybegin(IN_COMMAND); return COMMAND_START; }
    "§" { yypushback(yylength()); yybegin(IN_CHECK_COLORFUL_TEXT_START); }
    "§!" { decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END; }
    [^] { yypushback(yylength()); yybegin(IN_RICH_TEXT); }
}

//colorful text rules

<IN_CHECK_COLORFUL_TEXT_START>{
    {CHECK_COLORFUL_TEXT_START} {
        //特殊处理
        //如果匹配到的字符串的第2个字符存在且为字母，则认为代表彩色文本的开始
        //否则认为是常规字符串
        boolean isColorfulTextStart = isColorfulTextStart();
        yypushback(yylength()-1);
        if(isColorfulTextStart) {
            yybegin(IN_COLOR_ID);
            increaseDepth();
            return COLORFUL_TEXT_START;
        } else {
            yybegin(nextStateForText());
            return STRING_TOKEN;
        }
    }
}
<IN_COLOR_ID>{
    {WHITE_SPACE} { yybegin(IN_COLORFUL_TEXT); return WHITE_SPACE; }
    \" { return checkRightQuote(); }
    "§" { yypushback(yylength()); yybegin(IN_CHECK_COLORFUL_TEXT_START); }
    "§!" { decreaseDepth(); decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END; }
    {COLOR_TOKEN} { yybegin(IN_COLORFUL_TEXT); return COLOR_TOKEN; }
    [^] { yypushback(yylength()); yybegin(IN_COLORFUL_TEXT); } //提高兼容性
}
<IN_COLORFUL_TEXT>{
    \" { return checkRightQuote(); }
    "$" { referenceLocation=ReferenceLocation.NORMAL; yypushback(yylength()); yybegin(CHECK_PROPERTY_REFERENCE_START); }
    "£" { yypushback(yylength()); yybegin(CHECK_ICON_START); }
    "[" { increaseDepth(); commandLocation=CommandLocation.NORMAL; yybegin(IN_COMMAND); return COMMAND_START; }
    "§" { yypushback(yylength()); yybegin(IN_CHECK_COLORFUL_TEXT_START); }
    "§!" { decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END; }
}

<IN_PROPERTY_END>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    {END_OF_LINE_COMMENT} { return COMMENT; }
    \" { return checkRightQuote(); }
}

<IN_RICH_TEXT, IN_COLORFUL_TEXT> {
    "]" {
        if(inConceptText) {
            inConceptText = false;
            decreaseDepth();
            yybegin(nextStateForCommand());
            return COMMAND_END;
        }
        return STRING_TOKEN;
    }
    {STRING_TOKEN} {
        return STRING_TOKEN;
    }
}

{EOL} { depth=0; inConceptText=false; yybegin(YYINITIAL); return WHITE_SPACE; }
[^] { return BAD_CHARACTER; }
