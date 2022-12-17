package icu.windea.pls.script.editor

import com.intellij.lang.cacheBuilder.*
import com.intellij.psi.tree.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptWordScanner : DefaultWordsScanner(
	ParadoxScriptLexerAdapter(),
	ParadoxScriptTokenSets.IDENTIFIERS,
	ParadoxScriptTokenSets.COMMENTS,
	ParadoxScriptTokenSets.LITERALS
)
