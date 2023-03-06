package icu.windea.pls.script.psi

import com.intellij.lexer.*

class ParadoxScriptLexerAdapter: FlexAdapter(ParadoxScriptLexer())

//class ParadoxScriptLexerAdapter(
//	context: ParadoxScriptParsingContext? = null
//): FlexAdapter(ParadoxScriptLexer(context))