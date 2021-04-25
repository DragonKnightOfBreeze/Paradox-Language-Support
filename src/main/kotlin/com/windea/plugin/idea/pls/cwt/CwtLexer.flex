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
%state WAITING_PROPERTY_END
%state WAITING_VALUE_END

EOL=\R
BLANK=[ \t\n\x0B\f\r]+
SPACE=[ \t\x08\f]+

COMMENT=#[^\r\n]*
OPTION_COMMENT=##[^\r\n]*
DOCUMENTATION_COMMENT=###[^\r\n]*

KEY_TOKEN=([^#={}\s][^={}\s]*)|(\"([^\"\\\r\n]|\\.)*\")
BOOLEAN_TOKEN=(yes)|(no)
INT_TOKEN=[+-]?(0|[1-9][0-9]*)
FLOAT_TOKEN=[+-]?(0|[1-9][0-9]*)(\.[0-9]+)
STRING_TOKEN=([^#={}\s\"][^={}\s\"]*)|(\"([^\"\\\r\n]|\\.)*\")

IS_PROPERTY=({KEY_TOKEN})?({SPACE})?=

%%
<YYINITIAL> {
  "{" {return LEFT_BRACE;}
  "}" {return RIGHT_BRACE;}
  {BLANK} { return WHITE_SPACE; }
      
  {COMMENT} {return COMMENT; }
  {OPTION_COMMENT} {return OPTION_COMMENT; }
  {DOCUMENTATION_COMMENT} {return DOCUMENTATION_COMMENT; }
  
  {IS_PROPERTY} {yypushback(yylength()); yybegin(WAITING_PROPERTY_KEY);}
  
  {BOOLEAN_TOKEN} { yybegin(WAITING_VALUE_END); return BOOLEAN_TOKEN; }
  {INT_TOKEN} { yybegin(WAITING_VALUE_END); return INT_TOKEN; }
  {FLOAT_TOKEN} { yybegin(WAITING_VALUE_END); return FLOAT_TOKEN; }
  {STRING_TOKEN} {yybegin(WAITING_VALUE_END); return STRING_TOKEN;}
  
  <WAITING_PROPERTY_KEY>{
    "{" {yybegin(YYINITIAL); return LEFT_BRACE;}
    "}" {yybegin(YYINITIAL); return RIGHT_BRACE;}
    {KEY_TOKEN} {yybegin(WATIING_PROPERTY_SEPARATOR); return KEY_TOKEN;}
  }
  
  <WATIING_PROPERTY_SEPARATOR>{
    "{" {yybegin(YYINITIAL); return LEFT_BRACE;}
    "}" {yybegin(YYINITIAL); return RIGHT_BRACE;}
    "=" {yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;}
    {EOL} {yybegin(YYINITIAL); return WHITE_SPACE;}
    {SPACE} {return WHITE_SPACE;}
      
    {COMMENT} {return COMMENT;}
  }
  
  <WAITING_PROPERTY_VALUE>{
    "{" {yybegin(YYINITIAL); return LEFT_BRACE;}
    "}" {yybegin(YYINITIAL); return RIGHT_BRACE;}
    {EOL} {yybegin(YYINITIAL); return WHITE_SPACE;}
    {SPACE} {return WHITE_SPACE;}
      
    {COMMENT} {return COMMENT; }
    {OPTION_COMMENT} {return OPTION_COMMENT; }
    {DOCUMENTATION_COMMENT} {return DOCUMENTATION_COMMENT; }
    
    {BOOLEAN_TOKEN} { yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN; }
    {INT_TOKEN} { yybegin(WAITING_PROPERTY_END); return INT_TOKEN; }
    {FLOAT_TOKEN} { yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN; }
    {STRING_TOKEN} {yybegin(WAITING_PROPERTY_END); return STRING_TOKEN;}
  }
  
  <WAITING_PROPERTY_END>{
    "{" {yybegin(YYINITIAL); return LEFT_BRACE;}
    "}" {yybegin(YYINITIAL); return RIGHT_BRACE;}
    {EOL} {yybegin(YYINITIAL); return WHITE_SPACE;}
    {SPACE} {yybegin(YYINITIAL); return WHITE_SPACE;}
      
    {COMMENT} {return COMMENT;}
  }
  
  <WAITING_VALUE_END>{
    "{" {yybegin(YYINITIAL); return LEFT_BRACE;}
    "}" {yybegin(YYINITIAL); return RIGHT_BRACE;}
    {EOL} {yybegin(YYINITIAL); return WHITE_SPACE;}
    {SPACE} {yybegin(YYINITIAL); return WHITE_SPACE;}
      
    {COMMENT} {return COMMENT;}
  }
}

[^] { return BAD_CHARACTER; }
