package com.windea.plugin.idea.paradox.localisation.psi;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.*;
import static com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*;

//Stellaris官方本地化文件中本身就存在语法解析错误，需要保证存在错误的情况下仍然会解析后续的本地化文本，草

%%

%public
%class ParadoxLocalisationLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%state WAITING_LOCALE_COLON
%state WAITING_LOCALE_EOL
%state WAITING_PROPERTY_KEY
%state WAITING_PROPERTY_COLON
%state WAITING_PROPERTY_NUMBER
%state WAITING_PROPERTY_SPACE
%state WAITING_PROPERTY_VALUE
%state WAITING_PROPERTY_EOL
%state WAITING_RICH_TEXT
%state WAITING_PROPERTY_REFERENCE
%state WAITING_PROPERTY_REFERENCE_PARAMETER
%state WAITING_ICON
%state WAITING_ICON_NAME_FINISHED
%state WAITING_ICON_PARAMETER
%state WAITING_SERIAL_NUMBER
%state WAITING_COMMAND_KEY
%state WAITING_COMMAND_KEY_SEPARATOR
%state WAITING_COLOR_CODE
%state WAITING_COLORFUL_TEXT

%state WAITING_CHECK_ICON_START
%state WAITING_CHECK_SERIAL_NUMBER_START
%state WAITING_CHECK_COLORFUL_TEXT_START
%state WAITING_CHECK_RIGHT_QUOTE

%{
  int depth = 0;
  int codeLocation = 0;
  int propertyReferenceLocation = 0;
  boolean inIconName = false;
  boolean isColorfulText = false;

  public int nextStateForText(){
    return depth <= 0 ? WAITING_RICH_TEXT : WAITING_COLORFUL_TEXT;
  }

  public int nextStateForCheck(){
    return isColorfulText?WAITING_COLORFUL_TEXT:WAITING_RICH_TEXT;
  }

  public int nextStateForCode(){
    if(codeLocation == 0) return nextStateForText();
    else if (codeLocation == 1) return WAITING_PROPERTY_REFERENCE;
    else if (codeLocation == 2) return WAITING_ICON;
    else return nextStateForText();
  }

  public int nextStateForPropertyReference(){
    if(propertyReferenceLocation == 0) return nextStateForText();
    else if (propertyReferenceLocation == 2) return WAITING_ICON;
    else return nextStateForText();
  }
%}

//不要使用\R：可能不合法

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
PROPERTY_REFERENCE_ID=[a-zA-Z0-9_.\-' \t]+
PROPERTY_REFERENCE_PARAMETER=[a-zA-Z0-9+\-*%=\[.\]]+
ICON_ID=[a-zA-Z0-9\-_\\/]+
ICON_PARAMETER=[a-zA-Z0-9+\-*%=]+
SERIAL_NUMBER_ID=[a-zA-Z]
COMMAND_KEY_TOKEN=[a-zA-Z0-9_$]+ //加$是为了兼容性
COMMAND_KEY_SEPARATOR=\.
COLOR_CODE=[a-zA-Z]
//双引号和百分号实际上不需要转义
STRING_TOKEN=[^\"%$£§\[\r\n\\]+

CHECK_ICON_START=£.?
CHECK_SERIAL_NUMBER_START=%.?.?
CHECK_COLORFUL_TEXT_START=§.?
CHECK_RIGHT_QUOTE=\"[^\"\r\n]*\"?

%%

//同一状态下的规则无法保证顺序

<YYINITIAL> {
  {ROOT_COMMENT} {  return ROOT_COMMENT; }
  {LOCALE_ID} { yybegin(WAITING_LOCALE_COLON); return LOCALE_ID; }
  {EOL} { return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE; } //继续解析
}
<WAITING_LOCALE_COLON>{
  ":" { yybegin(WAITING_LOCALE_EOL); return COLON; }
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE; }
}
<WAITING_LOCALE_EOL>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE; }
  {END_OF_LINE_COMMENT} {  return END_OF_LINE_COMMENT; }
}

<WAITING_PROPERTY_KEY> {
  {COMMENT} { return COMMENT; }
  {EOL} { return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE; }
  {PROPERTY_KEY_ID} { yybegin(WAITING_PROPERTY_COLON); return PROPERTY_KEY_ID; }
}
<WAITING_PROPERTY_COLON>{
  ":" {yybegin(WAITING_PROPERTY_NUMBER); return COLON; }
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE;}
}
<WAITING_PROPERTY_NUMBER>{
  {NUMBER} {yybegin(WAITING_PROPERTY_SPACE); return NUMBER;}
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {yybegin(WAITING_PROPERTY_VALUE); return WHITE_SPACE;}
}
<WAITING_PROPERTY_SPACE>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} {yybegin(WAITING_PROPERTY_VALUE); return WHITE_SPACE;}
}
<WAITING_PROPERTY_VALUE> {
  \" { yybegin(WAITING_RICH_TEXT); return LEFT_QUOTE; }
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE;}
}
<WAITING_RICH_TEXT>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  \" { isColorfulText=false; yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "$" { propertyReferenceLocation=0; yybegin(WAITING_PROPERTY_REFERENCE); return PROPERTY_REFERENCE_START;}
  "£" { isColorfulText=false; yypushback(yylength()); yybegin(WAITING_CHECK_ICON_START);}
  "%" { isColorfulText=false; yypushback(yylength()); yybegin(WAITING_CHECK_SERIAL_NUMBER_START);}
  "[" { codeLocation=0; yybegin(WAITING_COMMAND_KEY); return COMMAND_START;}
  "§" { isColorfulText=false; yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  {VALID_ESCAPE_TOKEN} {return VALID_ESCAPE_TOKEN;}
  {INVALID_ESCAPE_TOKEN} {return INVALID_ESCAPE_TOKEN;}
  {STRING_TOKEN} {  return STRING_TOKEN;}
}
<WAITING_PROPERTY_REFERENCE>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  "$" {yybegin(nextStateForPropertyReference()); return PROPERTY_REFERENCE_END;}
  \" { yybegin(WAITING_PROPERTY_EOL); return RIGHT_QUOTE;}
  "[" { codeLocation=1; yybegin(WAITING_COMMAND_KEY); return COMMAND_START;}
  "§" { isColorfulText=false; yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "|" { yybegin(WAITING_PROPERTY_REFERENCE_PARAMETER); return PARAMETER_SEPARATOR;}
  {PROPERTY_REFERENCE_ID} {return PROPERTY_REFERENCE_ID;}
  //注释掉 - 属性引用名字可以包含空格，虽然不知道凭什么
  //{SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; } //继续解析
}
<WAITING_PROPERTY_REFERENCE_PARAMETER>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  "$" {yybegin(nextStateForPropertyReference()); return PROPERTY_REFERENCE_END;}
  \" { yybegin(WAITING_PROPERTY_EOL); return RIGHT_QUOTE;}
  "§" { isColorfulText=false; yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  {PROPERTY_REFERENCE_PARAMETER} {return PROPERTY_REFERENCE_PARAMETER;}
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; } //继续解析
}
<WAITING_ICON>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  "£" { yybegin(nextStateForText()); return ICON_END;}
  "$" { propertyReferenceLocation=2; yybegin(WAITING_PROPERTY_REFERENCE); return PROPERTY_REFERENCE_START;}
  \" { yybegin(WAITING_PROPERTY_EOL); return RIGHT_QUOTE;}
  "[" { codeLocation=2; yybegin(WAITING_COMMAND_KEY); return COMMAND_START;}
  "|" { yybegin(WAITING_ICON_PARAMETER); return PARAMETER_SEPARATOR;}
  "§" { isColorfulText=false; yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  {ICON_ID} { yybegin(WAITING_ICON_NAME_FINISHED); return ICON_ID;}
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; } //继续解析
}
<WAITING_ICON_NAME_FINISHED>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  "£" { yybegin(nextStateForText()); return ICON_END;}
  "$" { propertyReferenceLocation=2; yybegin(WAITING_PROPERTY_REFERENCE); return PROPERTY_REFERENCE_START;}
  \" { yybegin(WAITING_PROPERTY_EOL); return RIGHT_QUOTE;}
  "[" { codeLocation=2; yybegin(WAITING_COMMAND_KEY); return COMMAND_START;}
  "|" { yybegin(WAITING_ICON_PARAMETER); return PARAMETER_SEPARATOR;}
  "§" { isColorfulText=false; yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; } //继续解析
}
<WAITING_ICON_PARAMETER>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  "$" { propertyReferenceLocation=2; yybegin(WAITING_PROPERTY_REFERENCE); return PROPERTY_REFERENCE_START;}
  "£" {yybegin(nextStateForText()); return ICON_END;}
  \" { yybegin(WAITING_PROPERTY_EOL); return RIGHT_QUOTE;}
  "§" { isColorfulText=false; yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  {ICON_PARAMETER} {return ICON_PARAMETER;}
}
<WAITING_SERIAL_NUMBER>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  "%" {yybegin(nextStateForText()); return SERIAL_NUMBER_END;}
  \" { yybegin(WAITING_PROPERTY_EOL); return RIGHT_QUOTE;}
  "§" { isColorfulText=false; yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  {SERIAL_NUMBER_ID} {return SERIAL_NUMBER_ID;}
  {WHITE_SPACE} {yybegin(nextStateForText()); return WHITE_SPACE; } //继续解析
}
<WAITING_COMMAND_KEY>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  "]" {yybegin(nextStateForCode()); return COMMAND_END;}
  \" { yybegin(WAITING_PROPERTY_EOL); return RIGHT_QUOTE;}
  "§" { isColorfulText=false; yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  {COMMAND_KEY_TOKEN} {yybegin(WAITING_COMMAND_KEY_SEPARATOR); return COMMAND_KEY_TOKEN;}
}
<WAITING_COMMAND_KEY_SEPARATOR>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  "]" {yybegin(nextStateForCode()); return COMMAND_END;}
  \" { yybegin(WAITING_PROPERTY_EOL); return RIGHT_QUOTE;}
  "§" { isColorfulText=false; yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  {COMMAND_KEY_SEPARATOR} {yybegin(WAITING_COMMAND_KEY); return COMMAND_KEY_SEPARATOR;}
}
<WAITING_COLOR_CODE>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} { yybegin(WAITING_COLORFUL_TEXT); return WHITE_SPACE; }
  "§!" {depth--; yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  \" { yybegin(WAITING_PROPERTY_EOL); return RIGHT_QUOTE;}
  {COLOR_CODE} {yybegin(WAITING_COLORFUL_TEXT); return COLOR_CODE;}
}
<WAITING_COLORFUL_TEXT>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  \" { isColorfulText=true; yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);}
  "$" { propertyReferenceLocation=0; yybegin(WAITING_PROPERTY_REFERENCE); return PROPERTY_REFERENCE_START;}
  "£" { isColorfulText=true; yypushback(yylength()); yybegin(WAITING_CHECK_ICON_START);}
  "%" { isColorfulText=true; yypushback(yylength()); yybegin(WAITING_CHECK_SERIAL_NUMBER_START);}
  "[" { codeLocation=0; yybegin(WAITING_COMMAND_KEY); return COMMAND_START;}
  "§" { isColorfulText=true; yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);}
  "§!" {depth--; yybegin(nextStateForText()); return COLORFUL_TEXT_END;}
  {VALID_ESCAPE_TOKEN} {return VALID_ESCAPE_TOKEN;}
  {INVALID_ESCAPE_TOKEN} {return INVALID_ESCAPE_TOKEN;}
  {STRING_TOKEN} {  return STRING_TOKEN;}
}
<WAITING_PROPERTY_EOL>{
  {EOL} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE; } //继续解析
  {END_OF_LINE_COMMENT} {  return END_OF_LINE_COMMENT; }
}

<WAITING_CHECK_ICON_START>{
  {CHECK_ICON_START} {
    //特殊处理
    //如果匹配的字符串的第2个字符存在且不为空白或双引号，则认为代表图标的开始
    //否则认为是常规字符串
    boolean isIconStart = yylength() == 2 && !Character.isWhitespace(yycharat(1)) && yycharat(1) != '"' ;
    yypushback(yylength()-1);
    if(isIconStart){
    	  yybegin(WAITING_ICON);
    	  return ICON_START;
    }else{
        yybegin(nextStateForCheck());
        return STRING_TOKEN;
    }
  }
}
<WAITING_CHECK_SERIAL_NUMBER_START>{
  {CHECK_SERIAL_NUMBER_START} {
    //特殊处理
    //如果匹配的字符串的第3个字符存在且为百分号，则认为整个字符串代表一个编号
    //否则认为是常规字符串
    boolean isSerialNumberStart = yylength() == 3 && yycharat(2) == '%';
    yypushback(yylength()-1);
    if(isSerialNumberStart){
        yybegin(WAITING_SERIAL_NUMBER);
        return SERIAL_NUMBER_START;
    }else{
        yybegin(nextStateForCheck());
        return STRING_TOKEN;
    }
  }
}
<WAITING_CHECK_COLORFUL_TEXT_START>{
  {CHECK_COLORFUL_TEXT_START} {
    //特殊处理
    //如果匹配的字符串的第2个字符存在且不为空白或双引号，则认为代表彩色文本的开始
    //否则认为是常规字符串
    boolean isColorfulTextStart = yylength() == 2 && !Character.isWhitespace(yycharat(1)) && yycharat(1) != '"' ;
    yypushback(yylength()-1);
    if(isColorfulTextStart){
        yybegin(WAITING_COLOR_CODE);
        depth++;
        return COLORFUL_TEXT_START;
    }else{
        yybegin(nextStateForCheck());
        return STRING_TOKEN;
    }
  }
}
<WAITING_CHECK_RIGHT_QUOTE>{
  {CHECK_RIGHT_QUOTE} {
    //特殊处理
    //如果匹配到的字符串不是仅包含双引号，且最后一个字符是双引号，则表示开始的双引号不代表字符串的结束
    //否则认为是常规字符串
    boolean isRightQuote = yylength() == 1 || yycharat(yylength()-1) != '"';
    yypushback(yylength()-1);
    if(isRightQuote){
        yybegin(WAITING_PROPERTY_EOL);
        return RIGHT_QUOTE;
    }else{
        yybegin(nextStateForCheck());
        return STRING_TOKEN;
    }
  }
}

[^] { return TokenType.BAD_CHARACTER; }
