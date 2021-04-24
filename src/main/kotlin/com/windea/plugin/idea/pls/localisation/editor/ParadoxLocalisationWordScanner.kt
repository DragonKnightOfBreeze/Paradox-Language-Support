package com.windea.plugin.idea.pls.localisation.editor

import com.intellij.lang.cacheBuilder.*
import com.intellij.psi.tree.*
import com.windea.plugin.idea.pls.localisation.psi.*
import com.windea.plugin.idea.pls.localisation.psi.ParadoxLocalisationTypes.*

class ParadoxLocalisationWordScanner: DefaultWordsScanner(
	ParadoxLocalisationLexerAdapter(),
	TokenSet.create(PROPERTY_KEY_ID, PROPERTY_REFERENCE_ID, ICON_ID,COMMAND_SCOPE,COMMAND_FIELD),
	TokenSet.create(COMMENT, ROOT_COMMENT, END_OF_LINE_COMMENT),
	TokenSet.create(STRING_TOKEN)
){
	init {
		setMayHaveFileRefsInLiterals(false)
	}
}
