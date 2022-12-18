package icu.windea.pls.cwt.editor

import com.intellij.lang.cacheBuilder.*
import icu.windea.pls.cwt.psi.*

class CwtWordScanner: DefaultWordsScanner(
	CwtLexerAdapter(),
	CwtTokenSets.IDENTIFIER_TOKENS,
	CwtTokenSets.COMMENT_TOKENS,
	CwtTokenSets.LITERAL_TOKENS
)