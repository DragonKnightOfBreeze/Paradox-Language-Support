package icu.windea.pls.script.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;

%%

%public
%class ParadoxScriptLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%state WAITING_SCRIPTED_VARIABLE
%state WAITING_SCRIPTED_VARIABLE_NAME
%state WAITING_SCRIPTED_VARIABLE_VALUE
%state WAITING_SCRIPTED_VARIABLE_END

%state WAITING_PROPERTY
%state WAITING_PROPERTY_KEY
%state WAITING_PROPERTY_VALUE
%state WAITING_PROPERTY_END

%state WAITING_WILDCARD_KEY
%state WAITING_WILDCARD_VALUE

%state WAITING_SCRIPTED_VARIABLE_REFERENCE_NAME

%state WAITING_PARAMETER
%state WAITING_PARAMETER_DEFAULT_VALUE
%state WAITING_PARAMETER_DEFAULT_VALUE_END

%state WAITING_PARAMETER_CONDITION
%state WAITING_PARAMETER_CONDITION_EXPRESSION

%state WAITING_INLINE_MATH

%{
	private enum ParameterPosition {
	    NONE, SCRIPTED_VARIABLE_NAME, SCRIPTED_VARIABLE_REFERENCE_NAME, KEY, STRING, INLINE_MATH;
	}

    private int depth = 0;
    private ParameterPosition parameterPosition = ParameterPosition.NONE;
    private boolean inScriptedVariableName = false;
    private boolean inParameterCondition = false;
    private boolean isWildcardContainsParameter = false;
    private boolean leftAbsSign = true;
    
    public ParadoxScriptLexer() {
        this((java.io.Reader)null);
    }
	
    private void onBlank(){
        isWildcardContainsParameter = false;
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
		if(parameterPosition == ParameterPosition.SCRIPTED_VARIABLE_NAME) {
			yybegin(WAITING_SCRIPTED_VARIABLE_NAME);
		} else if(parameterPosition == ParameterPosition.SCRIPTED_VARIABLE_REFERENCE_NAME) {
            yybegin(WAITING_SCRIPTED_VARIABLE_REFERENCE_NAME);
        } else if(parameterPosition == ParameterPosition.KEY){
		    yybegin(WAITING_WILDCARD_KEY);
	    } else if(parameterPosition == ParameterPosition.STRING){
		    yybegin(WAITING_WILDCARD_VALUE);
	    } else if(parameterPosition == ParameterPosition.INLINE_MATH) {
            yybegin(WAITING_INLINE_MATH);
        } else {
            beginNextState();
        }
		parameterPosition = ParameterPosition.NONE;
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
CHECK_SCRIPTED_VARIABLE={SCRIPTED_VARIABLE_NAME_TOKEN}(\s*=)?
//判断接下来是否是属性的键
CHECK_PROPERTY_KEY=({WILDCARD_KEY_TOKEN}|{QUOTED_PROPERTY_KEY_TOKEN})\s*[!=<>]

SCRIPTED_VARIABLE_NAME_TOKEN=[a-zA-Z0-9_]+

PARAMETER_TOKEN=[a-zA-Z_][a-zA-Z0-9_]*

WILDCARD_KEY_TOKEN=[^#@={}\[\]\s\"][^#={}\[\]\s]*
PROPERTY_KEY_TOKEN=[^#@$={}\[\]\s\"][^#$={}\[\]\s]*
PROPERTY_KEY_TOKEN_WITH_SUFFIX={PROPERTY_KEY_TOKEN}[$]?
QUOTED_PROPERTY_KEY_TOKEN=\"([^\"(\r\n\\]|\\.)*?\"?

BOOLEAN_TOKEN=(yes)|(no)
INT_NUMBER_TOKEN=[0-9]+ //leading zero is permitted
INT_TOKEN=[+-]?{INT_NUMBER_TOKEN}
FLOAT_NUMBER_TOKEN=[0-9]*(\.[0-9]+) //leading zero is permitted
FLOAT_TOKEN=[+-]?{FLOAT_NUMBER_TOKEN}
COLOR_TOKEN=(rgb|hsv)[ \t]*\{[\d.\s&&[^\r\n]]*}

WILDCARD_VALUE_TOKEN=[^#@={}\[\]\s\"][^#={}\[\]\s\"]*
STRING_TOKEN_WITH_SUFFIX={STRING_TOKEN}[$]?
STRING_TOKEN=[^#@$={}\[\]\s\"][^#$={}\[\]\s\"]*
QUOTED_STRING_TOKEN=\"([^\"\r\n\\]|\\.)*?\"?

%%

<YYINITIAL> {
  {BLANK} { onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" { inParameterCondition = false;beginNextState(); return RIGHT_BRACKET;}
  "=" {yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;}
  "<" {yybegin(WAITING_PROPERTY_VALUE); return LT_SIGN;}
  ">" {yybegin(WAITING_PROPERTY_VALUE); return GT_SIGN;}
  "<=" {yybegin(WAITING_PROPERTY_VALUE); return LE_SIGN;}
  ">=" {yybegin(WAITING_PROPERTY_VALUE); return GE_SIGN;}
  "!="|"<>" {yybegin(WAITING_PROPERTY_VALUE); return NOT_EQUAL_SIGN;}
  //出于语法兼容性考虑，这里允许内联数学表达式
  "@["|"@\\[" {yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;}
  //这里必定是variable_name
  "@" {yybegin(WAITING_SCRIPTED_VARIABLE_NAME); return AT;}
  {CHECK_PROPERTY_KEY} {
	  if(yycharat(0) == '"'){
		  pushbackUntilBeforeBlank(1);
		  return QUOTED_PROPERTY_KEY_TOKEN;
	  } else {
	     yypushback(yylength()); 
		 yybegin(WAITING_WILDCARD_KEY);
	  }
  }
  {BOOLEAN_TOKEN} {yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_PROPERTY_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN;}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_END); return COLOR_TOKEN;}
  {WILDCARD_VALUE_TOKEN} { yypushback(yylength()); yybegin(WAITING_WILDCARD_VALUE);}
  {QUOTED_STRING_TOKEN} {yybegin(WAITING_PROPERTY_END); return QUOTED_STRING_TOKEN;}
}

<WAITING_SCRIPTED_VARIABLE>{
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  {CHECK_SCRIPTED_VARIABLE} {
    //如果匹配到的文本以等号结尾，则将空白之前的文本解析为SCRIPTED_VARIABLE_NAME_TOKEN，否则将整个匹配文本解析为VARIABLE_REFERENCE_ID
	if(yycharat(yylength() -1) == '='){
	  pushbackUntilBeforeBlank(1);
	  yybegin(WAITING_SCRIPTED_VARIABLE_NAME);
	  return SCRIPTED_VARIABLE_NAME_TOKEN;
	} else {
	  yybegin(WAITING_PROPERTY_END);
      return SCRIPTED_VARIABLE_REFERENCE_TOKEN;
	}
  }
}
<WAITING_SCRIPTED_VARIABLE_NAME>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" { inScriptedVariableName = false; depth--; beginNextState(); return RIGHT_BRACE;}
  "{" { inScriptedVariableName = false; depth++; beginNextState(); return LEFT_BRACE;}
  "]" { inScriptedVariableName = false; inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  "=" { inScriptedVariableName = false; yybegin(WAITING_SCRIPTED_VARIABLE_VALUE); return EQUAL_SIGN;}
  //scriptedVariableName可以包含参数
  {SCRIPTED_VARIABLE_NAME_TOKEN} { 
	  inScriptedVariableName = true; 
	  return SCRIPTED_VARIABLE_NAME_TOKEN;
  }
  "$" {
	  parameterPosition = ParameterPosition.SCRIPTED_VARIABLE_NAME; 
	  yybegin(WAITING_PARAMETER);
	  return PARAMETER_START;
  }
}
<WAITING_SCRIPTED_VARIABLE_VALUE> {
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  {BOOLEAN_TOKEN} {yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_SCRIPTED_VARIABLE_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_SCRIPTED_VARIABLE_END); return FLOAT_TOKEN;}
  {STRING_TOKEN} {yybegin(WAITING_SCRIPTED_VARIABLE_END); return STRING_TOKEN;}
  {QUOTED_STRING_TOKEN} {yybegin(WAITING_SCRIPTED_VARIABLE_END); return QUOTED_STRING_TOKEN;}
}
<WAITING_SCRIPTED_VARIABLE_END> {
  //只要有空白相间隔，就可以在写同一行
  {BLANK} {beginNextState(); onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
}

<WAITING_SCRIPTED_VARIABLE_REFERENCE_NAME>{
  {BLANK} {inScriptedVariableName = false; onBlank(); beginNextState(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {inScriptedVariableName = false; depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {inScriptedVariableName = false; depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inScriptedVariableName = false;  inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  {SCRIPTED_VARIABLE_NAME_TOKEN} {
	  inScriptedVariableName = true;
	  return SCRIPTED_VARIABLE_REFERENCE_TOKEN;
  }
  "$" {
	  parameterPosition = ParameterPosition.SCRIPTED_VARIABLE_REFERENCE_NAME; 
	  yybegin(WAITING_PARAMETER);
	  return PARAMETER_START;
  }
}

<WAITING_PROPERTY_KEY> {
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  "=" {yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;}
  "<" {yybegin(WAITING_PROPERTY_VALUE); return LT_SIGN;}
  ">" {yybegin(WAITING_PROPERTY_VALUE); return GT_SIGN;}
  "<=" {yybegin(WAITING_PROPERTY_VALUE); return LE_SIGN;}
  ">=" {yybegin(WAITING_PROPERTY_VALUE); return GE_SIGN;}
  "!="|"<>" {yybegin(WAITING_PROPERTY_VALUE); return NOT_EQUAL_SIGN;}
  //出于语法兼容性考虑，这里允许内联数学表达式
  "@["|"@\\[" { yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;}
  "@" {yybegin(WAITING_SCRIPTED_VARIABLE); return AT;}
  {CHECK_PROPERTY_KEY} {
 	  if(yycharat(0) == '"'){
		  pushbackUntilBeforeBlank(1);
 		  return QUOTED_PROPERTY_KEY_TOKEN;
 	  } else {
		   yypushback(yylength()); yybegin(WAITING_WILDCARD_KEY);
 	  }
  }
  {BOOLEAN_TOKEN} {yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_PROPERTY_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN;}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_END); return COLOR_TOKEN;}
  {WILDCARD_VALUE_TOKEN} { yypushback(yylength()); yybegin(WAITING_WILDCARD_VALUE);}
  {QUOTED_STRING_TOKEN} {yybegin(WAITING_PROPERTY_END); return QUOTED_STRING_TOKEN;}
}
<WAITING_PROPERTY_VALUE>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  "@["|"@\\[" { yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;}
  "@" {yybegin(WAITING_SCRIPTED_VARIABLE_REFERENCE_NAME); return AT;}
  //兼容处理
  {CHECK_PROPERTY_KEY} {
    if(yycharat(0) == '"'){
        pushbackUntilBeforeBlank(1);
        return QUOTED_PROPERTY_KEY_TOKEN;
    } else {
	   yypushback(yylength());
	   yybegin(WAITING_WILDCARD_KEY);
    }
  }
  {BOOLEAN_TOKEN} {yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_PROPERTY_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_PROPERTY_END);; return FLOAT_TOKEN;}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_END); return COLOR_TOKEN;}
  {WILDCARD_VALUE_TOKEN} { yypushback(yylength()); yybegin(WAITING_WILDCARD_VALUE);}
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
  "]" {inParameterCondition=false; beginNextState(); return RIGHT_BRACKET;}
  "=" {yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;}
  "<" {yybegin(WAITING_PROPERTY_VALUE); return LT_SIGN;}
  ">" {yybegin(WAITING_PROPERTY_VALUE); return GT_SIGN;}
  "<=" {yybegin(WAITING_PROPERTY_VALUE); return LE_SIGN;}
  ">=" {yybegin(WAITING_PROPERTY_VALUE); return GE_SIGN;}
  "!="|"<>" {yybegin(WAITING_PROPERTY_VALUE); return NOT_EQUAL_SIGN;}
  "$" {
      parameterPosition = ParameterPosition.KEY;
	  isWildcardContainsParameter=true; 
	  yybegin(WAITING_PARAMETER); 
	  return PARAMETER_START;
  }
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
  "]" {inParameterCondition=false; beginNextState(); return RIGHT_BRACKET;}
  "$" {
      parameterPosition = ParameterPosition.STRING;
	  isWildcardContainsParameter=true; 
	  yybegin(WAITING_PARAMETER); 
	  return PARAMETER_START;
  }
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

<WAITING_PARAMETER>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition=false; beginNextState(); return RIGHT_BRACKET;}
  "|" {yybegin(WAITING_PARAMETER_DEFAULT_VALUE); return PIPE;}
  "$" {beginNextStateForParameter(); return PARAMETER_END;}
  {PARAMETER_TOKEN} {return PARAMETER_TOKEN;}
}
<WAITING_PARAMETER_DEFAULT_VALUE>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  "$" {beginNextStateForParameter(); return PARAMETER_END;}
  {BOOLEAN_TOKEN} { yybegin(WAITING_PARAMETER_DEFAULT_VALUE_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_PARAMETER_DEFAULT_VALUE_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_PARAMETER_DEFAULT_VALUE_END);; return FLOAT_TOKEN;}
  {STRING_TOKEN} { yybegin(WAITING_PARAMETER_DEFAULT_VALUE_END); return STRING_TOKEN;} 
}
<WAITING_PARAMETER_DEFAULT_VALUE_END>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  "$" {beginNextStateForParameter(); return PARAMETER_END;}
}

<WAITING_PARAMETER_CONDITION>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "[" {yybegin(WAITING_PARAMETER_CONDITION_EXPRESSION); return NESTED_LEFT_BRACKET;}
  "]" {inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  //出于语法兼容性考虑，这里允许内联数学表达式
  "@["|"@\\[" { yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;}
  "@" {yybegin(WAITING_SCRIPTED_VARIABLE); return AT;}
  {CHECK_PROPERTY_KEY} {
 	  if(yycharat(0) == '"'){
		  pushbackUntilBeforeBlank(1);
 		  return QUOTED_PROPERTY_KEY_TOKEN;
 	  } else {
		  yypushback(yylength()); 
		  yybegin(WAITING_WILDCARD_KEY);
 	  }
  }
  {BOOLEAN_TOKEN} {yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;}
  {INT_TOKEN} {yybegin(WAITING_PROPERTY_END); return INT_TOKEN;}
  {FLOAT_TOKEN} {yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN;}
  {COLOR_TOKEN} {yybegin(WAITING_PROPERTY_END); return COLOR_TOKEN;}
  {WILDCARD_VALUE_TOKEN} { yypushback(yylength()); yybegin(WAITING_WILDCARD_VALUE);}
  {QUOTED_STRING_TOKEN} {yybegin(WAITING_PROPERTY_END); return QUOTED_STRING_TOKEN;}
}
<WAITING_PARAMETER_CONDITION_EXPRESSION>{
  {BLANK} {onBlank(); return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "!" {return NOT_SIGN;}
  "]" {inParameterCondition=true; yybegin(WAITING_PARAMETER_CONDITION); return NESTED_RIGHT_BRACKET;}
  {PARAMETER_TOKEN} {return ARGUMENT_ID;}
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
  "$" {parameterPosition=ParameterPosition.INLINE_MATH; yybegin(WAITING_PARAMETER); return PARAMETER_START;}
  "]" { beginNextState(); return INLINE_MATH_END;}
  {INT_NUMBER_TOKEN} {return INT_NUMBER_TOKEN;}
  {FLOAT_NUMBER_TOKEN} {return FLOAT_NUMBER_TOKEN;}
  {SCRIPTED_VARIABLE_NAME_TOKEN} {
	  return INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN;
  }
}

[^] {return BAD_CHARACTER;}
