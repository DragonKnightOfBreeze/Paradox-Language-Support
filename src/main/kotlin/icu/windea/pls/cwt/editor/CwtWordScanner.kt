package icu.windea.pls.cwt.editor

import com.intellij.lang.cacheBuilder.*
import com.intellij.psi.tree.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

class CwtWordScanner: DefaultWordsScanner(
	CwtLexerAdapter(),
	TokenSet.create(PROPERTY_KEY_TOKEN,OPTION_KEY_TOKEN),
	TokenSet.create(COMMENT, DOCUMENTATION_TOKEN),
	TokenSet.create(STRING_TOKEN)
){
	init {
		setMayHaveFileRefsInLiterals(false)
	}
}