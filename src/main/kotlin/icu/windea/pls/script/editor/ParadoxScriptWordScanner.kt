package icu.windea.pls.script.editor

import com.intellij.lang.cacheBuilder.*
import com.intellij.psi.tree.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptWordScanner : DefaultWordsScanner(
	ParadoxScriptLexerAdapter(),
	TokenSet.create(VARIABLE_NAME_ID, VARIABLE_REFERENCE_ID, INLINE_MATH_VARIABLE_REFERENCE_ID, INPUT_PARAMETER_ID, PARAMETER_ID),
	TokenSet.create(COMMENT),
	TokenSet.create(PROPERTY_KEY_TOKEN, QUOTED_PROPERTY_KEY_TOKEN, QUOTED_STRING_TOKEN, STRING_TOKEN, KEY_STRING_SNIPPET, VALUE_STRING_SNIPPET, ARG_STRING_TOKEN)
) {
	init {
		setMayHaveFileRefsInLiterals(false)
	}
}
