package icu.windea.pls.localisation.psi;

import com.intellij.openapi.project.*;import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;
import static icu.windea.pls.StdlibExtensionsKt.*;

%%

%public
%class ParadoxLocalisationLexer
%implements com.intellij.lexer.FlexLexer
%function advance
%type IElementType
%unicode

%state WAITING_LOCALE_COLON
%state WAITING_LOCALE_END
%state WAITING_PROPERTY_COLON
%state WAITING_PROPERTY_NUMBER
%state WAITING_PROPERTY_VALUE
%state WAITING_PROPERTY_END
%state WAITING_RICH_TEXT
%state WAITING_PROPERTY_REFERENCE
%state WAITING_PROPERTY_REFERENCE_PARAMETER_TOKEN
%state WAITING_ICON
%state WAITING_ICON_ID_FINISHED
%state WAITING_ICON_FRAME
%state WAITING_COMMAND_SCOPE_OR_FIELD
%state WAITING_COMMAND_SEPARATOR
%state WAITING_COLOR_ID
%state WAITING_COLORFUL_TEXT

%state WAITING_CHECK_PROPERTY_REFERENCE_START
%state WAITING_CHECK_ICON_START
%state WAITING_CHECK_COMMAND_START
%state WAITING_CHECK_COLORFUL_TEXT_START
%state WAITING_CHECK_RIGHT_QUOTE

%{
	public Project project;
		
    private boolean noIndent = true;
    private int depth = 0;
    private int commandLocation = 0;
    private int propertyReferenceLocation = 0;
    private boolean inIconName = false;
    
    public ParadoxLocalisationLexer(Project propect) {
        this((java.io.Reader)null);
        this.project = project;
    }
	
    private void increaseDepth(){
	    depth++;
    }
    
    private void decreaseDepth(){
	    if(depth > 0) depth--;
    }
    
    private int nextStateForText(){
      return depth <= 0 ? WAITING_RICH_TEXT : WAITING_COLORFUL_TEXT;
    }
    
    private int nextStateForCommand(){
      if(commandLocation == 0) return nextStateForText();
      else if (commandLocation == 1) return WAITING_PROPERTY_REFERENCE;
      else if (commandLocation == 2) return WAITING_ICON;
      else return nextStateForText();
    }
    
    private int nextStateForPropertyReference(){
      if(propertyReferenceLocation == 0) return nextStateForText();
      else if (propertyReferenceLocation == 2) return WAITING_ICON;
      else if (propertyReferenceLocation == 3) return WAITING_COMMAND_SCOPE_OR_FIELD;
      else return nextStateForText();
    }
    
    private boolean isPropertyReferenceStart(){
		  if(yylength() <= 1) return false;
	    char c = yycharat(yylength()-1);
	    return !Character.isWhitespace(c) && c != '"';
    }
    
    private boolean isIconStart(){
		  if(yylength() != 2) return false;
	    char c = yycharat(1);
	    return isExactLetter(c) || isExactDigit(c) || c == '_';
    }
    
    private boolean isCommandStart(){
		  if(yylength() <= 1) return false;
	    return yycharat(yylength()-1) == ']';
    }
    
    private boolean isColorfulTextStart(){
		  if(yylength() != 2) return false;
	    return isExactLetter(yycharat(1));
    }
    
    private boolean isRightQuote(){
		  if(yylength() == 1) return true;
	    return yycharat(yylength()-1) != '"';
    }
%}

//Stellaris官方本地化文件中本身就存在语法解析错误，需要保证存在错误的情况下仍然会解析后续的本地化文本，草

EOL=\s*\R
BLANK=\s+
WHITE_SPACE=[\s&&[^\r\n]]+

COMMENT=#[^\r\n]*
//行尾注释不能包含双引号，否则会有解析冲突
END_OF_LINE_COMMENT=#[^\"\r\n]*
NUMBER=\d+
//LOCALE_ID=[a-z_]+
PROPERTY_KEY_ID=[a-zA-Z0-9_.\-']+
VALID_ESCAPE_TOKEN=\\[rnt\"$£§%\[]
INVALID_ESCAPE_TOKEN=\\.
DOUBLE_LEFT_BRACKET=\[\[
PROPERTY_REFERENCE_ID=[a-zA-Z0-9_.\-']+
PROPERTY_REFERENCE_PARAMETER_TOKEN=[a-zA-Z0-9+\-*%=\[.\]]+
ICON_ID=[a-zA-Z0-9\-_\\/]+
ICON_FRAME=[1-9][0-9]* // positive integer
COMMAND_SCOPE_ID_WITH_SUFFIX=[a-zA-Z0-9_:@]+\.
COMMAND_FIELD_ID_WITH_SUFFIX=[a-zA-Z0-9_:@]+\]
COLOR_ID=[a-zA-Z]
//双引号和百分号实际上不需要转义
STRING_TOKEN=[^\"%$£§\[\r\n\\]+

CHECK_LOCALE_ID=[a-z_]+:\s*[\r\n]
CHECK_PROPERTY_REFERENCE_START=\$[^\$\s\"]*.?
CHECK_ICON_START=£.?
CHECK_COMMAND_START=\[[.a-zA-Z0-9_:@\s&&[^\r\n]]*.?
CHECK_COLORFUL_TEXT_START=§.?
CHECK_RIGHT_QUOTE=\"[^\"\r\n]*\"?

%%

//同一状态下的规则无法保证顺序

<YYINITIAL> {
  {EOL} {noIndent=true; return WHITE_SPACE; }
  {WHITE_SPACE} {noIndent=false; return WHITE_SPACE; } //继续解析
  {COMMENT} {return COMMENT; } //这里可以有注释
  {CHECK_LOCALE_ID} { //同一本地化文件中是可以有多个locale的，这是为了兼容localisation/languages.yml
	//locale应该在之后的冒号和换行符之间没有任何字符或者只有空白字符
    CharSequence text = yytext();
    int length = text.length();
    int i = length - 2;
    while(i >= 0){
 	    char c = text.charAt(i);
 	    if(c == ':') {
 		    int pushback = length - i;
			yypushback(pushback);
			//locale之前必须没有任何缩进
			if(noIndent){
 		        yybegin(WAITING_LOCALE_COLON);
 		        return LOCALE_ID;
			} else {
				yybegin(WAITING_PROPERTY_COLON);
				return PROPERTY_KEY_ID;
			}
 	    }
 	    i--;
    }
    return TokenType.BAD_CHARACTER; //不应该出现
  }
  //{LOCALE_ID} {yybegin(WAITING_LOCALE_COLON); return LOCALE_ID; }
  {PROPERTY_KEY_ID} {yybegin(WAITING_PROPERTY_COLON); return PROPERTY_KEY_ID; }
}
<WAITING_LOCALE_COLON>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE; }
  ":" {yybegin(WAITING_LOCALE_END); return COLON; }
}
<WAITING_LOCALE_END>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE; }
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT; }
}
<WAITING_PROPERTY_COLON>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE;}
  ":" {yybegin(WAITING_PROPERTY_NUMBER); return COLON; }
}
<WAITING_PROPERTY_NUMBER>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} {yybegin(WAITING_PROPERTY_VALUE); return WHITE_SPACE;}
  {NUMBER} {yybegin(WAITING_PROPERTY_VALUE); return PROPERTY_NUMBER;}
}
<WAITING_PROPERTY_VALUE> {
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE;}
  \" {yybegin(WAITING_RICH_TEXT); return LEFT_QUOTE; }
}

<WAITING_RICH_TEXT>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {VALID_ESCAPE_TOKEN} {return VALID_ESCAPE_TOKEN;}
  {INVALID_ESCAPE_TOKEN} {return INVALID_ESCAPE_TOKEN;}
  {DOUBLE_LEFT_BRACKET} {return DOUBLE_LEFT_BRACKET;}
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "$" {propertyReferenceLocation=0; yypushback(yylength()); yybegin(WAITING_CHECK_PROPERTY_REFERENCE_START);}
  "£" {yypushback(yylength()); yybegin(WAITING_CHECK_ICON_START);}
  "[" {commandLocation=0; yypushback(yylength()); yybegin(WAITING_CHECK_COMMAND_START);}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;} //允许多余的重置颜色标记
  {STRING_TOKEN} {return STRING_TOKEN;}
}

<WAITING_PROPERTY_REFERENCE>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; } 
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE;}
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "$" {yybegin(nextStateForPropertyReference()); return PROPERTY_REFERENCE_END;}
  "[" {commandLocation=1; yypushback(yylength()); yybegin(WAITING_CHECK_COMMAND_START);}
  "|" {yybegin(WAITING_PROPERTY_REFERENCE_PARAMETER_TOKEN); return PIPE;}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {PROPERTY_REFERENCE_ID} {return PROPERTY_REFERENCE_ID;}
}
<WAITING_PROPERTY_REFERENCE_PARAMETER_TOKEN>{
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; } 
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "$" {yybegin(nextStateForPropertyReference()); return PROPERTY_REFERENCE_END;}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {PROPERTY_REFERENCE_PARAMETER_TOKEN} {return PROPERTY_REFERENCE_PARAMETER_TOKEN;}
}

<WAITING_ICON>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "£" {yybegin(nextStateForText()); return ICON_END;}
  "$" {propertyReferenceLocation=2; yypushback(yylength()); yybegin(WAITING_CHECK_PROPERTY_REFERENCE_START);}
  "[" {commandLocation=2; yypushback(yylength()); yybegin(WAITING_CHECK_COMMAND_START);}
  "|" {yybegin(WAITING_ICON_FRAME); return PIPE;}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {ICON_ID} {yybegin(WAITING_ICON_ID_FINISHED); return ICON_ID;}
}
<WAITING_ICON_ID_FINISHED>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "£" {yybegin(nextStateForText()); return ICON_END;}
  "|" {yybegin(WAITING_ICON_FRAME); return PIPE;}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
}
<WAITING_ICON_FRAME>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "£" {yybegin(nextStateForText()); return ICON_END;}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {ICON_FRAME} {return ICON_FRAME;}
}
<WAITING_COMMAND_SCOPE_OR_FIELD>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "]" {yybegin(nextStateForCommand()); return COMMAND_END;}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {COMMAND_SCOPE_ID_WITH_SUFFIX} {yypushback(1); yybegin(WAITING_COMMAND_SEPARATOR); return COMMAND_SCOPE_ID;}
  {COMMAND_FIELD_ID_WITH_SUFFIX} {yypushback(1); yybegin(WAITING_COMMAND_SEPARATOR); return COMMAND_FIELD_ID;}
}
<WAITING_COMMAND_SEPARATOR>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "]" {yybegin(nextStateForCommand()); return COMMAND_END;}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  "." {yybegin(WAITING_COMMAND_SCOPE_OR_FIELD); return DOT;}
}

<WAITING_COLOR_ID>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} {yybegin(WAITING_COLORFUL_TEXT); return WHITE_SPACE; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {COLOR_ID} {yybegin(WAITING_COLORFUL_TEXT); return COLOR_ID;}
  [^] {yypushback(yylength()); yybegin(WAITING_COLORFUL_TEXT); } //提高兼容性
}
<WAITING_COLORFUL_TEXT>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {VALID_ESCAPE_TOKEN} {return VALID_ESCAPE_TOKEN;}
  {INVALID_ESCAPE_TOKEN} {return INVALID_ESCAPE_TOKEN;}
  {DOUBLE_LEFT_BRACKET} {return DOUBLE_LEFT_BRACKET;}
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "$" {propertyReferenceLocation=0; yypushback(yylength()); yybegin(WAITING_CHECK_PROPERTY_REFERENCE_START);}
  "£" {yypushback(yylength()); yybegin(WAITING_CHECK_ICON_START);}
  "[" {commandLocation=0; yypushback(yylength()); yybegin(WAITING_CHECK_COMMAND_START);}
  "§" {yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {STRING_TOKEN} {return STRING_TOKEN;}
}

<WAITING_PROPERTY_END>{
  {EOL} {noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} {return WHITE_SPACE; } //继续解析
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT; }
  \" {yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
}

<WAITING_CHECK_PROPERTY_REFERENCE_START>{
  {CHECK_PROPERTY_REFERENCE_START} {
    //特殊处理
    //如果匹配到的字符串长度大于1，且最后一个字符不为空白或双引号，则认为代表命令的开始
    //否则认为是常规字符串
    boolean isPropertyReferenceStart = isPropertyReferenceStart();
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
    boolean isIconStart = isIconStart();
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

<WAITING_CHECK_COMMAND_START>{
  {CHECK_COMMAND_START} {
    //特殊处理
    //除了可以通过连续的两个左方括号转义之外
    //如果匹配到的字符串长度大于1，且最后一个字符为右方括号，则认为代表命令的开始
    //否则认为是常规字符串
    boolean isCommandStart = isCommandStart();
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
    boolean isColorfulTextStart = isColorfulTextStart();
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
      boolean isRightQuote = isRightQuote();
      yypushback(yylength()-1);
      if(isRightQuote){
          yybegin(WAITING_PROPERTY_END);
          return RIGHT_QUOTE;
      }else{
          yybegin(nextStateForText());
          return STRING_TOKEN;
      }
    }
}

[^] {return TokenType.BAD_CHARACTER; }
