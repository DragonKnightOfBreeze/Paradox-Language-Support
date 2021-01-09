package com.windea.plugin.idea.paradox.script.psi;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.*;
import static com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*;

%%

%public
%class ParadoxScriptLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%state WAITING_VARIABLE_EQUAL_SIGN
%state WAITING_VARIABLE_VALUE
%state WAITING_VARIABLE_EOL

%state WAITING_PROPERTY
%state WAITING_PROPERTY_KEY
%state WATIING_PROPERTY_SEPARATOR
%state WAITING_PROPERTY_VALUE
%state WAITING_PROPERTY_EOL

%state WAITING_CODE

%{
  int depth = 0;

  public int nextState(){
	  return depth <= 0 ? YYINITIAL : WAITING_PROPERTY_KEY;
  }
%}

EOL=\s*\R\s*
WHITE_SPACE=[ \t]+

COMMENT=#[^\r\n]*
END_OF_LINE_COMMENT=#[^\r\n]*
VARIABLE_NAME_ID=@[a-zA-Z0-9_-]+
PROPERTY_KEY_ID=[^#@={}\s]+[^={}\s]*
QUOTED_PROPERTY_KEY_ID=\"([^\"(\r\n\\]|\\.)*?\"
VARIABLE_REFERENCE_ID=@[a-zA-Z0-9_-]+
BOOLEAN=(yes)|(no)
NUMBER=0|[+-]?[1-9][0-9]*|[+-]?[0-9]+\.[0-9]+
STRING=[^@\s\{\}=\"][^\s\{\}=\"]*
QUOTED_STRING=\"([^\"\r\n\\]|\\.)*?\"
COLOR_TOKEN=(rgb|rgba|hsb|hsv|hsl)[ \t]*\{[0-9. \t]*}
CODE_TEXT_TOKEN=[^\r\n\]}]+

//为了兼容cwt规则文件（<xxx>格式的propertyKey），需要弄得很麻烦
//要求=周围可以没有空格，但其他分隔符如<=周围必须有
IS_PROPERTY=(([^#@={}\s][^={}\s]*)|(\"([^\"(\r\n\\]|\\.)*?\"))((\s*=)|(\s+[=<>]))

%%

<YYINITIAL> {
  "}" {depth--;  yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++;  yybegin(nextState()); return LEFT_BRACE;}
  "@\\[" {yybegin(WAITING_CODE); return CODE_START;} //这里的反斜线需要转义
  {EOL} { return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE; }
  {COMMENT} {return COMMENT; }
  {VARIABLE_NAME_ID} { yybegin(WAITING_VARIABLE_EQUAL_SIGN); return VARIABLE_NAME_ID; }
  //在这里根据后面是否有"="判断是否是property
  {IS_PROPERTY} {yypushback(yylength()); yybegin(WAITING_PROPERTY);}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_EOL); return COLOR_TOKEN;}
  {BOOLEAN} { yybegin(WAITING_PROPERTY_EOL); return BOOLEAN_TOKEN; }
  {NUMBER} { yybegin(WAITING_PROPERTY_EOL); return NUMBER_TOKEN; }
  {STRING} {yybegin(WAITING_PROPERTY_EOL); return STRING_TOKEN;}
  {QUOTED_STRING} {yybegin(WAITING_PROPERTY_EOL); return QUOTED_STRING_TOKEN;}
}
<WAITING_VARIABLE_EQUAL_SIGN> {
  "}" {depth--;  yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++;  yybegin(nextState()); return LEFT_BRACE;}
  "=" {yybegin(WAITING_VARIABLE_VALUE); return EQUAL_SIGN;}
  {EOL} { yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE; }
  {END_OF_LINE_COMMENT} {  return END_OF_LINE_COMMENT; }
}
<WAITING_VARIABLE_VALUE> {
  "}" {depth--;  yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++;  yybegin(nextState()); return LEFT_BRACE;}
  {BOOLEAN} { yybegin(WAITING_VARIABLE_EOL); return BOOLEAN_TOKEN; }
  {NUMBER} {yybegin(WAITING_VARIABLE_EOL); return NUMBER_TOKEN; }
  {STRING} {yybegin(WAITING_VARIABLE_EOL); return STRING_TOKEN;}
  {QUOTED_STRING} {yybegin(WAITING_VARIABLE_EOL); return QUOTED_STRING_TOKEN;}
  {EOL} { yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE; }
  {END_OF_LINE_COMMENT} {  return END_OF_LINE_COMMENT; }
}
<WAITING_VARIABLE_EOL> {
  "}" {depth--;  yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++;  yybegin(nextState()); return LEFT_BRACE;}
  {EOL} { yybegin(YYINITIAL); return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE; }
  {END_OF_LINE_COMMENT} { return END_OF_LINE_COMMENT; }
}

<WAITING_PROPERTY_KEY> {
  "}" {depth--;  yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++;  yybegin(nextState()); return LEFT_BRACE;}
  "@\\[" {yybegin(WAITING_CODE); return CODE_START;} //这里的反斜线需要转义
  {EOL} { return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE; }
  {COMMENT} {  return COMMENT; }
  {IS_PROPERTY} {yypushback(yylength()); yybegin(WAITING_PROPERTY);}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_EOL); return COLOR_TOKEN;}
  {BOOLEAN} { yybegin(WAITING_PROPERTY_EOL); return BOOLEAN_TOKEN; }
  {NUMBER} { yybegin(WAITING_PROPERTY_EOL); return NUMBER_TOKEN; }
  {STRING} {yybegin(WAITING_PROPERTY_EOL); return STRING_TOKEN;}
  {QUOTED_STRING} {yybegin(WAITING_PROPERTY_EOL); return QUOTED_STRING_TOKEN;}
}
<WAITING_PROPERTY>{
  {PROPERTY_KEY_ID} {yybegin(WATIING_PROPERTY_SEPARATOR); return PROPERTY_KEY_ID;}
  {QUOTED_PROPERTY_KEY_ID} {yybegin(WATIING_PROPERTY_SEPARATOR); return QUOTED_PROPERTY_KEY_ID;}
}
<WATIING_PROPERTY_SEPARATOR> {
  "}" {depth--;  yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++;  yybegin(nextState()); return LEFT_BRACE;}
  "=" {yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;}
  "<" {yybegin(WAITING_PROPERTY_VALUE); return LT_SIGN;}
  ">" {yybegin(WAITING_PROPERTY_VALUE); return GT_SIGN;}
  "<=" {yybegin(WAITING_PROPERTY_VALUE); return LE_SIGN;}
  ">=" {yybegin(WAITING_PROPERTY_VALUE); return GE_SIGN;}
  "<>" {yybegin(WAITING_PROPERTY_VALUE); return NOT_EQUAL_SIGN;}
  {EOL} {  yybegin(nextState()); return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE; }
  {END_OF_LINE_COMMENT} {  return END_OF_LINE_COMMENT; }
}
<WAITING_PROPERTY_VALUE>{
  "}" {depth--;  yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++;  yybegin(nextState()); return LEFT_BRACE;}
  "@\\[" {yybegin(WAITING_CODE); return CODE_START;} //这里的反斜线需要转义
  {EOL} {  yybegin(nextState()); return WHITE_SPACE; }
  {WHITE_SPACE} { return WHITE_SPACE; }
  {END_OF_LINE_COMMENT} {  return END_OF_LINE_COMMENT; }
  {VARIABLE_REFERENCE_ID} {yybegin(WAITING_PROPERTY_EOL); return VARIABLE_REFERENCE_ID;}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_EOL); return COLOR_TOKEN;}
  {BOOLEAN} { yybegin(WAITING_PROPERTY_EOL); return BOOLEAN_TOKEN; }
  {NUMBER} { yybegin(WAITING_PROPERTY_EOL); return NUMBER_TOKEN; }
  {QUOTED_STRING} { yybegin(WAITING_PROPERTY_EOL); return QUOTED_STRING_TOKEN; }
  {STRING} { yybegin(WAITING_PROPERTY_EOL); return STRING_TOKEN; }
}
<WAITING_PROPERTY_EOL> {
  "}" {depth--;  yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++;  yybegin(nextState()); return LEFT_BRACE;}
  {EOL} {  yybegin(nextState()); return WHITE_SPACE; }
  {WHITE_SPACE} { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE; } //只要有空白相间隔，就可以在写同一行
  {END_OF_LINE_COMMENT} {  return END_OF_LINE_COMMENT; }
}

<WAITING_CODE>{
  "}" {depth--;  yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++;  yybegin(nextState()); return LEFT_BRACE;}
  "]" {yybegin(WAITING_PROPERTY_EOL); return CODE_END; }
  {EOL} {  yybegin(nextState()); return WHITE_SPACE; }
  {CODE_TEXT_TOKEN} {return CODE_TEXT_TOKEN;}
}

[^] { return BAD_CHARACTER; }
