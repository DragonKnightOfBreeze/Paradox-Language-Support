package icu.windea.pls.script.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import java.util.*;
import java.util.concurrent.atomic.*;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;

%%

%{
    private boolean leftAbsSign = true;
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final AtomicInteger templateStateRef = new AtomicInteger(-1);
    private final AtomicInteger parameterStateRef = new AtomicInteger(-1);

    public _ParadoxScriptLexer() {
        this((java.io.Reader)null);
    }

    private void enterState(Deque<Integer> stack, int state) {
        stack.offerLast(state);
        yybegin(state);
    }

    private void exitState(Deque<Integer> stack, int defaultState) {
        Integer state = stack.pollLast();
        if(state != null) {
            yybegin(state);
        } else {
            yybegin(defaultState);
        }
    }
    
    private void enterState(AtomicInteger stateRef, int state) {
        if(stateRef.get() == -1) {
            stateRef.set(state);
        }
    }
    
    private void exitState(AtomicInteger stateRef) {
        int state = stateRef.getAndSet(-1);
        if(state != -1) {
            if(stateRef == templateStateRef && state != IN_INLINE_MATH) {
                state = stack.isEmpty() ? YYINITIAL : stack.peekLast();
            } 
            yybegin(state);
        }
    }
    
    private boolean exitStateForErrorToken(AtomicInteger stateRef) {
        int state = stateRef.getAndSet(-1);
        if(state != -1) {
            if(stateRef == templateStateRef && state != IN_INLINE_MATH) {
                state = stack.isEmpty() ? YYINITIAL : stack.peekLast();
            } 
            yybegin(state);
        }
        if(state != -1) {
            yypushback(yylength());
            return true;
        } else {
            return false;
        }
    }
    
    private void recoverState(AtomicInteger stateRef) {
        int state = stateRef.get();
        if(state != -1) {
            yybegin(state);
        }
    }
%}

%public
%class _ParadoxScriptLexer
%implements FlexLexer
%function advance
%type IElementType

%s IN_SCRIPTED_VARIABLE
%s IN_SCRIPTED_VARIABLE_NAME
%s IN_SCRIPTED_VARIABLE_VALUE

%s IN_PROPERTY_OR_VALUE
%s IN_PROPERTY_VALUE

%s IN_KEY
%s IN_QUOTED_KEY
%s IN_STRING
%s IN_QUOTED_STRING

%s IN_SCRIPTED_VARIABLE_REFERENCE
%s IN_SCRIPTED_VARIABLE_REFERENCE_NAME

%s IN_PARAMETER
%s IN_PARAMETER_DEFAULT_VALUE
%s IN_PARAMETER_DEFAULT_VALUE_END

%s IN_PARAMETER_CONDITION
%s IN_PARAMETER_CONDITION_EXPRESSION
%s IN_PARAMETER_CONDITION_BODY

%s IN_INLINE_MATH
%s IN_INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE
%s IN_INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_NAME

%unicode

EOL=\s*\R
WHITE_SPACE=[\s&&[^\r\n]]+
BLANK=\s+
COMMENT=#[^\r\n]*

//leading number is not permitted for parameter names
PARAMETER_TOKEN=[a-zA-Z_][a-zA-Z0-9_]*

//leading number is not permitted for scripted variable names
SCRIPTED_VARIABLE_NAME_TOKEN=[a-zA-Z0-9_]+
CHECK_SCRIPTED_VARIABLE_NAME=[a-zA-Z_$\[][^@#={}\s\"]*(\s*=)?
CHECK_SCRIPTED_VARIABLE_REFERENCE=[a-zA-Z_$\[][^@#={}\s\"]*
CHECK_INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE=[a-zA-Z_][^@#={}\s\"]*

CHECK_PROPERTY_KEY=({WILDCARD_PROPERTY_KEY_TOKEN}|{WILDCARD_QUOTED_PROPERTY_KEY_TOKEN})\s*[!=<>]
WILDCARD_PROPERTY_KEY_TOKEN=[^@#={}\s\"][^#={}\s\"]*\"?
WILDCARD_QUOTED_PROPERTY_KEY_TOKEN=\"([^\"\r\n\\]|\\.)*\"?
PROPERTY_KEY_TOKEN=[^@#$={}\[\]\s\"][^#$={}\[\]\s\"]*\"?
QUOTED_PROPERTY_KEY_TOKEN=([^\"\r\n\\$]|\\.)+

BOOLEAN_TOKEN=(yes)|(no)
INT_NUMBER_TOKEN=[0-9]+ //leading zero is permitted
INT_TOKEN=[+-]?{INT_NUMBER_TOKEN}
FLOAT_NUMBER_TOKEN=[0-9]*(\.[0-9]+) //leading zero is permitted
FLOAT_TOKEN=[+-]?{FLOAT_NUMBER_TOKEN}
COLOR_TOKEN=(rgb|hsv)[ \t]*\{[\d.\s&&[^\r\n]]*}

CHECK_STRING={WILDCARD_STRING_TOKEN}|{WILDCARD_QUOTED_STRING_TOKEN}
WILDCARD_STRING_TOKEN=[^@#={}\s\"][^#={}\s\"]*\"?
WILDCARD_QUOTED_STRING_TOKEN=\"([^\"\\]|\\[\s\S])*\"?
STRING_TOKEN=[^@#$={}\[\]\s\"][^#$={}\[\]\s\"]*\"?
QUOTED_STRING_TOKEN=([^\"\\$]|\\[\s\S])+

SNIPPET_TOKEN=[^#$={}\[\]\s]+ //compatible with leading "@"

%%

<YYINITIAL> {
    "{" { enterState(stack, YYINITIAL); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, YYINITIAL); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    "=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN; }
    "<" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return LT_SIGN; }
    ">" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return GT_SIGN; }
    "<=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return LE_SIGN; }
    ">=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return GE_SIGN; }
    "!="|"<>" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN; }
    "?=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return QUESTION_EQUAL_SIGN; }
    "@" { yybegin(IN_SCRIPTED_VARIABLE); return AT; }
    {COMMENT} { return COMMENT; }
}

<IN_SCRIPTED_VARIABLE> {
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    "=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN; }
    "<" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return LT_SIGN; }
    ">" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return GT_SIGN; }
    "<=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return LE_SIGN; }
    ">=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return GE_SIGN; }
    "!="|"<>" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN; }
    "?=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return QUESTION_EQUAL_SIGN; }
    {CHECK_SCRIPTED_VARIABLE_NAME} {
        //如果匹配到的文本以等号结尾，则作为scriptedVariable进行解析，否则作为scriptedVariableReference进行解析
        if(yycharat(yylength() -1) == '='){
            yypushback(yylength());
            enterState(templateStateRef, yystate());
            yybegin(IN_SCRIPTED_VARIABLE_NAME);
        } else {
            yypushback(yylength());
            enterState(templateStateRef, yystate());
            yybegin(IN_SCRIPTED_VARIABLE_REFERENCE_NAME);
        }
    }
    {COMMENT} { return COMMENT; }
}

<IN_SCRIPTED_VARIABLE_NAME> {
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    "=" { exitState(templateStateRef); yybegin(IN_SCRIPTED_VARIABLE_VALUE); return EQUAL_SIGN; }
    "$" { enterState(parameterStateRef, yystate()); yybegin(IN_PARAMETER); return PARAMETER_START; }
    {SCRIPTED_VARIABLE_NAME_TOKEN} { return SCRIPTED_VARIABLE_NAME_TOKEN; }
    {COMMENT} { return COMMENT; }
}
<IN_SCRIPTED_VARIABLE_VALUE> {
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    "@["|"@\\[" { enterState(stack, yystate()); yybegin(IN_INLINE_MATH); return INLINE_MATH_START; }
    {BOOLEAN_TOKEN} { enterState(templateStateRef, yystate()); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { enterState(templateStateRef, yystate()); return INT_TOKEN; }
    {FLOAT_TOKEN} { enterState(templateStateRef, yystate()); return FLOAT_TOKEN; }
    {STRING_TOKEN} { enterState(templateStateRef, yystate()); return STRING_TOKEN; }
    {WILDCARD_QUOTED_STRING_TOKEN} { enterState(templateStateRef, yystate()); return STRING_TOKEN; }
    {COMMENT} { return COMMENT; }
}

<IN_SCRIPTED_VARIABLE_REFERENCE> {
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    {CHECK_SCRIPTED_VARIABLE_REFERENCE} {
        yypushback(yylength());
        enterState(templateStateRef, yystate());
        yybegin(IN_SCRIPTED_VARIABLE_REFERENCE_NAME);
    }
    {COMMENT} { return COMMENT; }
}
<IN_SCRIPTED_VARIABLE_REFERENCE_NAME> {
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    "$" { enterState(parameterStateRef, yystate()); yybegin(IN_PARAMETER); return PARAMETER_START; }
    {SCRIPTED_VARIABLE_NAME_TOKEN} { return SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
    {COMMENT} { return COMMENT; }
}

<IN_PROPERTY_OR_VALUE> {
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    "=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN; }
    "<" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return LT_SIGN; }
    ">" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return GT_SIGN; }
    "<=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return LE_SIGN; }
    ">=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return GE_SIGN; }
    "!="|"<>" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN; }
    "?=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return QUESTION_EQUAL_SIGN; }
    "@" { yybegin(IN_SCRIPTED_VARIABLE); return AT; }
    {COMMENT} { return COMMENT; }
}
<IN_PROPERTY_VALUE> {
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    "@" { yybegin(IN_SCRIPTED_VARIABLE_REFERENCE); return AT; }
    {COMMENT} { return COMMENT; }
}

<IN_PARAMETER> {
    \s|"#" { yypushback(yylength()); exitState(parameterStateRef); }
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    "|" { yybegin(IN_PARAMETER_DEFAULT_VALUE); return PIPE; }
    "$" { exitState(parameterStateRef); return PARAMETER_END; }
    {PARAMETER_TOKEN} { return PARAMETER_TOKEN; }
}
<IN_PARAMETER_DEFAULT_VALUE> {
    \s|"#" { yypushback(yylength()); exitState(parameterStateRef); }
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    "$" { exitState(parameterStateRef); return PARAMETER_END; }
    {SNIPPET_TOKEN} { yybegin(IN_PARAMETER_DEFAULT_VALUE_END); return SNIPPET_TOKEN; } 
}
<IN_PARAMETER_DEFAULT_VALUE_END> {
    \s|"#" { yypushback(yylength()); exitState(parameterStateRef); }
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    "$" { exitState(parameterStateRef); return PARAMETER_END; }
}

<IN_PARAMETER_CONDITION> {
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { yybegin(IN_PARAMETER_CONDITION_EXPRESSION); return NESTED_LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    {COMMENT} { return COMMENT; }
}
<IN_PARAMETER_CONDITION_EXPRESSION> {
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "]" { yybegin(IN_PARAMETER_CONDITION_BODY); return NESTED_RIGHT_BRACKET; }
    "!" { return NOT_SIGN; }
    {PARAMETER_TOKEN} { return CONDITION_PARAMETER_TOKEN; }
    {COMMENT} { return COMMENT; }
    {BLANK} { return WHITE_SPACE; }
}
<IN_PARAMETER_CONDITION_BODY> {
    "{" { enterState(stack, IN_PARAMETER_CONDITION_BODY); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, IN_PARAMETER_CONDITION_BODY); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    {COMMENT} { return COMMENT; }
}

<IN_INLINE_MATH> {
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }    
    "]" { exitState(stack, YYINITIAL); return INLINE_MATH_END; }
    "|" {
        if(leftAbsSign) {
            leftAbsSign = false; 
            return LABS_SIGN;
        } else {
            leftAbsSign = true;
            return RABS_SIGN;
        }
    }
    "(" { return LP_SIGN; }
    ")" { return RP_SIGN; }
    "+" { return PLUS_SIGN; }
    "-" { return MINUS_SIGN; }
    "*" { return TIMES_SIGN; }
    "/" { return DIV_SIGN; }
    "%" { return MOD_SIGN; }
    "$" { enterState(parameterStateRef, yystate()); yybegin(IN_PARAMETER); return PARAMETER_START; }
    {INT_NUMBER_TOKEN} { return INT_NUMBER_TOKEN; }
    {FLOAT_NUMBER_TOKEN} { return FLOAT_NUMBER_TOKEN; }
    {CHECK_INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE} {
        yypushback(yylength());
        enterState(templateStateRef, yystate());
        yybegin(IN_INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_NAME);
    }
    {COMMENT} { return COMMENT; }
    {BLANK} { return WHITE_SPACE; }
}
<IN_INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_NAME> {
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    "$" { enterState(parameterStateRef, yystate()); yybegin(IN_PARAMETER); return PARAMETER_START; }
    {SCRIPTED_VARIABLE_NAME_TOKEN} { return INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN; }
    {COMMENT} { return COMMENT; }
}

<YYINITIAL, IN_PROPERTY_OR_VALUE, IN_PROPERTY_VALUE, IN_PARAMETER_CONDITION_BODY> {
    "@["|"@\\[" { enterState(stack, yystate()); leftAbsSign = true; yybegin(IN_INLINE_MATH); return INLINE_MATH_START; }
    {CHECK_PROPERTY_KEY} {
        boolean leftQuoted = yycharat(0) == '"';
        if(leftQuoted) {
            yypushback(yylength() - 1);
            enterState(templateStateRef, yystate());
            yybegin(IN_QUOTED_KEY);
            return PROPERTY_KEY_TOKEN;
        } else {
            yypushback(yylength());
            enterState(templateStateRef, yystate());
            yybegin(IN_KEY);
        }
    }
    {BOOLEAN_TOKEN} { enterState(templateStateRef, yystate());  return BOOLEAN_TOKEN; }
    {INT_TOKEN} { enterState(templateStateRef, yystate());  return INT_TOKEN; }
    {FLOAT_TOKEN} { enterState(templateStateRef, yystate());  return FLOAT_TOKEN; }
    {COLOR_TOKEN} { enterState(templateStateRef, yystate());  return COLOR_TOKEN; }
    {CHECK_STRING} {
        boolean leftQuoted = yycharat(0) == '"';
        if(leftQuoted) {
            yypushback(yylength() - 1);
            enterState(templateStateRef, yystate());
            yybegin(IN_QUOTED_STRING);
            return STRING_TOKEN;
        } else {
            yypushback(yylength());
            enterState(templateStateRef, yystate());
            yybegin(IN_STRING);
        }
    }
}

<IN_KEY> {
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    "=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN; }
    "<" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return LT_SIGN; }
    ">" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return GT_SIGN; }
    "<=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return LE_SIGN; }
    ">=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return GE_SIGN; }
    "!="|"<>" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN; }
    "?=" { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return QUESTION_EQUAL_SIGN; }
    "$" { enterState(parameterStateRef, yystate()); yybegin(IN_PARAMETER); return PARAMETER_START; }
    {PROPERTY_KEY_TOKEN} { return PROPERTY_KEY_TOKEN; }
    {COMMENT} { return COMMENT; }
    {BLANK} { exitState(templateStateRef); return WHITE_SPACE; }
}
<IN_QUOTED_KEY> {
    {EOL} { exitState(templateStateRef); return WHITE_SPACE; }
    "$" { enterState(parameterStateRef, yystate()); yybegin(IN_PARAMETER); return PARAMETER_START; }
    \"|{QUOTED_PROPERTY_KEY_TOKEN}\"? {
        boolean rightQuoted = yycharat(yylength() -1) == '"';
        if(rightQuoted) {
            exitState(templateStateRef);
        }
        return PROPERTY_KEY_TOKEN;
    }
}

<IN_STRING> {
    "{" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE; }
    "}" { exitState(stack, YYINITIAL); return RIGHT_BRACE; }
    "[" { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET; }
    "]" { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET; }
    "$" { enterState(parameterStateRef, yystate()); yybegin(IN_PARAMETER); return PARAMETER_START; }
    {STRING_TOKEN} { return STRING_TOKEN; }
    {COMMENT} { return COMMENT; }
    {BLANK} { exitState(templateStateRef); return WHITE_SPACE; }
}
<IN_QUOTED_STRING> {
    //quoted multiline string is allowed
    //{EOL} { exitState(templateStateRef); return WHITE_SPACE; }
    "$" { enterState(parameterStateRef, yystate()); yybegin(IN_PARAMETER); return PARAMETER_START; }
    \"|{QUOTED_STRING_TOKEN}\"? {
        boolean rightQuoted = yycharat(yylength() -1) == '"';
        if(rightQuoted) {
            exitState(templateStateRef);
        }
        return STRING_TOKEN;
    }
}

<YYINITIAL> {
    {BLANK} { exitState(templateStateRef); return WHITE_SPACE; }
}
<IN_SCRIPTED_VARIABLE, IN_SCRIPTED_VARIABLE_NAME, IN_SCRIPTED_VARIABLE_VALUE,
IN_SCRIPTED_VARIABLE_REFERENCE, IN_SCRIPTED_VARIABLE_REFERENCE_NAME,
IN_PROPERTY_OR_VALUE, IN_PROPERTY_VALUE,
IN_PARAMETER_CONDITION, IN_PARAMETER_CONDITION_BODY,
IN_INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_NAME> {
    {BLANK} { exitState(templateStateRef); return WHITE_SPACE; }
    [^] {
        boolean r = exitStateForErrorToken(templateStateRef);
        if(!r) return BAD_CHARACTER;
    }
}

[^] { return BAD_CHARACTER; }
