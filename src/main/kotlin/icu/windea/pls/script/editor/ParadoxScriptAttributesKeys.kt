package icu.windea.pls.script.editor

import com.intellij.ide.highlighter.custom.CustomHighlighterColors.CUSTOM_KEYWORD2_ATTRIBUTES
import com.intellij.ide.highlighter.custom.CustomHighlighterColors.CUSTOM_KEYWORD3_ATTRIBUTES
import com.intellij.ide.highlighter.custom.CustomHighlighterColors.CUSTOM_KEYWORD4_ATTRIBUTES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.GLOBAL_VARIABLE
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.IDENTIFIER
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_METHOD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.LINE_COMMENT
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.METADATA
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.OPERATION_SIGN
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STATIC_FIELD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STATIC_METHOD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STRING
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
import com.intellij.openapi.editor.HighlighterColors.BAD_CHARACTER
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys as LKeys

object ParadoxScriptAttributesKeys {
    @JvmField
    val BRACES_KEY = createTextAttributesKey("PARADOX_SCRIPT.BRACES", BRACES)
    @JvmField
    val OPERATOR_KEY = createTextAttributesKey("PARADOX_SCRIPT.OPERATOR", OPERATION_SIGN)
    @JvmField
    val MARKER_KEY = createTextAttributesKey("PARADOX_SCRIPT.MARKER", KEYWORD)
    @JvmField
    val PARAMETER_CONDITION_BRACKETS_KEYS = createTextAttributesKey("PARADOX_SCRIPT.PARAMETER_CONDITION_BRACKETS", BRACES)
    @JvmField
    val PARAMETER_CONDITION_EXPRESSION_BRACKETS_KEYS = createTextAttributesKey("PARADOX_SCRIPT.PARAMETER_CONDITION_EXPRESSION_BRACKETS", BRACES)
    @JvmField
    val INLINE_MATH_BRACES_KEY = createTextAttributesKey("PARADOX_SCRIPT.INLINE_MATH_BRACKETS", BRACES)
    @JvmField
    val INLINE_MATH_OPERATOR_KEY = createTextAttributesKey("PARADOX_SCRIPT.INLINE_MATH_OPERATOR", OPERATION_SIGN)
    @JvmField
    val KEYWORD_KEY = createTextAttributesKey("PARADOX_SCRIPT.KEYWORD", KEYWORD)
    @JvmField
    val COMMENT_KEY = createTextAttributesKey("PARADOX_SCRIPT.COMMENT", LINE_COMMENT)
    @JvmField
    val SCRIPTED_VARIABLE_KEY = createTextAttributesKey("PARADOX_SCRIPT.SCRIPTED_VARIABLE", STATIC_FIELD)
    @JvmField
    val PARAMETER_KEY = createTextAttributesKey("PARADOX_SCRIPT.PARAMETER", KEYWORD)
    @JvmField
    val CONDITION_PARAMETER_KEY = createTextAttributesKey("PARADOX_SCRIPT.CONDITION_PARAMETER", KEYWORD)  //KOTLIN_NAMED_ARGUMENT
    @JvmField
    val ARGUMENT_KEY = createTextAttributesKey("PARADOX_SCRIPT.ARGUMENT", KEYWORD) //KOTLIN_NAMED_ARGUMENT
    @JvmField
    val PROPERTY_KEY_KEY = createTextAttributesKey("PARADOX_SCRIPT.PROPERTY_KEY", INSTANCE_FIELD)
    @JvmField
    val NUMBER_KEY = createTextAttributesKey("PARADOX_SCRIPT.NUMBER", NUMBER)
    @JvmField
    val STRING_KEY = createTextAttributesKey("PARADOX_SCRIPT.STRING", STRING)
    @JvmField
    val COLOR_KEY = createTextAttributesKey("PARADOX_SCRIPT.COLOR", KEYWORD) //HTML_TAG_NAME
    @JvmField
    val VALID_ESCAPE_KEY = createTextAttributesKey("Paradox_Script.VALID_ESCAPE", VALID_STRING_ESCAPE)
    @JvmField
    val INVALID_ESCAPE_KEY = createTextAttributesKey("PARADOX_SCRIPT.INVALID_ESCAPE", INVALID_STRING_ESCAPE)
    @JvmField
    val BAD_CHARACTER_KEY = createTextAttributesKey("PARADOX_SCRIPT.BAD_CHARACTER", BAD_CHARACTER)

    @JvmField
    val DEFINITION_KEY = createTextAttributesKey("PARADOX_SCRIPT.DEFINITION", PROPERTY_KEY_KEY) //underscored
    @JvmField
    val DEFINITION_NAME_KEY = createTextAttributesKey("PARADOX_SCRIPT.DEFINITION_NAME") //background #223C23
    @JvmField
    val DEFINITION_REFERENCE_KEY = createTextAttributesKey("PARADOX_SCRIPT.DEFINITION_REFERENCE", DEFINITION_KEY)  //dotted line #707D95
    @JvmField
    val LOCALISATION_REFERENCE_KEY = createTextAttributesKey("PARADOX_SCRIPT.LOCALISATION_REFERENCE", LKeys.PROPERTY_KEY_KEY)
    @JvmField
    val SYNCED_LOCALISATION_REFERENCE_KEY = createTextAttributesKey("PARADOX_SCRIPT.SYNCED_LOCALISATION_REFERENCE", LOCALISATION_REFERENCE_KEY)
    @JvmField
    val PATH_REFERENCE_KEY = createTextAttributesKey("PARADOX_SCRIPT.PATH_REFERENCE", CodeInsightColors.INACTIVE_HYPERLINK_ATTRIBUTES)
    @JvmField
    val ENUM_VALUE_KEY = createTextAttributesKey("PARADOX_SCRIPT.ENUM_VALUE", STATIC_FIELD)
    @JvmField
    val COMPLEX_ENUM_VALUE_KEY = createTextAttributesKey("PARADOX_SCRIPT.COMPLEX_ENUM_VALUE", INSTANCE_FIELD) //dotted line #707D95, italic

    @JvmField
    val MODIFIER_KEY = createTextAttributesKey("PARADOX_SCRIPT.MODIFIER", CUSTOM_KEYWORD2_ATTRIBUTES)
    @JvmField
    val TRIGGER_KEY = createTextAttributesKey("PARADOX_SCRIPT.TRIGGER", CUSTOM_KEYWORD3_ATTRIBUTES)
    @JvmField
    val EFFECT_KEY = createTextAttributesKey("PARADOX_SCRIPT.EFFECT", CUSTOM_KEYWORD4_ATTRIBUTES)
    @JvmField
    val TAG_KEY = createTextAttributesKey("PARADOX_SCRIPT.TAG", METADATA)

    @JvmField
    val SYSTEM_SCOPE_KEY = createTextAttributesKey("PARADOX_SCRIPT.SYSTEM_SCOPE", STATIC_METHOD)
    @JvmField
    val SCOPE_KEY = createTextAttributesKey("PARADOX_SCRIPT.SCOPE", INSTANCE_METHOD)
    @JvmField
    val SCOPE_LINK_PREFIX_KEY = createTextAttributesKey("PARADOX_SCRIPT.SCOPE_LINK_PREFIX", KEYWORD)
    @JvmField
    val SCOPE_LINK_VALUE_KEY = createTextAttributesKey("PARADOX_SCRIPT.SCOPE_LINK_VALUE")
    @JvmField
    val VALUE_FIELD_KEY = createTextAttributesKey("PARADOX_SCRIPT.VALUE_FIELD", IDENTIFIER) //HTML_ENTITY_REFERENCE
    @JvmField
    val VALUE_FIELD_PREFIX_KEY = createTextAttributesKey("PARADOX_SCRIPT.VALUE_FIELD_PREFIX", KEYWORD)
    @JvmField
    val VALUE_FIELD_VALUE_KEY = createTextAttributesKey("PARADOX_SCRIPT.VALUE_FIELD_VALUE")
    @JvmField
    val DYNAMIC_VALUE_KEY = createTextAttributesKey("PARADOX_SCRIPT.DYNAMIC_VALUE", GLOBAL_VARIABLE)
    @JvmField
    val VARIABLE_KEY = createTextAttributesKey("PARADOX_SCRIPT.VARIABLE", GLOBAL_VARIABLE) //italic
    @JvmField
    val DATABASE_OBJECT_TYPE_KEY = createTextAttributesKey("PARADOX_SCRIPT.DATABASE_OBJECT_TYPE", KEYWORD)
    @JvmField
    val DATABASE_OBJECT_KEY = createTextAttributesKey("PARADOX_SCRIPT.DATABASE_OBJECT")
    @JvmField
    val DEFINE_PREFIX_KEY = createTextAttributesKey("PARADOX_SCRIPT.DEFINE_PREFIX", KEYWORD)
    @JvmField
    val DEFINE_NAMESPACE_KEY = createTextAttributesKey("PARADOX_SCRIPT.DEFINE_NAMESPACE", INSTANCE_METHOD)
    @JvmField
    val DEFINE_VARIABLE_KEY = createTextAttributesKey("PARADOX_SCRIPT.DEFINE_VARIABLE", GLOBAL_VARIABLE)
}
