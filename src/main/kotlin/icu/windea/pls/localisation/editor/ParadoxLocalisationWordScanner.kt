package icu.windea.pls.localisation.editor

import com.intellij.lang.cacheBuilder.*
import com.intellij.psi.tree.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationWordScanner : DefaultWordsScanner(
	ParadoxLocalisationLexerAdapter(),
	ParadoxLocalisationTokenSets.IDENTIFIERS,
	ParadoxLocalisationTokenSets.COMMENTS,
	ParadoxLocalisationTokenSets.LITERALS
)
