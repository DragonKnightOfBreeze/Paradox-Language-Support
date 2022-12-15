package icu.windea.pls.localisation.editor

import com.intellij.lang.cacheBuilder.*
import com.intellij.psi.tree.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationWordScanner : DefaultWordsScanner(
	ParadoxLocalisationLexerAdapter(),
	TokenSet.create(PROPERTY_KEY_TOKEN, PROPERTY_REFERENCE_ID, SCRIPTED_VARIABLE_REFERENCE_ID, ICON_ID, COMMAND_SCOPE_ID, COMMAND_FIELD_ID, STELLARIS_NAME_FORMAT_ID),
	TokenSet.create(COMMENT),
	TokenSet.create(STRING_TOKEN)
)
