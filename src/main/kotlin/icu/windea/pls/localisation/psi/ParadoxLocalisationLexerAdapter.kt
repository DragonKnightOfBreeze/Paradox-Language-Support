package icu.windea.pls.localisation.psi

import com.intellij.lexer.*

class ParadoxLocalisationLexerAdapter(
	context: ParadoxLocalisationParsingContext? = null
): FlexAdapter(ParadoxLocalisationLexer(context))

