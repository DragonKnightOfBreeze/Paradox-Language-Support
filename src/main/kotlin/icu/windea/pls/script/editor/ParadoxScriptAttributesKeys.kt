package icu.windea.pls.script.editor

import com.intellij.ide.highlighter.custom.CustomHighlighterColors
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys

object ParadoxScriptAttributesKeys {
    @JvmField val BRACES = create("PARADOX_SCRIPT.BRACES", DefaultLanguageHighlighterColors.BRACES)
    @JvmField val OPERATOR = create("PARADOX_SCRIPT.OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    @JvmField val MARKER = create("PARADOX_SCRIPT.MARKER", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val PARAMETER_CONDITION_BRACKETS = create("PARADOX_SCRIPT.PARAMETER_CONDITION_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    @JvmField val PARAMETER_CONDITION_EXPRESSION_BRACKETS = create("PARADOX_SCRIPT.PARAMETER_CONDITION_EXPRESSION_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    @JvmField val INLINE_MATH_BRACKETS = create("PARADOX_SCRIPT.INLINE_MATH_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    @JvmField val INLINE_MATH_OPERATOR = create("PARADOX_SCRIPT.INLINE_MATH_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    @JvmField val COMMENT = create("PARADOX_SCRIPT.COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    @JvmField val KEYWORD = create("PARADOX_SCRIPT.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val AT_SIGN = create("PARADOX_SCRIPT.AT_SIGN", DefaultLanguageHighlighterColors.STATIC_FIELD)
    @JvmField val SCRIPTED_VARIABLE_NAME = create("PARADOX_SCRIPT.SCRIPTED_VARIABLE_NAME", DefaultLanguageHighlighterColors.STATIC_FIELD)
    @JvmField val SCRIPTED_VARIABLE_REFERENCE = create("PARADOX_SCRIPT.SCRIPTED_VARIABLE_REFERENCE", DefaultLanguageHighlighterColors.STATIC_FIELD)
    @JvmField val PARAMETER = create("PARADOX_SCRIPT.PARAMETER", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val CONDITION_PARAMETER = create("PARADOX_SCRIPT.CONDITION_PARAMETER", DefaultLanguageHighlighterColors.KEYWORD)  // KOTLIN_NAMED_ARGUMENT
    @JvmField val ARGUMENT = create("PARADOX_SCRIPT.ARGUMENT", DefaultLanguageHighlighterColors.KEYWORD) // KOTLIN_NAMED_ARGUMENT
    @JvmField val PROPERTY_KEY = create("PARADOX_SCRIPT.PROPERTY_KEY", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    @JvmField val NUMBER = create("PARADOX_SCRIPT.NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    @JvmField val STRING = create("PARADOX_SCRIPT.STRING", DefaultLanguageHighlighterColors.STRING)
    @JvmField val COLOR = create("PARADOX_SCRIPT.COLOR", DefaultLanguageHighlighterColors.KEYWORD) // HTML_TAG_NAME
    @JvmField val VALID_ESCAPE = create("Paradox_Script.VALID_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    @JvmField val INVALID_ESCAPE = create("PARADOX_SCRIPT.INVALID_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
    @JvmField val BAD_CHARACTER = create("PARADOX_SCRIPT.BAD_CHARACTER", com.intellij.openapi.editor.HighlighterColors.BAD_CHARACTER)

    @JvmField val DEFINITION = create("PARADOX_SCRIPT.DEFINITION", PROPERTY_KEY) // underscored
    @JvmField val DEFINITION_NAME = create("PARADOX_SCRIPT.DEFINITION_NAME") // background #223C23
    @JvmField val DEFINITION_REFERENCE = create("PARADOX_SCRIPT.DEFINITION_REFERENCE", DEFINITION)  // dotted line #707D95
    @JvmField val LOCALISATION_REFERENCE = create("PARADOX_SCRIPT.LOCALISATION_REFERENCE", ParadoxLocalisationAttributesKeys.PROPERTY_KEY)
    @JvmField val SYNCED_LOCALISATION_REFERENCE = create("PARADOX_SCRIPT.SYNCED_LOCALISATION_REFERENCE", LOCALISATION_REFERENCE)
    @JvmField val DEFINE_NAMESPACE = create("PARADOX_SCRIPT.DEFINE_NAMESPACE", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
    @JvmField val DEFINE_VARIABLE = create("PARADOX_SCRIPT.DEFINE_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE)
    @JvmField val ENUM_VALUE = create("PARADOX_SCRIPT.ENUM_VALUE", DefaultLanguageHighlighterColors.STATIC_FIELD)
    @JvmField val COMPLEX_ENUM_VALUE = create("PARADOX_SCRIPT.COMPLEX_ENUM_VALUE", DefaultLanguageHighlighterColors.INSTANCE_FIELD) // dotted line #707D95, italic
    @JvmField val DYNAMIC_VALUE = create("PARADOX_SCRIPT.DYNAMIC_VALUE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE)
    @JvmField val VARIABLE = create("PARADOX_SCRIPT.VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE) // italic
    @JvmField val MODIFIER = create("PARADOX_SCRIPT.MODIFIER", CustomHighlighterColors.CUSTOM_KEYWORD2_ATTRIBUTES)
    @JvmField val TRIGGER = create("PARADOX_SCRIPT.TRIGGER", CustomHighlighterColors.CUSTOM_KEYWORD3_ATTRIBUTES)
    @JvmField val EFFECT = create("PARADOX_SCRIPT.EFFECT", CustomHighlighterColors.CUSTOM_KEYWORD4_ATTRIBUTES)
    @JvmField val SYSTEM_SCOPE = create("PARADOX_SCRIPT.SYSTEM_SCOPE", DefaultLanguageHighlighterColors.STATIC_METHOD)
    @JvmField val SCOPE = create("PARADOX_SCRIPT.SCOPE", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
    @JvmField val VALUE_FIELD = create("PARADOX_SCRIPT.VALUE_FIELD", DefaultLanguageHighlighterColors.IDENTIFIER) // HTML_ENTITY_REFERENCE
    @JvmField val DATABASE_OBJECT_TYPE = create("PARADOX_SCRIPT.DATABASE_OBJECT_TYPE", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val DATABASE_OBJECT = create("PARADOX_SCRIPT.DATABASE_OBJECT")

    @JvmField val SCOPE_LINK_PREFIX = create("PARADOX_SCRIPT.SCOPE_LINK_PREFIX", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val VALUE_FIELD_PREFIX = create("PARADOX_SCRIPT.VALUE_FIELD_PREFIX", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val DEFINE_PREFIX = create("PARADOX_SCRIPT.DEFINE_PREFIX", DefaultLanguageHighlighterColors.KEYWORD)

    @JvmField val TAG = create("PARADOX_SCRIPT.TAG", DefaultLanguageHighlighterColors.METADATA)
    @JvmField val DIRECTIVE = create("PARADOX_SCRIPT.DIRECTIVE", DefaultLanguageHighlighterColors.KEYWORD) // JSP directive name
    @JvmField val PATH_REFERENCE = create("PARADOX_SCRIPT.PATH_REFERENCE", CodeInsightColors.INACTIVE_HYPERLINK_ATTRIBUTES)

    private fun create(name: String, fallback: TextAttributesKey? = null): TextAttributesKey {
        if (fallback == null) return TextAttributesKey.createTextAttributesKey(name)
        return TextAttributesKey.createTextAttributesKey(name, fallback)
    }
}
