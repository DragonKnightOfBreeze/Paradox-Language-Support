package icu.windea.pls.localisation.psi;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*;

%%

%public
%class ParadoxLocalisationLexer
%implements com.intellij.lexer.FlexLexer
%function advance
%type IElementType
%unicode

%state WAITING_LOCALE_COLON
%state WAITING_LOCALE_EOL
%state WAITING_PROPERTY_KEY
%state WAITING_PROPERTY_COLON
%state WAITING_PROPERTY_NUMBER
%state WAITING_PROPERTY_VALUE
%state WAITING_PROPERTY_EOL
%state WAITING_RICH_TEXT
%state WAITING_PROPERTY_REFERENCE
%state WAITING_PROPERTY_REFERENCE_PARAMETER
%state WAITING_ICON
%state WAITING_ICON_NAME_FINISHED
%state WAITING_ICON_PARAMETER
%state WAITING_SEQUENTIAL_NUMBER
%state WAITING_COMMAND_SCOPE_OR_FIELD
%state WAITING_COMMAND_SEPARATOR
%state WAITING_COLOR_ID
%state WAITING_COLORFUL_TEXT

%state WAITING_CHECK_PROPERTY_REFERENCE_START
%state WAITING_CHECK_ICON_START
%state WAITING_CHECK_SEQUENTIAL_NUMBER_START
%state WAITING_CHECK_COMMAND_START
%state WAITING_CHECK_COLORFUL_TEXT_START
%state WAITING_CHECK_RIGHT_QUOTE

%{
  private int depth = 0;
  private int commandLocation = 0;
  private int propertyReferenceLocation = 0;
  private boolean inIconName = false;

  public void increaseDepth(){
	  depth++;
  }
  
  public void decreaseDepth(){
	  if(depth > 0) depth--;
  }
  
  public int nextStateForText(){
    return depth <= 0 ? WAITING_RICH_TEXT : WAITING_COLORFUL_TEXT;
  }
  
  public int nextStateForCommand(){
    if(commandLocation == 0) return nextStateForText();
    else if (commandLocation == 1) return WAITING_PROPERTY_REFERENCE;
    else if (commandLocation == 2) return WAITING_ICON;
    else return nextStateForText();
  }

  public int nextStateForPropertyReference(){
    if(propertyReferenceLocation == 0) return nextStateForText();
    else if (propertyReferenceLocation == 2) return WAITING_ICON;
    else if (propertyReferenceLocation == 3) return WAITING_COMMAND_SCOPE_OR_FIELD;
    else return nextStateForText();
  }
  
  public boolean isLetter(char c){
	  return ('a' <= c && 'z' >= c) || ('A' <= c && 'Z' >= c);
  }
  
  public boolean isDigit(char c){
	  return '0' <= c && '9' >= c;
  }
  
  public boolean isLetterOrDigitOrUnderline(char c){
	  return isLetter(c) || isDigit(c) || c == '_';
  }
  
  public boolean isBlankOrDoubleQuote(char c){
	  return Character.isWhitespace(c) || c == '"';
  }
%}

//Stellaris官方本地化文件中本身就存在语法解析错误，需要保证存在错误的情况下仍然会解析后续的本地化文本，草

EOL=\s*\R\s*
WHITE_SPACE=[ \t]+

COMMENT=#[^\r\n]*
ROOT_COMMENT=#[^\r\n]*
//行尾注释不能包含双引号，否则会有解析冲突
END_OF_LINE_COMMENT=#[^\"\r\n]*
NUMBER=\d+
LOCALE_ID=[a-z_]+
PROPERTY_KEY_ID=[a-zA-Z0-9_.\-']+
VALID_ESCAPE_TOKEN=\\[rnt\"$£§%\[]
INVALID_ESCAPE_TOKEN=\\.
DOUBLE_LEFT_BRACKET=\[\[
PROPERTY_REFERENCE_ID=[a-zA-Z0-9_.\-']+
PROPERTY_REFERENCE_PARAMETER=[a-zA-Z0-9+\-*%=\[.\]]+
ICON_ID=[a-zA-Z0-9\-_\\/]+
ICON_PARAMETER=[a-zA-Z0-9+\-*%=]+
SEQUENTIAL_NUMBER_ID=[a-zA-Z]
COMMAND_SCOPE_ID_WITH_SUFFIX=[a-zA-Z0-9_:@]+\.
COMMAND_SEPARATOR=\.
COMMAND_FIELD_ID_WITH_SUFFIX=[a-zA-Z0-9_:@]+\]
COLOR_ID=[a-zA-Z]
//双引号和百分号实际上不需要转义
STRING_TOKEN=[^\"%$£§\[\r\n\\]+
//[123
CHECK_PROPERTY_REFERENCE_START=\$[^\$\s\"]*.?
CHECK_ICON_START=£.?
CHECK_SEQUENTIAL_NUMBER_START=%.?.?
CHECK_COMMAND_START=\[[^\.\]\s\"]*.?
CHECK_COLORFUL_TEXT_START=§.?
CHECK_RIGHT_QUOTE=\"[^\"\r\n]*\"?

%%

//同一状态下的规则无法保证顺序

<YYINITIAL> {
  {EOL} {return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE; } //继续解析
  {ROOT_COMMENT} {return ROOT_COMMENT; }
  {LOCALE_ID} {yybegin(WAITING_LOCALE_COLON); return LOCALE_ID; }
  {PROPERTY_KEY_ID} {yybegin(WAITING_PROPERTY_COLON); return PROPERTY_KEY_ID; } //为了兼容快速定义功能
}

<WAITING_LOCALE_COLON>{
  {WHITE_SPACE} {return WHITE_SPACE; }
  ":" {yybegin(WAITING_LOCALE_EOL); return COLON; }
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
}
<WAITING_LOCALE_EOL>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE; }
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT; }
}

<WAITING_PROPERTY_KEY> {
  {EOL} {return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE; }
  {COMMENT} {return COMMENT; }
  {PROPERTY_KEY_ID} {yybegin(WAITING_PROPERTY_COLON); return PROPERTY_KEY_ID; }
}
<WAITING_PROPERTY_COLON>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE;}
  ":" {yybegin(WAITING_PROPERTY_NUMBER); return COLON; }
}
<WAITING_PROPERTY_NUMBER>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {yybegin(WAITING_PROPERTY_VALUE); return WHITE_SPACE;}
  {NUMBER} {yybegin(WAITING_PROPERTY_VALUE); return NUMBER;}
}
<WAITING_PROPERTY_VALUE> {
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE;}
  \" {yybegin(WAITING_RICH_TEXT); return LEFT_QUOTE; }
}

<WAITING_RICH_TEXT>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {VALID_ESCAPE_TOKEN} {return VALID_ESCAPE_TOKEN;}
  {INVALID_ESCAPE_TOKEN} {return INVALID_ESCAPE_TOKEN;}
  {DOUBLE_LEFT_BRACKET} {return DOUBLE_LEFT_BRACKET;}
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "$" {propertyReferenceLocation=0; yypushback(yylength()); yybegin(WAITING_CHECK_PROPERTY_REFERENCE_START);}
  "£" {yypushback(yylength()); yybegin(WAITING_CHECK_ICON_START);}
  "%" {yypushback(yylength()); yybegin(WAITING_CHECK_SEQUENTIAL_NUMBER_START);}
  "[" {commandLocation=0; yypushback(yylength()); yybegin(WAITING_CHECK_COMMAND_START);}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;} //允许多余的重置颜色标记
  {STRING_TOKEN} {return STRING_TOKEN;}
}

<WAITING_PROPERTY_REFERENCE>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; } 
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE;}
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "$" {yybegin(nextStateForPropertyReference()); return PROPERTY_REFERENCE_END;}
  "[" {commandLocation=1; yypushback(yylength()); yybegin(WAITING_CHECK_COMMAND_START);}
  "|" {yybegin(WAITING_PROPERTY_REFERENCE_PARAMETER); return PARAMETER_SEPARATOR;}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {PROPERTY_REFERENCE_ID} {return PROPERTY_REFERENCE_ID;}
}
<WAITING_PROPERTY_REFERENCE_PARAMETER>{
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; } 
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "$" {yybegin(nextStateForPropertyReference()); return PROPERTY_REFERENCE_END;}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {PROPERTY_REFERENCE_PARAMETER} {return PROPERTY_REFERENCE_PARAMETER;}
}

<WAITING_ICON>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "£" {yybegin(nextStateForText()); return ICON_END;}
  "$" {propertyReferenceLocation=2; yypushback(yylength()); yybegin(WAITING_CHECK_PROPERTY_REFERENCE_START);}
  "[" {commandLocation=2; yypushback(yylength()); yybegin(WAITING_CHECK_COMMAND_START);}
  "|" {yybegin(WAITING_ICON_PARAMETER); return PARAMETER_SEPARATOR;}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {ICON_ID} {yybegin(WAITING_ICON_NAME_FINISHED); return ICON_ID;}
}
<WAITING_ICON_NAME_FINISHED>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "£" {yybegin(nextStateForText()); return ICON_END;}
  "$" {propertyReferenceLocation=2; yypushback(yylength()); yybegin(WAITING_CHECK_PROPERTY_REFERENCE_START);}
  "[" {commandLocation=2; yypushback(yylength()); yybegin(WAITING_CHECK_COMMAND_START);}
  "|" {yybegin(WAITING_ICON_PARAMETER); return PARAMETER_SEPARATOR;}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
}
<WAITING_ICON_PARAMETER>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "$" {propertyReferenceLocation=2; yypushback(yylength()); yybegin(WAITING_CHECK_PROPERTY_REFERENCE_START);}
  "£" {yybegin(nextStateForText()); return ICON_END;}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {ICON_PARAMETER} {return ICON_PARAMETER;}
}

<WAITING_SEQUENTIAL_NUMBER>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "%" {yybegin(nextStateForText()); return SEQUENTIAL_NUMBER_END;}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {SEQUENTIAL_NUMBER_ID} {return SEQUENTIAL_NUMBER_ID;}
}

<WAITING_COMMAND_SCOPE_OR_FIELD>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "]" {yybegin(nextStateForCommand()); return COMMAND_END;}
  "$" {propertyReferenceLocation=3; yypushback(yylength()); yybegin(WAITING_CHECK_PROPERTY_REFERENCE_START);}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {COMMAND_SCOPE_ID_WITH_SUFFIX} {yypushback(1); yybegin(WAITING_COMMAND_SEPARATOR); return COMMAND_SCOPE_ID;}
  {COMMAND_FIELD_ID_WITH_SUFFIX} {yypushback(1); yybegin(WAITING_COMMAND_SEPARATOR); return COMMAND_FIELD_ID;}
}
<WAITING_COMMAND_SEPARATOR>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "]" {yybegin(nextStateForCommand()); return COMMAND_END;}
  "$" {propertyReferenceLocation=3; yypushback(yylength()); yybegin(WAITING_CHECK_PROPERTY_REFERENCE_START);}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {COMMAND_SEPARATOR} {yybegin(WAITING_COMMAND_SCOPE_OR_FIELD); return COMMAND_SEPARATOR;}
}

<WAITING_COLOR_ID>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {yybegin(WAITING_COLORFUL_TEXT); return WHITE_SPACE; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {COLOR_ID} {yybegin(WAITING_COLORFUL_TEXT); return COLOR_ID;}
  [^] {yypushback(yylength()); yybegin(WAITING_COLORFUL_TEXT); } //提高兼容性
}
<WAITING_COLORFUL_TEXT>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {VALID_ESCAPE_TOKEN} {return VALID_ESCAPE_TOKEN;}
  {INVALID_ESCAPE_TOKEN} {return INVALID_ESCAPE_TOKEN;}
  {DOUBLE_LEFT_BRACKET} {return DOUBLE_LEFT_BRACKET;}
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "$" {propertyReferenceLocation=0; yypushback(yylength()); yybegin(WAITING_CHECK_PROPERTY_REFERENCE_START);}
  "£" {yypushback(yylength()); yybegin(WAITING_CHECK_ICON_START);}
  "%" {yypushback(yylength()); yybegin(WAITING_CHECK_SEQUENTIAL_NUMBER_START);}
  "[" {commandLocation=0; yypushback(yylength()); yybegin(WAITING_CHECK_COMMAND_START);}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {STRING_TOKEN} {return STRING_TOKEN;}
}

<WAITING_PROPERTY_EOL>{
  {EOL} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE; } //继续解析
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
}

<WAITING_CHECK_PROPERTY_REFERENCE_START>{
  {CHECK_PROPERTY_REFERENCE_START} {
    //特殊处理
    //如果匹配到的字符串长度大于1，且最后一个字符不为空白或双引号，则认为代表命令的开始
    //否则认为是常规字符串
    boolean isPropertyReferenceStart = yylength() > 1 && !isBlankOrDoubleQuote(yycharat(yylength()-1));
	yypushback(yylength()-1);
	if(isPropertyReferenceStart){
		yybegin(WAITING_PROPERTY_REFERENCE);
		return PROPERTY_REFERENCE_START;
	}else{
		yybegin(nextStateForText());
		return STRING_TOKEN;
	}
  }
}

<WAITING_CHECK_ICON_START>{
  {CHECK_ICON_START} {
    //特殊处理
    //如果匹配到的字符串的第2个字符存在且为字母、数字或下划线，则认为代表图标的开始
    //否则认为是常规字符串
    boolean isIconStart = yylength() == 2 && isLetterOrDigitOrUnderline(yycharat(1));
    yypushback(yylength()-1);
    if(isIconStart){
    	  yybegin(WAITING_ICON);
    	  return ICON_START;
    }else{
        yybegin(nextStateForText());
        return STRING_TOKEN;
    }
  }
}
<WAITING_CHECK_SEQUENTIAL_NUMBER_START>{
  {CHECK_SEQUENTIAL_NUMBER_START} {
    //特殊处理
    //如果匹配的字符串的第3个字符存在且为百分号，则认为整个字符串代表一个编号
    //否则认为是常规字符串
    boolean isSequentialNumberStart = yylength() == 3 && yycharat(2) == '%';
    yypushback(yylength()-1);
    if(isSequentialNumberStart){
        yybegin(WAITING_SEQUENTIAL_NUMBER);
        return SEQUENTIAL_NUMBER_START;
    }else{
        yybegin(nextStateForText());
        return STRING_TOKEN;
    }
  }
}

<WAITING_CHECK_COMMAND_START>{
  {CHECK_COMMAND_START} {
    //特殊处理
    //除了可以通过连续的两个左方括号转义之外
    //如果匹配到的字符串长度大于1，且最后一个字符不为空白或双引号，则认为代表命令的开始
    //否则认为是常规字符串
    boolean isCommandStart = yylength() > 1 && !isBlankOrDoubleQuote(yycharat(yylength()-1));
    yypushback(yylength()-1);
    if(isCommandStart){
	    yybegin(WAITING_COMMAND_SCOPE_OR_FIELD);
	    return COMMAND_START;
    } else {
	    yybegin(nextStateForText());
	    return STRING_TOKEN;
    }
  }
}

<WAITING_CHECK_COLORFUL_TEXT_START>{
  {CHECK_COLORFUL_TEXT_START} {
    //特殊处理
    //如果匹配到的字符串的第2个字符存在且为字母，则认为代表彩色文本的开始
    //否则认为是常规字符串
    boolean isColorfulTextStart = yylength() == 2 && isLetter(yycharat(1));
    yypushback(yylength()-1);
    if(isColorfulTextStart){
        yybegin(WAITING_COLOR_ID);
        increaseDepth();
        return COLORFUL_TEXT_START;
    }else{
        yybegin(nextStateForText());
        return STRING_TOKEN;
    }
  }
}
<WAITING_CHECK_RIGHT_QUOTE>{
  {CHECK_RIGHT_QUOTE} {
    //特殊处理
    //如果匹配到的字符串长度为1，或者最后一个字符不是双引号，则认为代表本地化富文本的结束
    //否则认为是常规字符串
    boolean isRightQuote = yylength() == 1 || yycharat(yylength()-1) != '"';
    yypushback(yylength()-1);
    if(isRightQuote){
        yybegin(WAITING_PROPERTY_EOL);
        return RIGHT_QUOTE;
    }else{
        yybegin(nextStateForText());
        return STRING_TOKEN;
    }
  }
}

[^] {return TokenType.BAD_CHARACTER; }
