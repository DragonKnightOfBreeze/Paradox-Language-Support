package com.windea.plugin.idea.paradox.script.editor

import com.intellij.lang.cacheBuilder.*
import com.intellij.psi.tree.*
import com.windea.plugin.idea.paradox.script.psi.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*

class ParadoxScriptWordScanner: DefaultWordsScanner(
	ParadoxScriptLexerAdapter(),
	TokenSet.create(VARIABLE_NAME_ID, VARIABLE_REFERENCE_ID, PROPERTY_KEY_ID, QUOTED_PROPERTY_KEY_ID),
	TokenSet.create(COMMENT, END_OF_LINE_COMMENT),
	TokenSet.create(QUOTED_STRING_TOKEN, STRING_TOKEN)
){
	init {
		setMayHaveFileRefsInLiterals(true)
	}
}
