package icu.windea.pls.script.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import icu.windea.pls.localisation.highlighter.*

object ParadoxScriptAttributesKeys {
	@JvmField val BRACES_KEY = createTextAttributesKey("PARADOX_SCRIPT.BRACES", BRACES)
	@JvmField val OPERATOR_KEY = createTextAttributesKey("PARADOX_SCRIPT.OPERATOR", OPERATION_SIGN)
	@JvmField val MARKER_KEY = createTextAttributesKey("PARADOX_SCRIPT.MARKER", KEYWORD)
	@JvmField val PARAMETER_CONDITION_BRACKETS_KEYS = createTextAttributesKey("PARADOX_SCRIPT.PARAMETER_CONDITION_BRACKETS", BRACES)
	@JvmField val PARAMETER_CONDITION_EXPRESSION_BRACKETS_KEYS = createTextAttributesKey("PARADOX_SCRIPT.PARAMETER_CONDITION_EXPRESSION_BRACKETS", BRACES)
	@JvmField val INLINE_MATH_BRACES_KEY = createTextAttributesKey("PARADOX_SCRIPT.INLINE_MATH_BRACKETS", BRACES)
	@JvmField val INLINE_MATH_OPERATOR_KEY = createTextAttributesKey("PARADOX_SCRIPT.INLINE_MATH_OPERATOR", OPERATION_SIGN)
	@JvmField val KEYWORD_KEY = createTextAttributesKey("PARADOX_SCRIPT.KEYWORD", KEYWORD)
	@JvmField val COMMENT_KEY = createTextAttributesKey("PARADOX_SCRIPT.COMMENT", LINE_COMMENT)
	@JvmField val SCRIPTED_VARIABLE_KEY = createTextAttributesKey("PARADOX_SCRIPT.SCRIPTED_VARIABLE", STATIC_FIELD)
	@JvmField val ARGUMENT_KEY = createTextAttributesKey("PARADOX_SCRIPT.ARGUMENT", KEYWORD) //Kotlin > Named argument
	@JvmField val PARAMETER_KEY = createTextAttributesKey("PARADOX_SCRIPT.PARAMETER", KEYWORD)
	@JvmField val PROPERTY_KEY_KEY = createTextAttributesKey("PARADOX_SCRIPT.PROPERTY_KEY", INSTANCE_FIELD)
	@JvmField val COLOR_KEY = createTextAttributesKey("PARADOX_SCRIPT.COLOR", KEYWORD) //HTML > Tag name
	@JvmField val NUMBER_KEY = createTextAttributesKey("PARADOX_SCRIPT.NUMBER", NUMBER)
	@JvmField val STRING_KEY = createTextAttributesKey("PARADOX_SCRIPT.STRING", STRING)
	@JvmField val VALID_ESCAPE_KEY = createTextAttributesKey("Paradox_Script.VALID_ESCAPE", VALID_STRING_ESCAPE)
	@JvmField val INVALID_ESCAPE_KEY = createTextAttributesKey("PARADOX_SCRIPT.INVALID_ESCAPE", INVALID_STRING_ESCAPE)
	@JvmField val BAD_CHARACTER_KEY = createTextAttributesKey("PARADOX_SCRIPT.BAD_CHARACTER", BAD_CHARACTER)
	
	@JvmField val DEFINITION_KEY = createTextAttributesKey("PARADOX_SCRIPT.DEFINITION", PROPERTY_KEY_KEY) //underscored
	@JvmField val DEFINITION_NAME_KEY = createTextAttributesKey("PARADOX_SCRIPT.DEFINITION_NAME") //background #223C23
	@JvmField val COMPLEX_ENUM_VALUE_NAME_KEY = createTextAttributesKey("PARADOX_SCRIPT.COMPLEX_ENUM_VALUE_NAME", INSTANCE_FIELD) //background #223C23
	@JvmField val DEFINITION_REFERENCE_KEY = createTextAttributesKey("PARADOX_SCRIPT.DEFINITION_REFERENCE", DEFINITION_KEY)  //dotted line  #707D95
	@JvmField val LOCALISATION_REFERENCE_KEY = createTextAttributesKey("PARADOX_SCRIPT.LOCALISATION_REFERENCE", ParadoxLocalisationAttributesKeys.LOCALISATION_KEY)  //dotted line #707D95
	@JvmField val SYNCED_LOCALISATION_REFERENCE_KEY = createTextAttributesKey("PARADOX_SCRIPT.SYNCED_LOCALISATION_REFERENCE", ParadoxLocalisationAttributesKeys.SYNCED_LOCALISATION_KEY)  //dotted line #707D95
	@JvmField val PATH_REFERENCE_KEY = createTextAttributesKey("PARADOX_SCRIPT.PATH_REFERENCE", CodeInsightColors.INACTIVE_HYPERLINK_ATTRIBUTES)
	@JvmField val ENUM_VALUE_KEY = createTextAttributesKey("PARADOX_SCRIPT.ENUM_VALUE", STATIC_FIELD)
	@JvmField val COMPLEX_ENUM_VALUE_KEY = createTextAttributesKey("PARADOX_SCRIPT.COMPLEX_ENUM_VALUE", INSTANCE_FIELD) //dotted line #707D95
	@JvmField val VARIABLE_KEY = createTextAttributesKey("PARADOX_SCRIPT.VARIABLE_VALUE", INSTANCE_FIELD) //Less > Variable
	@JvmField val VALUE_SET_VALUE_KEY = createTextAttributesKey("PARADOX_SCRIPT.VALUE_SET_VALUE", LOCAL_VARIABLE)
	@JvmField val SYSTEM_SCOPE_KEY = createTextAttributesKey("PARADOX_SCRIPT.SYSTEM_SCOPE", STATIC_METHOD)
	@JvmField val SCOPE_KEY = createTextAttributesKey("PARADOX_SCRIPT.SCOPE", INSTANCE_METHOD)
	@JvmField val SCOPE_LINK_PREFIX_KEY = createTextAttributesKey("PARADOX_SCRIPT.SCOPE_LINK_PREFIX", KEYWORD)
	@JvmField val SCOPE_LINK_DATA_SOURCE_KEY = createTextAttributesKey("PARADOX_SCRIPT.SCOPE_LINK_DATA_SOURCE")
	@JvmField val VALUE_LINK_VALUE_KEY = createTextAttributesKey("PARADOX_SCRIPT.VALUE_LINK_VALUE", INSTANCE_FIELD)
	@JvmField val VALUE_LINK_PREFIX_KEY = createTextAttributesKey("PARADOX_SCRIPT.VALUE_LINK_PREFIX", KEYWORD)
	@JvmField val VALUE_LINK_DATA_SOURCE_KEY = createTextAttributesKey("PARADOX_SCRIPT.VALUE_LINK_DATA_SOURCE")
	@JvmField val MODIFIER_KEY = createTextAttributesKey("PARADOX_SCRIPT.MODIFIER", PREDEFINED_SYMBOL) //Python > Special names//Definition
	@JvmField val TAG_KEY = createTextAttributesKey("PARADOX_SCRIPT.TAG", METADATA)
	@JvmField val SCOPE_FILED_EXPRESSION_KEY = createTextAttributesKey("PARADOX_SCRIPT.SCOPE_FILED_EXPRESSION", IDENTIFIER)
	@JvmField val VALUE_FILED_EXPRESSION_KEY = createTextAttributesKey("PARADOX_SCRIPT.VALUE_FILED_EXPRESSION", IDENTIFIER)
	@JvmField val VALUE_SET_VALUE_EXPRESSION_KEY = createTextAttributesKey("PARADOX_SCRIPT.VALUE_SET_VALUE_EXPRESSION", IDENTIFIER)
}
