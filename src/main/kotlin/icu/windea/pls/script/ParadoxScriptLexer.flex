package icu.windea.pls.script.psi;

import com.intellij.openapi.project.*;
import com.intellij.psi.tree.IElementType;

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

%state WAITING_VARIABLE
%state WAITING_VARIABLE_REFERENCE_NAME

%state WAITING_PARAMETER
%state WAITING_PARAMETER_DEFAULT_VALUE
%state WAITING_PARAMETER_DEFAULT_VALUE_END
%state WAITING_AFTER_PARAMETER

%state WAITING_INLINE_MATH

%{
    public Project project;
                  
    private int depth = 0;
	private boolean isPropertyKey = false;
    private boolean inInlineMath = false;
    private boolean leftAbsSign = true;
    
    public ParadoxScriptLexer(Project propect) {
        this((java.io.Reader)null);
        this.project = project;
    }
    
    private void beginNextState(){
        if(depth <= 0){
	        yybegin(YYINITIAL);
        } else {
	        yybegin(WAITING_PROPERTY_KEY);
        }
    }
	
    private void beginNextStateForParameter(){
        if(inInlineMath){
            yybegin(WAITING_INLINE_MATH);
        } else if(isPropertyKey){
			isPropertyKey = false;
			yybegin(WATIING_PROPERTY_SEPARATOR);
		} else {
            yybegin(WAITING_AFTER_PARAMETER);
        }
    }
    
    private void onBlank(){
		//ignore
    }
    
    private void pushbackUntilBeforeBlank(int begin){
        //回退到末尾可能出现的空白之前
        int length = yylength();
        int i;
        for (i = begin; i < length ; i++) {
          char c = yycharat(length-i-1);
          if(!Character.isWhitespace(c)) break;
        }
        if(i != 0){
            yypushback(i);
        }
    }
%}

EOL=\s*\R
WHITE_SPACE=[\s&&[^\r\n]]+
BLANK=\s+
COMMENT=#[^\r\n]*
END_OF_LINE_COMMENT=#[^\r\n]*

//判断接下来是变量名还是变量引用
CHECK_VARIABLE_NAME={VARIABLE_ID}(\s*=)?
//判断接下来是否是属性
CHECK_PROPERTY_KEY=({PROPERTY_KEY_ID}|{QUOTED_PROPERTY_KEY_ID}|[$]{PARAMETER_ID})[$]?\s*[=<>] //不是必须匹配参数结尾的"$"
//判断接下来是否是字符串模版中的字符串部分
CHECK_STRING_PART={STRING_PART}[$]

VARIABLE_ID=[a-zA-Z_][a-zA-Z0-9_]*
PARAMETER_ID=[a-zA-Z_][a-zA-Z0-9_]*
PROPERTY_KEY_ID=[^#@={}\s][^$={}\s]*
//PROPERTY_KEY_ID=[^#@={}\[\]\s][^$={}\[\]\s]*
QUOTED_PROPERTY_KEY_ID=\"([^\"(\r\n\\]|\\.)*?\"
BOOLEAN_TOKEN=(yes)|(no)
INT_TOKEN=[+-]?(0|[1-9][0-9]*)
FLOAT_TOKEN=[+-]?(0|[1-9][0-9]*)(\.[0-9]+)
COLOR_TOKEN=(rgb|rgba|hsb|hsv|hsl)[ \t]*\{[\d.\s&&[^\r\n]]*}
STRING_TOKEN=[^#@$={}\s\"][^$={}\s\"]*
//STRING_TOKEN=[^#@$={}\[\]\s\"][^$={}\[\]\s\"]*
QUOTED_STRING_TOKEN=\"([^\"\r\n\\]|\\.)*?\"
STRING_PART=[^#@$={}\s\"][^$={}\s\"]*
//STRING_PART=[^#@$={}\[\]\s\"][^$={}\[\]\s\"]*

NUMBER_TOKEN=(0|[1-9][0-9]*)(\.[0-9]+)? //non-negative integer / float, without unary sign
ARG_NUMBER_TOKEN=[+-]?{NUMBER_TOKEN}
ARG_STRING_TOKEN={STRING_TOKEN}

%%

<YYINITIAL> {
  {BLANK} { onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  //出于语法兼容性考虑，这里允许内联数学表达式
  "@["|"@\\[" {inInlineMath=true; yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;}
  "@" {yybegin(WAITING_VARIABLE_NAME); return AT;}
  "$" {yybegin(WAITING_PARAMETER); return PARAMETER_START;}
  {CHECK_PROPERTY_KEY} {
    //根据后面是否有"="判断是否是property
	yybegin(WATIING_PROPERTY_SEPARATOR);
	pushbackUntilBeforeBlank(1);
	char firstChar = yycharat(0);
	if(firstChar == '"'){
		return QUOTED_PROPERTY_KEY_ID;
	} else if(firstChar == '$'){
		//propertyKey也可以是parameter
		isPropertyKey = true;
		yypushback(yylength() -1);
		yybegin(WAITING_PARAMETER);
		return PARAMETER_START;
	} else {
		return PROPERTY_KEY_ID;
	}
  }
  {CHECK_STRING_PART} {
	yypushback(1);
	return STRING_PART;
  }
  {BOOLEAN_TOKEN} {beginNextState(); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {beginNextState(); return INT_TOKEN;}
  {FLOAT_TOKEN} {beginNextState(); return FLOAT_TOKEN;}
  {COLOR_TOKEN} {beginNextState(); return COLOR_TOKEN;}
  {STRING_TOKEN} {beginNextState(); return STRING_TOKEN;}
  {QUOTED_STRING_TOKEN} {beginNextState(); return QUOTED_STRING_TOKEN;}
  {COMMENT} {return COMMENT;}
}
<WAITING_VARIABLE_NAME>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {VARIABLE_ID} {yybegin(WAITING_VARIABLE_EQUAL_SIGN); return VARIABLE_NAME_ID;}
  "=" {yybegin(WAITING_VARIABLE_VALUE); return EQUAL_SIGN;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
}
<WAITING_VARIABLE_EQUAL_SIGN> {
  {BLANK} {onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "=" {yybegin(WAITING_VARIABLE_VALUE); return EQUAL_SIGN;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
}
<WAITING_VARIABLE_VALUE> {
  {BLANK} {onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {INT_TOKEN} {yybegin(WAITING_VARIABLE_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_VARIABLE_END); return FLOAT_TOKEN;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
}
<WAITING_VARIABLE_END> {
  //只要有空白相间隔，就可以在写同一行
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
}

<WAITING_PROPERTY_KEY> {
  {BLANK} {onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  //出于语法兼容性考虑，这里允许内联数学表达式
  "@["|"@\\[" {inInlineMath=true; yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;}
  "@" {yybegin(WAITING_VARIABLE); return AT;}
  "$" {yybegin(WAITING_PARAMETER); return PARAMETER_START;}
  {CHECK_PROPERTY_KEY} {
    //根据后面是否有"="判断是否是property
	yybegin(WATIING_PROPERTY_SEPARATOR);
	pushbackUntilBeforeBlank(1);
	char firstChar = yycharat(0);
	if(firstChar == '"'){
		return QUOTED_PROPERTY_KEY_ID;
	} else if(firstChar == '$'){
		//propertyKey也可以是parameter
		isPropertyKey = true;
		yypushback(yylength() -1);
		yybegin(WAITING_PARAMETER);
		return PARAMETER_START;
	} else {
		return PROPERTY_KEY_ID;
	}
  }
  {CHECK_STRING_PART} {
  	yypushback(1);
  	return STRING_PART;
  }
  {BOOLEAN_TOKEN} {beginNextState(); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {beginNextState(); return INT_TOKEN;}
  {FLOAT_TOKEN} {beginNextState(); return FLOAT_TOKEN;}
  {COLOR_TOKEN} {beginNextState(); return COLOR_TOKEN;}
  {STRING_TOKEN} {beginNextState(); return STRING_TOKEN;}
  {QUOTED_STRING_TOKEN} {beginNextState(); return QUOTED_STRING_TOKEN;}
  {COMMENT} {return COMMENT;}
}
<WATIING_PROPERTY_SEPARATOR> {
  {BLANK} {onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "=" {yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;}
  "<" {yybegin(WAITING_PROPERTY_VALUE); return LT_SIGN;}
  ">" {yybegin(WAITING_PROPERTY_VALUE); return GT_SIGN;}
  "<=" {yybegin(WAITING_PROPERTY_VALUE); return LE_SIGN;}
  ">=" {yybegin(WAITING_PROPERTY_VALUE); return GE_SIGN;}
  "<>" {yybegin(WAITING_PROPERTY_VALUE); return NOT_EQUAL_SIGN;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
}
<WAITING_PROPERTY_VALUE>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "@["|"@\\[" {inInlineMath=true; yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;}
  "@" {yybegin(WAITING_VARIABLE_REFERENCE_NAME); return AT;}
  "$" {yybegin(WAITING_PARAMETER); return PARAMETER_START;}
  {BOOLEAN_TOKEN} {beginNextState(); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {beginNextState(); return INT_TOKEN;}
  {FLOAT_TOKEN} {beginNextState(); return FLOAT_TOKEN;}
  {COLOR_TOKEN} {beginNextState(); return COLOR_TOKEN;}
  {CHECK_STRING_PART} {
  	yypushback(1);
  	return STRING_PART;
  }
  {STRING_TOKEN} {beginNextState(); return STRING_TOKEN;}
  {QUOTED_STRING_TOKEN} {beginNextState(); return QUOTED_STRING_TOKEN;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
}
<WAITING_PROPERTY_END> {
  //只要有空白相间隔，就可以在写同一行
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {END_OF_LINE_COMMENT} {return END_OF_LINE_COMMENT;}
}

<WAITING_VARIABLE>{
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {CHECK_VARIABLE_NAME} {
    //如果匹配到的文本以等号结尾，则将空白之前的文本解析为VARIABLE_NAME_ID，否则将整个匹配文本解析为VARIABLE_REFERENCE_ID
	if(yycharat(yylength() -1) == '='){
	  pushbackUntilBeforeBlank(1);
	  yybegin(WAITING_VARIABLE_EQUAL_SIGN);
	  return VARIABLE_NAME_ID;
	} else {
	  beginNextState(); 
      return VARIABLE_REFERENCE_ID;
	}
  }
}
<WAITING_VARIABLE_REFERENCE_NAME>{
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {VARIABLE_ID} {beginNextState(); return VARIABLE_REFERENCE_ID;}
}

//TODO 这里相关的解析失败时的处理可以考虑优化
<WAITING_PARAMETER>{
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {PARAMETER_ID} {return PARAMETER_ID;}
  "|" {yybegin(WAITING_PARAMETER_DEFAULT_VALUE); return PIPE;}
  "$" {beginNextStateForParameter(); return PARAMETER_END;}
}
<WAITING_PARAMETER_DEFAULT_VALUE>{
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {ARG_NUMBER_TOKEN} { yybegin(WAITING_PARAMETER_DEFAULT_VALUE_END); return ARG_NUMBER_TOKEN;}
  {ARG_STRING_TOKEN} { yybegin(WAITING_PARAMETER_DEFAULT_VALUE_END); return ARG_STRING_TOKEN;} 
  "$" {beginNextStateForParameter(); return PARAMETER_END;}
}
<WAITING_PARAMETER_DEFAULT_VALUE_END>{
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "$" {beginNextStateForParameter(); return PARAMETER_END;}
}
<WAITING_AFTER_PARAMETER>{
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "$" {yybegin(WAITING_PARAMETER); return PARAMETER_START;}
  {STRING_PART} {return STRING_PART; }
}

<WAITING_INLINE_MATH>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
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
  "$" {yybegin(WAITING_PARAMETER); return PARAMETER_START;}
  {NUMBER_TOKEN} {return NUMBER_TOKEN;}
  {VARIABLE_ID} {return INLINE_MATH_VARIABLE_REFERENCE_ID;}
  "]" {leftAbsSign=true; inInlineMath=false; beginNextState(); return INLINE_MATH_END;}
}

[^] {return BAD_CHARACTER;}
