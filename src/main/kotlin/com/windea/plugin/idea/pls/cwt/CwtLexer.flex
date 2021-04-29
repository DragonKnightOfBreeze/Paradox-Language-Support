package com.windea.plugin.idea.pls.cwt.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.windea.plugin.idea.pls.cwt.psi.CwtTypes.*;

%%

%{
  public CwtLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class CwtLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%state WAITING_PROPERTY_KEY
%state WATIING_PROPERTY_SEPARATOR
%state WAITING_PROPERTY_VALUE
%state WAITING_PROPERTY_VALUE_END
%state WAITING_PROPERTY_END
%state WAITING_OPTION_KEY
%state WATIING_OPTION_SEPARATOR
%state WAITING_OPTION_VALUE
%state WAITING_OPTION_VALUE_END
%state WAITING_OPTION_END
%state WAITING_OPTION
%state WAITING_DOCUMENTATION

EOL=\R
BLANK=[ \t\n\x0B\f\r]+
SPACE=[ \t\x08\f]+

COMMENT=(#)|(#[^#\r\n][^\r\n]*)
//OPTION_COMMENT=##[^\r\n]*
//DOCUMENTATION_COMMENT=###[^\r\n]*

PROPERTY_KEY_TOKEN=([^#={}\s][^={}\s]*)|(\"([^\"\\\r\n]|\\.)*\")
OPTION_KEY_TOKEN=([^#={}\s][^={}\s]*)|(\"([^\"\\\r\n]|\\.)*\")
BOOLEAN_TOKEN=(yes)|(no)
INT_TOKEN=[+-]?(0|[1-9][0-9]*)
FLOAT_TOKEN=[+-]?(0|[1-9][0-9]*)(\.[0-9]+)
STRING_TOKEN=([^#={}\s\"][^={}\s\"]*)|(\"([^\"\\\r\n]|\\.)*\")
DOCUMENTATION_TOKEN=([^\r\n]*)

IS_PROPERTY_KEY=({PROPERTY_KEY_TOKEN})?({SPACE})?=
IS_OPTION_KEY=({OPTION_KEY_TOKEN})?({SPACE})?=

%%
<YYINITIAL> {
  {BLANK} { return WHITE_SPACE; }

  "{" {return LEFT_BRACE;}
  "}" {return RIGHT_BRACE;}
  
  "###" { yybegin(WAITING_DOCUMENTATION); return DOCUMENTATION_START; }
  "##" {  yybegin(WAITING_OPTION); return OPTION_START; }
  {COMMENT} {return COMMENT; }
  
  {IS_PROPERTY_KEY} {yypushback(yylength()); yybegin(WAITING_PROPERTY_KEY);}
  
  {BOOLEAN_TOKEN} { yybegin(WAITING_PROPERTY_VALUE_END); return BOOLEAN_TOKEN; }
  {INT_TOKEN} { yybegin(WAITING_PROPERTY_VALUE_END); return INT_TOKEN; }
  {FLOAT_TOKEN} { yybegin(WAITING_PROPERTY_VALUE_END); return FLOAT_TOKEN; }
  {STRING_TOKEN} {yybegin(WAITING_PROPERTY_VALUE_END); return STRING_TOKEN;}
}
  
<WAITING_PROPERTY_KEY>{
  {EOL} { yybegin(YYINITIAL); return WHITE_SPACE;}
  {SPACE} {return WHITE_SPACE;}
  
  "{" {yybegin(YYINITIAL); return LEFT_BRACE;}
  "}" {yybegin(YYINITIAL); return RIGHT_BRACE;}
  
  {PROPERTY_KEY_TOKEN} {yybegin(WATIING_PROPERTY_SEPARATOR); return PROPERTY_KEY_TOKEN;}
}

<WATIING_PROPERTY_SEPARATOR>{
  {EOL} { yybegin(YYINITIAL); return WHITE_SPACE;}
  {SPACE} {return WHITE_SPACE;}
  
  "{" {yybegin(YYINITIAL); return LEFT_BRACE;}
  "}" {yybegin(YYINITIAL); return RIGHT_BRACE;}
  "=" {yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;}
    
  {COMMENT} {return COMMENT;}
}

<WAITING_PROPERTY_VALUE>{
  {EOL} { yybegin(YYINITIAL); return WHITE_SPACE;}
  {SPACE} {return WHITE_SPACE;}
  
  "{" {yybegin(YYINITIAL); return LEFT_BRACE;}
  "}" {yybegin(YYINITIAL); return RIGHT_BRACE;}
  
  "###" { yybegin(WAITING_DOCUMENTATION); return DOCUMENTATION_START; }
  "##" {  yybegin(WAITING_OPTION); return OPTION_START; }
   
  {COMMENT} {return COMMENT; }
      
  {BOOLEAN_TOKEN} { yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN; }
  {INT_TOKEN} { yybegin(WAITING_PROPERTY_END); return INT_TOKEN; }
  {FLOAT_TOKEN} { yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN; }
  {STRING_TOKEN} {yybegin(WAITING_PROPERTY_END); return STRING_TOKEN;}
}

<WAITING_PROPERTY_VALUE_END>{
  {EOL} { yybegin(YYINITIAL); return WHITE_SPACE;}
  {SPACE} {yybegin(YYINITIAL); return WHITE_SPACE;}
  
  "{" {yybegin(YYINITIAL); return LEFT_BRACE;}
  "}" {yybegin(YYINITIAL); return RIGHT_BRACE;}
    
  {COMMENT} {return COMMENT;}
}

<WAITING_PROPERTY_END>{
  {EOL} { yybegin(YYINITIAL);  return WHITE_SPACE;}
  {SPACE} {yybegin(YYINITIAL);  return WHITE_SPACE;}
  
  "{" {yybegin(YYINITIAL); return LEFT_BRACE;}
  "}" {yybegin(YYINITIAL); return RIGHT_BRACE;}
  
  {COMMENT} {return COMMENT;}
}

<WAITING_OPTION>{
  {EOL} {yybegin(YYINITIAL); return WHITE_SPACE;}    
  {SPACE} {return WHITE_SPACE;}
      
  "{" {return LEFT_BRACE;}
  "}" {return RIGHT_BRACE;}

  {COMMENT} {return COMMENT; }
  
  {IS_OPTION_KEY} {yypushback(yylength()); yybegin(WAITING_OPTION_KEY);}
  
  {BOOLEAN_TOKEN} { yybegin(WAITING_OPTION_VALUE_END); return BOOLEAN_TOKEN; }
  {INT_TOKEN} { yybegin(WAITING_OPTION_VALUE_END); return INT_TOKEN; }
  {FLOAT_TOKEN} { yybegin(WAITING_OPTION_VALUE_END); return FLOAT_TOKEN; }
  {STRING_TOKEN} {yybegin(WAITING_OPTION_VALUE_END); return STRING_TOKEN;}
}

<WAITING_OPTION_KEY>{
  {EOL} { yybegin(YYINITIAL); return WHITE_SPACE;}
  {SPACE} {return WHITE_SPACE;}
  
  "{" {yybegin(WAITING_OPTION); return LEFT_BRACE;}
  "}" {yybegin(WAITING_OPTION); return RIGHT_BRACE;}
  
  {OPTION_KEY_TOKEN} {yybegin(WATIING_OPTION_SEPARATOR); return OPTION_KEY_TOKEN;}
}

<WATIING_OPTION_SEPARATOR>{
  {EOL} { yybegin(YYINITIAL); return WHITE_SPACE;}
  {SPACE} {return WHITE_SPACE;}
  
  "{" {yybegin(WAITING_OPTION); return LEFT_BRACE;}
  "}" {yybegin(WAITING_OPTION); return RIGHT_BRACE;}
  "=" {yybegin(WAITING_OPTION_VALUE); return EQUAL_SIGN;}
    
  {COMMENT} {return COMMENT;}
}

<WAITING_OPTION_VALUE>{
  {EOL} { yybegin(YYINITIAL); return WHITE_SPACE;}
  {SPACE} {return WHITE_SPACE;}
  
  "{" {yybegin(WAITING_OPTION); return LEFT_BRACE;}
  "}" {yybegin(WAITING_OPTION); return RIGHT_BRACE;}
  
  "###" { yybegin(WAITING_DOCUMENTATION); return DOCUMENTATION_START; }
  "##" {  yybegin(WAITING_OPTION); return OPTION_START; }
 
  {COMMENT} {return COMMENT; }
      
  {BOOLEAN_TOKEN} { yybegin(WAITING_OPTION_END); return BOOLEAN_TOKEN; }
  {INT_TOKEN} { yybegin(WAITING_OPTION_END); return INT_TOKEN; }
  {FLOAT_TOKEN} { yybegin(WAITING_OPTION_END); return FLOAT_TOKEN; }
  {STRING_TOKEN} {yybegin(WAITING_OPTION_END); return STRING_TOKEN;}
}

<WAITING_OPTION_END>{
  {EOL} { yybegin(YYINITIAL);  return WHITE_SPACE;}
  {SPACE} {yybegin(WAITING_OPTION);  return WHITE_SPACE;}
  
  "{" {yybegin(WAITING_OPTION); return LEFT_BRACE;}
  "}" {yybegin(WAITING_OPTION); return RIGHT_BRACE;}
  
  {COMMENT} {return COMMENT;}
}

<WAITING_OPTION_VALUE_END>{
  {EOL} { yybegin(YYINITIAL); return WHITE_SPACE;}
  {SPACE} {yybegin(WAITING_OPTION); return WHITE_SPACE;}
  
  "{" {yybegin(WAITING_OPTION); return LEFT_BRACE;}
  "}" {yybegin(WAITING_OPTION); return RIGHT_BRACE;}
    
  {COMMENT} {return COMMENT;}
}

<WAITING_DOCUMENTATION>{
  {EOL} {yybegin(YYINITIAL); return WHITE_SPACE;}
  {SPACE} { return WHITE_SPACE;}
    
  {DOCUMENTATION_TOKEN} { yybegin(YYINITIAL); return DOCUMENTATION_TOKEN;}
}

[^] { return BAD_CHARACTER; }
