package icu.windea.pls.script.editor

import com.intellij.lang.cacheBuilder.*
import icu.windea.pls.script.psi.*

class ParadoxScriptWordScanner : DefaultWordsScanner(
	ParadoxScriptLexerAdapter(),
	ParadoxScriptTokenSets.IDENTIFIER_TOKENS,
	ParadoxScriptTokenSets.COMMENT_TOKENS,
	ParadoxScriptTokenSets.LITERAL_TOKENS
)
