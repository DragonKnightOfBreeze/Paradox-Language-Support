package icu.windea.pls.cwt.editor

import com.intellij.lang.cacheBuilder.*
import com.intellij.psi.tree.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

class CwtWordScanner: DefaultWordsScanner(
	CwtLexerAdapter(),
	CwtTokenSets.IDENTIFIERS,
	CwtTokenSets.COMMENTS,
	CwtTokenSets.LITERALS
)