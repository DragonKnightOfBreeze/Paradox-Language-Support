package com.windea.plugin.idea.paradox.localisation.editor

import com.intellij.lang.cacheBuilder.*
import com.intellij.psi.tree.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*

class ParadoxLocalisationWordScanner: DefaultWordsScanner(
	ParadoxLocalisationLexerAdapter(),
	TokenSet.create(PROPERTY_KEY_ID, PROPERTY_REFERENCE_ID, ICON_ID,COMMAND_KEY),
	TokenSet.create(COMMENT, ROOT_COMMENT, END_OF_LINE_COMMENT),
	TokenSet.create(STRING_TOKEN)
){
	init {
		setMayHaveFileRefsInLiterals(false)
	}
}
