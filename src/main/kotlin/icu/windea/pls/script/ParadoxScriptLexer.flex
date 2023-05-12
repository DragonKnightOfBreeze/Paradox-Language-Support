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

%state WAITING_PROPERTY_OR_VALUE
%state WAITING_PROPERTY
%state WAITING_PROPERTY_VALUE

%state WAITING_KEY
%state WAITING_STRING

%state WAITING_SCRIPTED_VARIABLE_REFERENCE
%state WAITING_SCRIPTED_VARIABLE_REFERENCE_NAME

%state WAITING_PARAMETER
%state WAITING_PARAMETER_DEFAULT_VALUE
%state WAITING_PARAMETER_DEFAULT_VALUE_END

%state WAITING_PARAMETER_CONDITION
%state WAITING_PARAMETER_CONDITION_EXPRESSION

%state WAITING_INLINE_MATH

%{
	private enum ParameterPosition {
	    NONE, SCRIPTED_VARIABLE_NAME, SCRIPTED_VARIABLE_REFERENCE, KEY, STRING, INLINE_MATH;
	}

    private int depth = 0;
    private ParameterPosition parameterPosition = ParameterPosition.NONE;
	private boolean scriptedVariableValueStarted = false;
	private boolean valueStarted = false;
    private boolean inParameterCondition = false;
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
	            yybegin(WAITING_PROPERTY_OR_VALUE);
            }
	    }
    }
    
    private void beginNextStateForParameter(){
		if(parameterPosition == ParameterPosition.SCRIPTED_VARIABLE_NAME) {
			yybegin(WAITING_SCRIPTED_VARIABLE_NAME);
		} else if(parameterPosition == ParameterPosition.SCRIPTED_VARIABLE_REFERENCE) {
            yybegin(WAITING_SCRIPTED_VARIABLE_REFERENCE_NAME);
        } else if(parameterPosition == ParameterPosition.KEY){
		    yybegin(WAITING_KEY);
	    } else if(parameterPosition == ParameterPosition.STRING){
		    yybegin(WAITING_STRING);
	    } else if(parameterPosition == ParameterPosition.INLINE_MATH) {
            yybegin(WAITING_INLINE_MATH);
        } else {
            beginNextState(); //unexpected
        }
    }
	
	private boolean isParameterized() {
		int length = yylength();
		for(int i = 0; i < length; i++) {
		  char c1 = yycharat(i);
		  if(c1 == '$') return true;
		}
		return false;
	}
	
    private void pushbackUntilBeforeBlank(int from){
        //回退到末尾可能出现的空白之前
        int length = yylength();
        int i;
        for (i = from; i < length ; i++) {
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

PARAMETER_TOKEN=[a-zA-Z_][a-zA-Z0-9_]*

WILDCARD_SCRIPTED_VARIABLE_NAME_TOKEN=[^#@={}\[\]\s\"]+
SCRIPTED_VARIABLE_NAME_TOKEN=[a-zA-Z0-9_]+
CHECK_SCRIPTED_VARIABLE_NAME={WILDCARD_SCRIPTED_VARIABLE_NAME_TOKEN}(\s*=)? //判断接下来是变量还是变量引用的名字
CHECK_SCRIPTED_VARIABLE_REFERENCE={WILDCARD_SCRIPTED_VARIABLE_NAME_TOKEN}

WILDCARD_KEY_TOKEN=[^#@={}\[\]\s\"][^#={}\[\]\s]*
PROPERTY_KEY_TOKEN=[^#@$={}\[\]\s\"][^#$={}\[\]\s]*
QUOTED_PROPERTY_KEY_TOKEN=\"([^\"(\r\n\\]|\\.)*?\"?
CHECK_PROPERTY_KEY=({WILDCARD_KEY_TOKEN}|{QUOTED_PROPERTY_KEY_TOKEN})\s*[!=<>] //判断接下来是否是属性的键

BOOLEAN_TOKEN=(yes)|(no)
INT_NUMBER_TOKEN=[0-9]+ //leading zero is permitted
INT_TOKEN=[+-]?{INT_NUMBER_TOKEN}
FLOAT_NUMBER_TOKEN=[0-9]*(\.[0-9]+) //leading zero is permitted
FLOAT_TOKEN=[+-]?{FLOAT_NUMBER_TOKEN}
COLOR_TOKEN=(rgb|hsv)[ \t]*\{[\d.\s&&[^\r\n]]*}

WILDCARD_STRING_TOKEN=[^#@={}\[\]\s\"][^#={}\[\]\s]*
STRING_TOKEN=[^#@$={}\[\]\s\"][^#$={}\[\]\s]*
QUOTED_STRING_TOKEN=\"([^\"\r\n\\]|\\.)*?\"?
CHECK_STRING={WILDCARD_STRING_TOKEN}|{QUOTED_STRING_TOKEN} //判断接下来是否是字符串

%%

<YYINITIAL> {
  {BLANK} {
	  if(valueStarted) {
		  valueStarted = false;
		  beginNextState();
	  }
	  return WHITE_SPACE;
  }
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
  "@" {yybegin(WAITING_SCRIPTED_VARIABLE); return AT;}
  {CHECK_PROPERTY_KEY} {
      if(yycharat(0) == '"'){
          pushbackUntilBeforeBlank(1);
          return PROPERTY_KEY_TOKEN;
      } else {
          if(isParameterized()) {
              parameterPosition = ParameterPosition.KEY; 
          }
          yypushback(yylength()); 
          yybegin(WAITING_KEY);
      }
  }
  {BOOLEAN_TOKEN} {valueStarted=true; return BOOLEAN_TOKEN;}
  {INT_TOKEN} {valueStarted=true; return INT_TOKEN;}
  {FLOAT_TOKEN} {valueStarted=true; return FLOAT_TOKEN;}
  {COLOR_TOKEN} {valueStarted=true; return COLOR_TOKEN;}
  {CHECK_STRING} {
      if(yycharat(0) == '"') {
		  valueStarted=true;
		  return STRING_TOKEN;
	  } else {
          if(isParameterized()) {
              parameterPosition = ParameterPosition.STRING; 
          }
		  yypushback(yylength());
		  yybegin(WAITING_STRING);
	  }
  }
}

<WAITING_SCRIPTED_VARIABLE>{
  {BLANK} {beginNextState(); return WHITE_SPACE;}
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
  {CHECK_SCRIPTED_VARIABLE_NAME} {
        //如果匹配到的文本以等号结尾，则作为scriptedVariable进行解析，否则作为scriptedVariableReference解析
        if(yycharat(yylength() -1) == '='){
            if(isParameterized()) {
                parameterPosition = ParameterPosition.SCRIPTED_VARIABLE_NAME; 
            }
            yypushback(yylength());
            yybegin(WAITING_SCRIPTED_VARIABLE_NAME);
        } else {
            if(isParameterized()) {
                parameterPosition = ParameterPosition.SCRIPTED_VARIABLE_REFERENCE; 
            }
            yypushback(yylength());
            yybegin(WAITING_SCRIPTED_VARIABLE_REFERENCE_NAME);
        }
    }
}
<WAITING_SCRIPTED_VARIABLE_NAME>{
  {BLANK} {
	  parameterPosition = ParameterPosition.NONE;
      return WHITE_SPACE;
  }
  {COMMENT} {return COMMENT;}
  "}" { depth--; beginNextState(); return RIGHT_BRACE;}
  "{" { depth++; beginNextState(); return LEFT_BRACE;}
  "]" { inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  "=" { yybegin(WAITING_SCRIPTED_VARIABLE_VALUE); return EQUAL_SIGN;}
  //scriptedVariableName可以包含参数
  "$" {
	  yybegin(WAITING_PARAMETER);
	  return PARAMETER_START;
  }
  {SCRIPTED_VARIABLE_NAME_TOKEN} {
      if(parameterPosition == ParameterPosition.SCRIPTED_VARIABLE_NAME) {
          return SCRIPTED_VARIABLE_NAME_SNIPPET;
      } else {
		  return SCRIPTED_VARIABLE_NAME_TOKEN;
      }
  }
}
<WAITING_SCRIPTED_VARIABLE_VALUE> {
  {BLANK} {
      if(scriptedVariableValueStarted) {
          scriptedVariableValueStarted = false;
          beginNextState();
      }
	  return WHITE_SPACE;
  }
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  {BOOLEAN_TOKEN} {scriptedVariableValueStarted=true; return BOOLEAN_TOKEN;}
  {INT_TOKEN} {scriptedVariableValueStarted=true; return INT_TOKEN;}
  {FLOAT_TOKEN} {scriptedVariableValueStarted=true; return FLOAT_TOKEN;}
  {STRING_TOKEN} {scriptedVariableValueStarted=true; return STRING_TOKEN;}
  {QUOTED_STRING_TOKEN} {scriptedVariableValueStarted=true; return STRING_TOKEN;}
}

<WAITING_SCRIPTED_VARIABLE_REFERENCE>{
  {BLANK} {beginNextState();return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  {CHECK_SCRIPTED_VARIABLE_REFERENCE} {
      if(isParameterized()) {
          parameterPosition = ParameterPosition.SCRIPTED_VARIABLE_REFERENCE; 
      }
      yypushback(yylength());
      yybegin(WAITING_SCRIPTED_VARIABLE_REFERENCE_NAME);
 }
}
<WAITING_SCRIPTED_VARIABLE_REFERENCE_NAME>{
  {BLANK} {
	  parameterPosition = ParameterPosition.NONE;
	  beginNextState();
	  return WHITE_SPACE;
  }
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  //scriptedVariableName可以包含参数
  "$" {
	  yybegin(WAITING_PARAMETER);
	  return PARAMETER_START;
  }
  {SCRIPTED_VARIABLE_NAME_TOKEN} {
      if(parameterPosition == ParameterPosition.SCRIPTED_VARIABLE_REFERENCE) {
          return SCRIPTED_VARIABLE_REFERENCE_SNIPPET;
      } else {
		  return SCRIPTED_VARIABLE_REFERENCE_TOKEN;
      }
  }
}

<WAITING_PROPERTY_OR_VALUE> {
  {BLANK} {
	  if(valueStarted) {
		  valueStarted = false;
		  beginNextState();
	  }
	  return WHITE_SPACE;
  }
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
  "[" {inParameterCondition=true; yybegin(WAITING_PARAMETER_CONDITION); return LEFT_BRACKET;}
  {CHECK_PROPERTY_KEY} {
      if(yycharat(0) == '"'){
          pushbackUntilBeforeBlank(1);
          return PROPERTY_KEY_TOKEN;
      } else {
          if(isParameterized()) {
              parameterPosition = ParameterPosition.KEY; 
          }
          yypushback(yylength()); 
          yybegin(WAITING_KEY);
      }
  }
  {BOOLEAN_TOKEN} {valueStarted=true; return BOOLEAN_TOKEN;}
  {INT_TOKEN} {valueStarted=true; return INT_TOKEN;}
  {FLOAT_TOKEN} {valueStarted=true; return FLOAT_TOKEN;}
  {COLOR_TOKEN} {valueStarted=true; return COLOR_TOKEN;}
  {CHECK_STRING} { 
      if(yycharat(0) == '"') {
		  valueStarted=true;
		  return STRING_TOKEN;
	  } else {
          if(isParameterized()) {
              parameterPosition = ParameterPosition.STRING; 
          }
		  yypushback(yylength());
		  yybegin(WAITING_STRING);
	  }
  }
}
<WAITING_PROPERTY_VALUE>{
  {BLANK} {
	  if(valueStarted) {
		  valueStarted = false;
		  beginNextState();
	  }
	  return WHITE_SPACE;
  }
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  "@["|"@\\[" { yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;}
  "@" {yybegin(WAITING_SCRIPTED_VARIABLE_REFERENCE); return AT;}
  //兼容处理
  {CHECK_PROPERTY_KEY} {
      if(yycharat(0) == '"'){
          pushbackUntilBeforeBlank(1);
          return PROPERTY_KEY_TOKEN;
      } else {
          if(isParameterized()) {
              parameterPosition = ParameterPosition.KEY; 
          }
          yypushback(yylength()); 
          yybegin(WAITING_KEY);
      }
    }
  {BOOLEAN_TOKEN} {valueStarted=true; return BOOLEAN_TOKEN;}
  {INT_TOKEN} {valueStarted=true; return INT_TOKEN;}
  {FLOAT_TOKEN} {valueStarted=true; return FLOAT_TOKEN;}
  {COLOR_TOKEN} {valueStarted=true; return COLOR_TOKEN;}
  {CHECK_STRING} { 
      if(yycharat(0) == '"') {
		  valueStarted=true;
		  return STRING_TOKEN;
	  } else {
          if(isParameterized()) {
              parameterPosition = ParameterPosition.STRING; 
          }
		  yypushback(yylength());
		  yybegin(WAITING_STRING);
	  }
  }
}

<WAITING_KEY>{
  {BLANK} {
      parameterPosition = ParameterPosition.NONE;
	  return WHITE_SPACE;
  }
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
	  yybegin(WAITING_PARAMETER); 
	  return PARAMETER_START;
  }
  {PROPERTY_KEY_TOKEN} {
	  if(parameterPosition == ParameterPosition.KEY) {
          return PROPERTY_KEY_SNIPPET;
	  } else {
		  return PROPERTY_KEY_TOKEN;
	  }
  }
}

<WAITING_STRING>{
  {BLANK} {
	  if(valueStarted) {
          parameterPosition = ParameterPosition.NONE;
		  valueStarted = false;
		  beginNextState();
	  }
	  return WHITE_SPACE;
  }
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition=false; beginNextState(); return RIGHT_BRACKET;}      
  "$" {
	  valueStarted=true;
	  yybegin(WAITING_PARAMETER); 
	  return PARAMETER_START;
  }
  {STRING_TOKEN} {
	  valueStarted=true;
	  if(parameterPosition == ParameterPosition.STRING) {
		  return STRING_SNIPPET;
	  } else {
	      return STRING_TOKEN;
	  }
  }
}

<WAITING_PARAMETER>{
  {BLANK} {return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition=false; beginNextState(); return RIGHT_BRACKET;}
  "|" {yybegin(WAITING_PARAMETER_DEFAULT_VALUE); return PIPE;}
  "$" {beginNextStateForParameter(); return PARAMETER_END;}
  {PARAMETER_TOKEN} { return PARAMETER_TOKEN; }
}
<WAITING_PARAMETER_DEFAULT_VALUE>{
  {BLANK} {return WHITE_SPACE;}
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
  {BLANK} {return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "]" {inParameterCondition = false; beginNextState(); return RIGHT_BRACKET;}
  "$" {beginNextStateForParameter(); return PARAMETER_END;}
}

<WAITING_PARAMETER_CONDITION>{
  {BLANK} {
	  if(valueStarted) {
		  valueStarted = false;
		  beginNextState();
	  }
	  return WHITE_SPACE;
  }
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
          return PROPERTY_KEY_TOKEN;
      } else {
          if(isParameterized()) {
              parameterPosition = ParameterPosition.KEY; 
          }
          yypushback(yylength()); 
          yybegin(WAITING_KEY);
      }
  }
  {BOOLEAN_TOKEN} {valueStarted=true; return BOOLEAN_TOKEN;}
  {INT_TOKEN} {valueStarted=true; return INT_TOKEN;}
  {FLOAT_TOKEN} {valueStarted=true; return FLOAT_TOKEN;}
  {COLOR_TOKEN} {valueStarted=true; return COLOR_TOKEN;}
  {CHECK_STRING} { 
      if(yycharat(0) == '"') {
		  valueStarted=true;
		  return STRING_TOKEN;
	  } else {
          if(isParameterized()) {
              parameterPosition = ParameterPosition.STRING; 
          }
		  yypushback(yylength());
		  yybegin(WAITING_STRING);
	  }
  }
}
<WAITING_PARAMETER_CONDITION_EXPRESSION>{
  {BLANK} {return WHITE_SPACE;}
  {COMMENT} {return COMMENT;}
  "}" {depth--; beginNextState(); return RIGHT_BRACE;}
  "{" {depth++; beginNextState(); return LEFT_BRACE;}
  "!" {return NOT_SIGN;}
  "]" {inParameterCondition=true; yybegin(WAITING_PARAMETER_CONDITION); return NESTED_RIGHT_BRACKET;}
  {PARAMETER_TOKEN} { return CONDITION_PARAMETER_TOKEN; }
}

<WAITING_INLINE_MATH>{
  {BLANK} {return WHITE_SPACE;}
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
  "$" { parameterPosition=ParameterPosition.INLINE_MATH; yybegin(WAITING_PARAMETER); return PARAMETER_START;}
  "]" { beginNextState(); return INLINE_MATH_END;}
  {INT_NUMBER_TOKEN} {return INT_NUMBER_TOKEN;}
  {FLOAT_NUMBER_TOKEN} {return FLOAT_NUMBER_TOKEN;}
  {SCRIPTED_VARIABLE_NAME_TOKEN} {
	  return INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN;
  }
}

[^] {return BAD_CHARACTER;}
