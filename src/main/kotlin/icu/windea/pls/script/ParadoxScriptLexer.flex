package icu.windea.pls.script.psi;

import com.intellij.openapi.project.*;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.config.cwt.*;
import icu.windea.pls.core.*;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;

%%

%public
%class ParadoxScriptLexer
%implements com.intellij.lexer.FlexLexer
%function advance
%type IElementType
%unicode

%state WAITING_VARIABLE_NAME
%state WAITING_VARIABLE_EQUAL_SIGN
%state WAITING_VARIABLE_VALUE
%state WAITING_VARIABLE_END

%state WAITING_PROPERTY
%state WAITING_PROPERTY_KEY
%state WATIING_PROPERTY_SEPARATOR
%state WAITING_PROPERTY_VALUE
%state WAITING_PROPERTY_END

%state WAITING_IS_VARIABLE
%state WAITING_VARIABLE_REFERENCE_NAME

%state WAITING_INLINE_MATH
%state WAITING_INLINE_MATH_PARAMETER
%state WAITING_INLINE_MATH_PARAMETER_DEFAULT_VALUE

%{
    public Project project;
      
    private int depth = 0;
    private boolean leftAbsSign = true;
    
    public ParadoxScriptLexer(Project propect) {
        this((java.io.Reader)null);
        this.project = project;
    }
    
    public int nextState(){
        return depth <= 0 ? YYINITIAL : WAITING_PROPERTY_KEY;
    }
%}

EOL=\s*\R
WHITE_SPACE=[\s&&[^\r\n]]+
BLANK=\s+

COMMENT=#[^\r\n]*
END_OF_LINE_COMMENT=#[^\r\n]*
VARIABLE_ID=[a-zA-Z_][a-zA-Z0-9_]*
PARAMETER_ID=[a-zA-Z_][a-zA-Z0-9_]*
PROPERTY_KEY_ID=[^#@={}\s]+[^={}\s]*
QUOTED_PROPERTY_KEY_ID=\"([^\"(\r\n\\]|\\.)*?\"
BOOLEAN_TOKEN=(yes)|(no)
INT_TOKEN=[+-]?(0|[1-9][0-9]*)
FLOAT_TOKEN=[+-]?(0|[1-9][0-9]*)(\.[0-9]+)
NUMBER_TOKEN=(0|[1-9][0-9]*)(\.[0-9]+)? //non-negative integer / float, without unary sign 
STRING_TOKEN=[^@\s\{\}=\"][^\s\{\}=\"]*
QUOTED_STRING_TOKEN=\"([^\"\r\n\\]|\\.)*?\"
COLOR_TOKEN=(rgb|rgba|hsb|hsv|hsl)[ \t]*\{[\d.\s&&[^\r\n]]*}

//判断接下来是否是属性
IS_PROPERTY=({PROPERTY_KEY_ID}|{QUOTED_PROPERTY_KEY_ID})\s*[=<>]
//判断接下来是变量名还是变量引用
IS_VARIABLE={VARIABLE_ID}(\s*=)?

%%

<YYINITIAL> {
  {BLANK} {return WHITE_SPACE;}
  "}" {depth--; yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++; yybegin(nextState()); return LEFT_BRACE;}
  "@["|"@\\[" {yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;} //出于兼容性考虑，这里允许内联数学表达式
  {COMMENT} {return COMMENT;}
  "@" {yybegin(WAITING_VARIABLE_NAME); return AT;}
  //在这里根据后面是否有"="判断是否是property
  {IS_PROPERTY} {yypushback(yylength()); yybegin(WAITING_PROPERTY);}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_END); return COLOR_TOKEN;}
  {BOOLEAN_TOKEN} {yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_PROPERTY_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN;}
  {STRING_TOKEN} {yybegin(WAITING_PROPERTY_END); return STRING_TOKEN;}
  {QUOTED_STRING_TOKEN} {yybegin(WAITING_PROPERTY_END); return QUOTED_STRING_TOKEN;}
}
<WAITING_VARIABLE_NAME>{
  {VARIABLE_ID} {yybegin(WAITING_VARIABLE_EQUAL_SIGN); return VARIABLE_NAME_ID;}
  "}" {depth--; yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++; yybegin(nextState()); return LEFT_BRACE;}
  "=" {yybegin(WAITING_VARIABLE_VALUE); return EQUAL_SIGN;}
  {BLANK} {return WHITE_SPACE;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
}
<WAITING_VARIABLE_EQUAL_SIGN> {
  "=" {yybegin(WAITING_VARIABLE_VALUE); return EQUAL_SIGN;}
  "}" {depth--; yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++; yybegin(nextState()); return LEFT_BRACE;}
  {BLANK} {return WHITE_SPACE;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
}
<WAITING_VARIABLE_VALUE> {
  {BLANK} {return WHITE_SPACE;}
  "}" {depth--; yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++; yybegin(nextState()); return LEFT_BRACE;}
  {BOOLEAN_TOKEN} {yybegin(WAITING_VARIABLE_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_VARIABLE_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_VARIABLE_END); return FLOAT_TOKEN;}
  {STRING_TOKEN} {yybegin(WAITING_VARIABLE_END); return STRING_TOKEN;}
  {QUOTED_STRING_TOKEN} {yybegin(WAITING_VARIABLE_END); return QUOTED_STRING_TOKEN;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
}
<WAITING_VARIABLE_END> {
  {EOL} {yybegin(nextState()); return WHITE_SPACE;}
  {WHITE_SPACE} {return WHITE_SPACE;}
  "}" {depth--; yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++; yybegin(nextState()); return LEFT_BRACE;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
}

<WAITING_PROPERTY_KEY> {
  {BLANK} {return WHITE_SPACE;}
  "}" {depth--; yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++; yybegin(nextState()); return LEFT_BRACE;}
  "@["|"@\\[" {yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;} //出于兼容性考虑，这里允许内联数学表达式
  "@" {yybegin(WAITING_IS_VARIABLE); return AT;}
  {COMMENT} {return COMMENT;}
  {IS_PROPERTY} {yypushback(yylength()); yybegin(WAITING_PROPERTY);}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_END); return COLOR_TOKEN;}
  {BOOLEAN_TOKEN} {yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_PROPERTY_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN;}
  {STRING_TOKEN} { yybegin(WAITING_PROPERTY_END); return STRING_TOKEN;}
  {QUOTED_STRING_TOKEN} {yybegin(WAITING_PROPERTY_END); return QUOTED_STRING_TOKEN;}
}
<WAITING_PROPERTY>{
  {PROPERTY_KEY_ID} {yybegin(WATIING_PROPERTY_SEPARATOR); return PROPERTY_KEY_ID;}
  {QUOTED_PROPERTY_KEY_ID} {yybegin(WATIING_PROPERTY_SEPARATOR); return QUOTED_PROPERTY_KEY_ID;}
}
<WATIING_PROPERTY_SEPARATOR> {
  {BLANK} {return WHITE_SPACE;}
  "}" {depth--; yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++; yybegin(nextState()); return LEFT_BRACE;}
  "=" {yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;}
  "<" {yybegin(WAITING_PROPERTY_VALUE); return LT_SIGN;}
  ">" {yybegin(WAITING_PROPERTY_VALUE); return GT_SIGN;}
  "<=" {yybegin(WAITING_PROPERTY_VALUE); return LE_SIGN;}
  ">=" {yybegin(WAITING_PROPERTY_VALUE); return GE_SIGN;}
  "<>" {yybegin(WAITING_PROPERTY_VALUE); return NOT_EQUAL_SIGN;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
}
<WAITING_PROPERTY_VALUE>{
  {BLANK} {return WHITE_SPACE;}
  "}" {depth--; yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++; yybegin(nextState()); return LEFT_BRACE;}
  "@["|"@\\[" {yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;}
  "@" {yybegin(WAITING_VARIABLE_REFERENCE_NAME); return AT;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_END); return COLOR_TOKEN;}
  {BOOLEAN_TOKEN} {yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_PROPERTY_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN;}
  {QUOTED_STRING_TOKEN} {yybegin(WAITING_PROPERTY_END); return QUOTED_STRING_TOKEN;}
  {STRING_TOKEN} {yybegin(WAITING_PROPERTY_END); return STRING_TOKEN;}
}
<WAITING_PROPERTY_END> {
  {EOL} {yybegin(nextState()); return WHITE_SPACE;}
  {WHITE_SPACE} {yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE;} //只要有空白相间隔，就可以在写同一行
  "}" {depth--; yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++; yybegin(nextState()); return LEFT_BRACE;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
}

<WAITING_IS_VARIABLE>{
  {IS_VARIABLE} {
    //如果匹配到的文本以等号结尾，则将空白之前的部分解析为VARIABLE_NAME_ID，否则将其解析为VARIABLE_REFERENCE_ID
	CharSequence text = yytext();
	  int length = text.length();
	  if(text.charAt(length -1) == '='){
	  //计算需要回退的长度 @1=
	  int i;
	  for (i = 1; i < length ; i++) {
	    char c = text.charAt(length-i-1);
		if(!Character.isWhitespace(c)) break;
	  }
	  yypushback(i);
	  yybegin(WAITING_VARIABLE_EQUAL_SIGN);
	  return VARIABLE_NAME_ID;
	} else {
	  yybegin(WAITING_PROPERTY_END);
      return VARIABLE_REFERENCE_ID;
	}
  }
  "}" {depth--; yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++; yybegin(nextState()); return LEFT_BRACE;}
  {BLANK} {return WHITE_SPACE;}
}
<WAITING_VARIABLE_REFERENCE_NAME>{
  {VARIABLE_ID} {yybegin(WAITING_PROPERTY_END); return VARIABLE_REFERENCE_ID;}
  "}" {depth--; yybegin(nextState()); return RIGHT_BRACE;}
  "{" {depth++; yybegin(nextState()); return LEFT_BRACE;}
  {BLANK} {return WHITE_SPACE;}
}

<WAITING_INLINE_MATH>{
  {BLANK} {return WHITE_SPACE;}
  "|" {
    if(leftAbsSign){
      leftAbsSign=false; 
      return LABS_SIGN;
    }else{
      leftAbsSign=true;
      return RABS_SIGN;
    }
  }
  "(" {return LP_SIGN;}
  ")" {return RP_SIGN;}
  "+" {yybegin(WAITING_INLINE_MATH); return PLUS_SIGN;}
  "-" {yybegin(WAITING_INLINE_MATH); return MINUS_SIGN;}
  "*" {yybegin(WAITING_INLINE_MATH); return TIMES_SIGN;}
  "/" {yybegin(WAITING_INLINE_MATH); return DIV_SIGN;}
  "%" {yybegin(WAITING_INLINE_MATH); return MOD_SIGN;}
  "$" {yybegin(WAITING_INLINE_MATH_PARAMETER); return PARAMETER_START;}
  {NUMBER_TOKEN} {return NUMBER_TOKEN;}
  {VARIABLE_ID} {return INLINE_MATH_VARIABLE_REFERENCE_ID;}
  "]" {leftAbsSign=true; yybegin(WAITING_PROPERTY_END); return INLINE_MATH_END;}
}
<WAITING_INLINE_MATH_PARAMETER>{
  {PARAMETER_ID} {return PARAMETER_ID;}
  "|" {yybegin(WAITING_INLINE_MATH_PARAMETER_DEFAULT_VALUE); return PIPE;}
  "$" {yybegin(WAITING_INLINE_MATH); return PARAMETER_END;}
}
<WAITING_INLINE_MATH_PARAMETER_DEFAULT_VALUE>{
  {NUMBER_TOKEN} {return NUMBER_TOKEN;}
  "$" {yybegin(WAITING_INLINE_MATH); return PARAMETER_END;}
}

[^] {return BAD_CHARACTER;}
