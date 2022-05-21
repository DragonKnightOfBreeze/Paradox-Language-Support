package icu.windea.pls.script.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import icu.windea.pls.*
import icu.windea.pls.localisation.highlighter.*

object ParadoxScriptAttributesKeys {
	@JvmField val BRACES_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.braces"), BRACES)
	@JvmField val OPERATOR_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.operator"), OPERATION_SIGN)
	@JvmField val MARKER_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.marker"), KEYWORD)
	@JvmField val INLINE_MATH_BRACES_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.inlineMathBraces"), BRACES)
	@JvmField val INLINE_MATH_OPERATOR_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.inlineMathOperator"), OPERATION_SIGN)
	@JvmField val COMMENT_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.comment"), LINE_COMMENT)
	@JvmField val VARIABLE_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.variable"), STATIC_FIELD)
	@JvmField val PARAMETER_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.parameter"), KEYWORD)
	@JvmField val PROPERTY_KEY_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.propertyKey"), INSTANCE_FIELD)
	@JvmField val KEYWORD_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.keyword"), KEYWORD)
	@JvmField val COLOR_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.color"), FUNCTION_DECLARATION)
	@JvmField val NUMBER_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.number"), NUMBER)
	@JvmField val STRING_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.string"), STRING)
	@JvmField val VALID_ESCAPE_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.validEscape"), VALID_STRING_ESCAPE)
	@JvmField val INVALID_ESCAPE_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.invalidEscape"), INVALID_STRING_ESCAPE)
	@JvmField val BAD_CHARACTER_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.badCharacter"), BAD_CHARACTER)
	
	@JvmField val DEFINITION_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.definition"), PROPERTY_KEY_KEY)
	@JvmField val DEFINITION_REFERENCE_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.definitionReference"), DEFINITION_KEY)
	@JvmField val LOCALISATION_REFERENCE_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.localisationReference"), ParadoxLocalisationAttributesKeys.LOCALISATION_KEY)
	@JvmField val SYNCED_LOCALISATION_REFERENCE_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.syncedLocalisationReference"), ParadoxLocalisationAttributesKeys.SYNCED_LOCALISATION_KEY)
	@JvmField val ENUM_VALUE_REFERENCE_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.enumValueReference"), STATIC_FIELD)
	@JvmField val PATH_REFERENCE_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.pathReference"), STRING_KEY)
	@JvmField val TAG_KEY = createTextAttributesKey(PlsBundle.message("script.externalName.tag"), METADATA)
}
