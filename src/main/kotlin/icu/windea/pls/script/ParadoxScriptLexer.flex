package icu.windea.pls.script.psi;

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
%state WAITING_VARIABLE_VALUE
%state WAITING_VARIABLE_END

%state WAITING_PROPERTY
%state WAITING_PROPERTY_KEY
%state WAITING_PROPERTY_VALUE
%state WAITING_PROPERTY_END

%state WAITING_WILDCARD_KEY
%state WAITING_WILDCARD_VALUE

%state CHECK_VARIABLE
%state WAITING_VARIABLE_REFERENCE_NAME

%state WAITING_PARAMETER
%state WAITING_PARAMETER_DEFAULT_VALUE
%state WAITING_PARAMETER_DEFAULT_VALUE_END
%state WAITING_AFTER_PARAMETER

%state WAITING_PARAMETER_CONDITION
%state WAITING_PARAMETER_CONDITION_EXPRESSION

%state WAITING_INLINE_MATH

%{
    private int depth = 0;
    private boolean inWildcardKey = false;
    private boolean inWildcardValue = false;
	private boolean isWildcardContainsParameter = false;
	private boolean inParameterCondition = false;
    private boolean inInlineMath = false;
    private boolean leftAbsSign = true;
    
    public ParadoxScriptLexer() {
        this((java.io.Reader)null);
    }
    
    private void beginNextState(){
		if(inParameterCondition){
			yybegin(WAITING_PARAMETER_CONDITION);
		} else {
	        if(depth <= 0){
		        yybegin(YYINITIAL);
	        } else {
		        yybegin(WAITING_PROPERTY_KEY);
	        }
		}
    }
	
    private void beginNextStateForParameter(){
		if(inWildcardKey){
			yybegin(WAITING_WILDCARD_KEY);
		} else if(inWildcardValue){
			yybegin(WAITING_WILDCARD_VALUE);
		} else if(inInlineMath){
            yybegin(WAITING_INLINE_MATH);
        } else {
            beginNextState();
        }
    }
    
    private void onBlank(){
		inWildcardKey = false;
		inWildcardValue = false;
		isWildcardContainsParameter = false;
    }
	
	private void enterWildcardKey(){
		inWildcardKey = true;
	}
	
	private void enterWildcardValue(){
		inWildcardValue = true;
	}
    
	private void enterInlineMath(){
		inInlineMath = true;
		leftAbsSign = true;
	}
	
	private void exitInlineMath(){
		inInlineMath = false;
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

//判断接下来是变量名还是变量引用
CHECK_VARIABLE={VARIABLE_ID}(\s*=)?
//判断接下来是否是属性
CHECK_PROPERTY_KEY=({WILDCARD_KEY_TOKEN}|{QUOTED_PROPERTY_KEY_TOKEN})\s*[=<>] //不是必须匹配参数结尾的"$"

VARIABLE_ID=[a-zA-Z_][a-zA-Z0-9_]*
PARAMETER_ID=[a-zA-Z_][a-zA-Z0-9_]*

WILDCARD_KEY_TOKEN=[^#@={}\[\]\s][^={}\[\]\s]*
PROPERTY_KEY_TOKEN=[^#@$={}\[\]\s][^$={}\[\]\s]*
PROPERTY_KEY_TOKEN_WITH_SUFFIX={PROPERTY_KEY_TOKEN}[$]?
QUOTED_PROPERTY_KEY_TOKEN=\"([^\"(\r\n\\]|\\.)*?\"

BOOLEAN_TOKEN=(yes)|(no)
INT_NUMBER_TOKEN=(0|[1-9][0-9]*)
INT_TOKEN=[+-]?{INT_NUMBER_TOKEN}
FLOAT_NUMBER_TOKEN=(0|[1-9][0-9]*)(\.[0-9]+)
FLOAT_TOKEN=[+-]?{FLOAT_NUMBER_TOKEN}
COLOR_TOKEN=(rgb|rgba|hsb|hsv|hsl)[ \t]*\{[\d.\s&&[^\r\n]]*}

WILDCARD_VALUE_TOKEN=[^#@={}\[\]\s\"][^={}\[\]\s\"]*
STRING_TOKEN_WITH_SUFFIX={STRING_TOKEN}[$]?
STRING_TOKEN=[^#@$={}\[\]\s\"][^$={}\[\]\s\"]*
QUOTED_STRING_TOKEN=\"([^\"\r\n\\]|\\.)*?\"

%%

<YYINITIAL> {
  {BLANK} { onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "=" {yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;}
  "<" {yybegin(WAITING_PROPERTY_VALUE); return LT_SIGN;}
  ">" {yybegin(WAITING_PROPERTY_VALUE); return GT_SIGN;}
  "<=" {yybegin(WAITING_PROPERTY_VALUE); return LE_SIGN;}
  ">=" {yybegin(WAITING_PROPERTY_VALUE); return GE_SIGN;}
  "<>" {yybegin(WAITING_PROPERTY_VALUE); return NOT_EQUAL_SIGN;}
  //出于语法兼容性考虑，这里允许内联数学表达式
  "@["|"@\\[" {enterInlineMath(); yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;}
  //这里必定是variable_name
  "@" {yybegin(WAITING_VARIABLE_NAME); return AT;}
  {CHECK_PROPERTY_KEY} {
	  if(yycharat(0) == '"'){
		  pushbackUntilBeforeBlank(1);
		  return QUOTED_PROPERTY_KEY_TOKEN;
	  } else {
	     enterWildcardKey(); yypushback(yylength()); yybegin(WAITING_WILDCARD_KEY);
	  }
  }
  {BOOLEAN_TOKEN} {yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_PROPERTY_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN;}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_END); return COLOR_TOKEN;}
  {WILDCARD_VALUE_TOKEN} { enterWildcardValue(); yypushback(yylength()); yybegin(WAITING_WILDCARD_VALUE);}
  {QUOTED_STRING_TOKEN} {yybegin(WAITING_PROPERTY_END); return QUOTED_STRING_TOKEN;}
}

<CHECK_VARIABLE>{
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {CHECK_VARIABLE} {
    //如果匹配到的文本以等号结尾，则将空白之前的文本解析为VARIABLE_NAME_ID，否则将整个匹配文本解析为VARIABLE_REFERENCE_ID
	if(yycharat(yylength() -1) == '='){
	  pushbackUntilBeforeBlank(1);
	  yybegin(WAITING_VARIABLE_NAME);
	  return SCRIPTED_VARIABLE_NAME_ID;
	} else {
	  yybegin(WAITING_PROPERTY_END);
      return SCRIPTED_VARIABLE_REFERENCE_ID;
	}
  }
}
<WAITING_VARIABLE_NAME>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {VARIABLE_ID} {return SCRIPTED_VARIABLE_NAME_ID;}
  "=" {yybegin(WAITING_VARIABLE_VALUE); return EQUAL_SIGN;}
}
<WAITING_VARIABLE_VALUE> {
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {BOOLEAN_TOKEN} {yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_VARIABLE_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_VARIABLE_END); return FLOAT_TOKEN;}
}
<WAITING_VARIABLE_END> {
  //只要有空白相间隔，就可以在写同一行
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition=false; beginNextState(); return RIGHT_BRACKET;}
}
<WAITING_VARIABLE_REFERENCE_NAME>{
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {VARIABLE_ID} {yybegin(WAITING_PROPERTY_END); return SCRIPTED_VARIABLE_REFERENCE_ID;}
}

<WAITING_PROPERTY_KEY> {
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "=" {yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;}
  "<" {yybegin(WAITING_PROPERTY_VALUE); return LT_SIGN;}
  ">" {yybegin(WAITING_PROPERTY_VALUE); return GT_SIGN;}
  "<=" {yybegin(WAITING_PROPERTY_VALUE); return LE_SIGN;}
  ">=" {yybegin(WAITING_PROPERTY_VALUE); return GE_SIGN;}
  "<>" {yybegin(WAITING_PROPERTY_VALUE); return NOT_EQUAL_SIGN;}
  //出于语法兼容性考虑，这里允许内联数学表达式
  "@["|"@\\[" {enterInlineMath(); leftAbsSign=true; yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;}
  "@" {yybegin(CHECK_VARIABLE); return AT;}
  "[" {inParameterCondition=true; yybegin(WAITING_PARAMETER_CONDITION); return LEFT_BRACKET;}
   {CHECK_PROPERTY_KEY} {
 	  if(yycharat(0) == '"'){
		  pushbackUntilBeforeBlank(1);
 		  return QUOTED_PROPERTY_KEY_TOKEN;
 	  } else {
 	     enterWildcardKey(); yypushback(yylength()); yybegin(WAITING_WILDCARD_KEY);
 	  }
  }
  {BOOLEAN_TOKEN} {yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_PROPERTY_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN;}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_END); return COLOR_TOKEN;}
  {WILDCARD_VALUE_TOKEN} { enterWildcardValue(); yypushback(yylength()); yybegin(WAITING_WILDCARD_VALUE);}
  {QUOTED_STRING_TOKEN} {yybegin(WAITING_PROPERTY_END); return QUOTED_STRING_TOKEN;}
}
<WAITING_PROPERTY_VALUE>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "@["|"@\\[" {enterInlineMath(); yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;}
  "@" {yybegin(WAITING_VARIABLE_REFERENCE_NAME); return AT;}
  {BOOLEAN_TOKEN} {yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_PROPERTY_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_PROPERTY_END);; return FLOAT_TOKEN;}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_END); return COLOR_TOKEN;}
  {WILDCARD_VALUE_TOKEN} { enterWildcardValue(); yypushback(yylength()); yybegin(WAITING_WILDCARD_VALUE);}
  {QUOTED_STRING_TOKEN} {beginNextState(); return QUOTED_STRING_TOKEN;}
}
<WAITING_PROPERTY_END> {
  //只要有空白相间隔，就可以在写同一行
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition=false; beginNextState(); return RIGHT_BRACKET;}
}

<WAITING_WILDCARD_KEY>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "=" {yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;}
  "<" {yybegin(WAITING_PROPERTY_VALUE); return LT_SIGN;}
  ">" {yybegin(WAITING_PROPERTY_VALUE); return GT_SIGN;}
  "<=" {yybegin(WAITING_PROPERTY_VALUE); return LE_SIGN;}
  ">=" {yybegin(WAITING_PROPERTY_VALUE); return GE_SIGN;}
  "<>" {yybegin(WAITING_PROPERTY_VALUE); return NOT_EQUAL_SIGN;}
  "$" {isWildcardContainsParameter=true; yybegin(WAITING_PARAMETER); return PARAMETER_START;}
  {PROPERTY_KEY_TOKEN_WITH_SUFFIX} {
	  if(yycharat(yylength() - 1) == '$'){
		  yypushback(1);
		  isWildcardContainsParameter = true;
		  return KEY_STRING_SNIPPET;
      } else if(isWildcardContainsParameter){
          return KEY_STRING_SNIPPET;
      } else {
		  return PROPERTY_KEY_TOKEN;
      }
    }
  //{QUOTED_PROPERTY_KEY_TOKEN} {return QUOTED_PROPERTY_KEY_TOKEN;}
}
<WAITING_WILDCARD_VALUE>{
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "$" {isWildcardContainsParameter=true; yybegin(WAITING_PARAMETER); return PARAMETER_START;}
  {STRING_TOKEN_WITH_SUFFIX} {
	  if(yycharat(yylength() - 1) == '$'){
		  yypushback(1);
		  isWildcardContainsParameter = true;
		  return VALUE_STRING_SNIPPET;
      } else if(isWildcardContainsParameter){
          return VALUE_STRING_SNIPPET;
      } else {
		  return STRING_TOKEN;
      }
    }
  //{QUOTED_STRING_TOKEN} {return QUOTED_STRING_TOKEN;}
}

//TODO 这里相关的解析失败时的处理可以考虑优化
<WAITING_PARAMETER>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {PARAMETER_ID} {return PARAMETER_ID;}
  "|" {yybegin(WAITING_PARAMETER_DEFAULT_VALUE); return PIPE;}
  "$" {beginNextStateForParameter(); return PARAMETER_END;}
}
<WAITING_PARAMETER_DEFAULT_VALUE>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  {BOOLEAN_TOKEN} { yybegin(WAITING_PARAMETER_DEFAULT_VALUE_END); return BOOLEAN;}
  {INT_TOKEN} {yybegin(WAITING_PARAMETER_DEFAULT_VALUE_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_PARAMETER_DEFAULT_VALUE_END);; return FLOAT_TOKEN;}
  {STRING_TOKEN} { yybegin(WAITING_PARAMETER_DEFAULT_VALUE_END); return STRING_TOKEN;} 
  "$" {beginNextStateForParameter(); return PARAMETER_END;}
}
<WAITING_PARAMETER_DEFAULT_VALUE_END>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "$" {beginNextStateForParameter(); return PARAMETER_END;}
}

<WAITING_PARAMETER_CONDITION>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "[" {yybegin(WAITING_PARAMETER_CONDITION_EXPRESSION); return NESTED_LEFT_BRACKET;}
  "]" {inParameterCondition=false; beginNextState(); return RIGHT_BRACKET;}
  //出于语法兼容性考虑，这里允许内联数学表达式
  "@["|"@\\[" {enterInlineMath(); yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;}
  "@" {yybegin(CHECK_VARIABLE); return AT;}
  {CHECK_PROPERTY_KEY} {
 	  if(yycharat(0) == '"'){
		  pushbackUntilBeforeBlank(1);
 		  return QUOTED_PROPERTY_KEY_TOKEN;
 	  } else {
 	     enterWildcardKey(); yypushback(yylength()); yybegin(WAITING_WILDCARD_KEY);
 	  }
  }
  {BOOLEAN_TOKEN} {yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_PROPERTY_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN;}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_END); return COLOR_TOKEN;}
  {WILDCARD_VALUE_TOKEN} { enterWildcardValue(); yypushback(yylength()); yybegin(WAITING_WILDCARD_VALUE);}
  {QUOTED_STRING_TOKEN} {yybegin(WAITING_PROPERTY_END); return QUOTED_STRING_TOKEN;}
}
<WAITING_PARAMETER_CONDITION_EXPRESSION>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "!" {return NOT_SIGN;}
  {PARAMETER_ID} {return ARGUMENT_ID;}
  "]" {yybegin(WAITING_PARAMETER_CONDITION); return NESTED_RIGHT_BRACKET;}
}

<WAITING_INLINE_MATH>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
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
  {INT_NUMBER_TOKEN} {return INT_NUMBER_TOKEN;}
  {FLOAT_NUMBER_TOKEN} {return FLOAT_NUMBER_TOKEN;}
  {VARIABLE_ID} {return INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_ID;}
  "]" {exitInlineMath(); beginNextState(); return INLINE_MATH_END;}
}

[^] {return BAD_CHARACTER;}
