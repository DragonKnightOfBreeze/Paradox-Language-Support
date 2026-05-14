package icu.windea.pls.script.editor

import com.intellij.ide.highlighter.custom.CustomHighlighterColors
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey.*

object ParadoxScriptAttributesKeys {
    @JvmField val BRACES = createTextAttributesKey("PARADOX_SCRIPT.BRACES", DefaultLanguageHighlighterColors.BRACES)
    @JvmField val OPERATOR = createTextAttributesKey("PARADOX_SCRIPT.OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    @JvmField val MARKER = createTextAttributesKey("PARADOX_SCRIPT.MARKER", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val PARAMETER_CONDITION_BRACKETS = createTextAttributesKey("PARADOX_SCRIPT.PARAMETER_CONDITION_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    @JvmField val PARAMETER_CONDITION_EXPRESSION_BRACKETS = createTextAttributesKey("PARADOX_SCRIPT.PARAMETER_CONDITION_EXPRESSION_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    @JvmField val INLINE_MATH_BRACKETS = createTextAttributesKey("PARADOX_SCRIPT.INLINE_MATH_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    @JvmField val INLINE_MATH_OPERATOR = createTextAttributesKey("PARADOX_SCRIPT.INLINE_MATH_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    @JvmField val COMMENT = createTextAttributesKey("PARADOX_SCRIPT.COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    @JvmField val KEYWORD = createTextAttributesKey("PARADOX_SCRIPT.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val AT_SIGN = createTextAttributesKey("PARADOX_SCRIPT.AT_SIGN", DefaultLanguageHighlighterColors.STATIC_FIELD)
    @JvmField val SCRIPTED_VARIABLE_NAME = createTextAttributesKey("PARADOX_SCRIPT.SCRIPTED_VARIABLE_NAME", DefaultLanguageHighlighterColors.STATIC_FIELD)
    @JvmField val SCRIPTED_VARIABLE_REFERENCE = createTextAttributesKey("PARADOX_SCRIPT.SCRIPTED_VARIABLE_REFERENCE", DefaultLanguageHighlighterColors.STATIC_FIELD)
    @JvmField val PARAMETER = createTextAttributesKey("PARADOX_SCRIPT.PARAMETER", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val CONDITION_PARAMETER = createTextAttributesKey("PARADOX_SCRIPT.CONDITION_PARAMETER", DefaultLanguageHighlighterColors.KEYWORD) // JAVA_TYPE_PARAMETER
    @JvmField val ARGUMENT = createTextAttributesKey("PARADOX_SCRIPT.ARGUMENT") // KOTLIN_NAMED_ARGUMENT
    @JvmField val PROPERTY_KEY = createTextAttributesKey("PARADOX_SCRIPT.PROPERTY_KEY", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    @JvmField val NUMBER = createTextAttributesKey("PARADOX_SCRIPT.NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    @JvmField val STRING = createTextAttributesKey("PARADOX_SCRIPT.STRING", DefaultLanguageHighlighterColors.STRING)
    @JvmField val COLOR = createTextAttributesKey("PARADOX_SCRIPT.COLOR", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    @JvmField val VALID_ESCAPE = createTextAttributesKey("PARADOX_SCRIPT.VALID_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    @JvmField val INVALID_ESCAPE = createTextAttributesKey("PARADOX_SCRIPT.INVALID_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
    @JvmField val BAD_CHARACTER = createTextAttributesKey("PARADOX_SCRIPT.BAD_CHARACTER", com.intellij.openapi.editor.HighlighterColors.BAD_CHARACTER)

    @JvmField val SEMANTIC_OPERATOR = createTextAttributesKey("PARADOX_SCRIPT.SEMANTIC_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    @JvmField val SEMANTIC_MARKER = createTextAttributesKey("PARADOX_SCRIPT.SEMANTIC_MARKER", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val SEMANTIC_KEYWORD = createTextAttributesKey("PARADOX_SCRIPT.SEMANTIC_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val SEMANTIC_ARGUMENT = createTextAttributesKey("PARADOX_SCRIPT.SEMANTIC_ARGUMENT") // KOTLIN_NAMED_ARGUMENT
    @JvmField val SEMANTIC_STRING = createTextAttributesKey("PARADOX_SCRIPT.SEMANTIC_STRING", DefaultLanguageHighlighterColors.STRING)

    @JvmField val DEFINITION = createTextAttributesKey("PARADOX_SCRIPT.DEFINITION", PROPERTY_KEY) // underscored
    @JvmField val DEFINITION_NAME = createTextAttributesKey("PARADOX_SCRIPT.DEFINITION_NAME") // background #223C23
    @JvmField val DEFINITION_REFERENCE = createTextAttributesKey("PARADOX_SCRIPT.DEFINITION_REFERENCE", DefaultLanguageHighlighterColors.INSTANCE_FIELD)  // dotted line #707D95
    @JvmField val LOCALISATION_REFERENCE = createTextAttributesKey("PARADOX_SCRIPT.LOCALISATION_REFERENCE", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val DEFINE_NAMESPACE = createTextAttributesKey("PARADOX_SCRIPT.DEFINE_NAMESPACE", DefaultLanguageHighlighterColors.STATIC_METHOD)
    @JvmField val DEFINE_VARIABLE = createTextAttributesKey("PARADOX_SCRIPT.DEFINE_VARIABLE", DefaultLanguageHighlighterColors.STATIC_FIELD)
    @JvmField val ENUM_VALUE = createTextAttributesKey("PARADOX_SCRIPT.ENUM_VALUE", DefaultLanguageHighlighterColors.STATIC_FIELD)
    @JvmField val COMPLEX_ENUM_VALUE = createTextAttributesKey("PARADOX_SCRIPT.COMPLEX_ENUM_VALUE", DefaultLanguageHighlighterColors.INSTANCE_FIELD) // dotted line #707D95, italic
    @JvmField val DYNAMIC_VALUE = createTextAttributesKey("PARADOX_SCRIPT.DYNAMIC_VALUE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
    @JvmField val VARIABLE = createTextAttributesKey("PARADOX_SCRIPT.VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE) // italic
    @JvmField val MODIFIER = createTextAttributesKey("PARADOX_SCRIPT.MODIFIER", CustomHighlighterColors.CUSTOM_KEYWORD2_ATTRIBUTES)
    @JvmField val TRIGGER = createTextAttributesKey("PARADOX_SCRIPT.TRIGGER", CustomHighlighterColors.CUSTOM_KEYWORD3_ATTRIBUTES)
    @JvmField val EFFECT = createTextAttributesKey("PARADOX_SCRIPT.EFFECT", CustomHighlighterColors.CUSTOM_KEYWORD4_ATTRIBUTES)
    @JvmField val SYSTEM_SCOPE = createTextAttributesKey("PARADOX_SCRIPT.SYSTEM_SCOPE", DefaultLanguageHighlighterColors.STATIC_METHOD)
    @JvmField val SCOPE = createTextAttributesKey("PARADOX_SCRIPT.SCOPE", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
    @JvmField val SCOPE_PREFIX = createTextAttributesKey("PARADOX_SCRIPT.SCOPE_PREFIX", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val VALUE_FIELD = createTextAttributesKey("PARADOX_SCRIPT.VALUE_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD) // SASS_VARIABLE
    @JvmField val VALUE_FIELD_PREFIX = createTextAttributesKey("PARADOX_SCRIPT.VALUE_FIELD_PREFIX", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val SYSTEM_COMMAND_SCOPE = createTextAttributesKey("PARADOX_SCRIPT.SYSTEM_COMMAND_SCOPE", DefaultLanguageHighlighterColors.STATIC_METHOD)
    @JvmField val COMMAND_SCOPE = createTextAttributesKey("PARADOX_SCRIPT.COMMAND_SCOPE", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
    @JvmField val COMMAND_SCOPE_PREFIX = createTextAttributesKey("PARADOX_SCRIPT.COMMAND_SCOPE_PREFIX", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val COMMAND_FIELD = createTextAttributesKey("PARADOX_SCRIPT.COMMAND_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD) // SASS_VARIABLE
    @JvmField val COMMAND_FIELD_PREFIX = createTextAttributesKey("PARADOX_SCRIPT.COMMAND_FIELD_PREFIX", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val DATABASE_OBJECT_TYPE = createTextAttributesKey("PARADOX_SCRIPT.DATABASE_OBJECT_TYPE", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val DATABASE_OBJECT = createTextAttributesKey("PARADOX_SCRIPT.DATABASE_OBJECT")

    @JvmField val TAG = createTextAttributesKey("PARADOX_SCRIPT.TAG", DefaultLanguageHighlighterColors.METADATA)
    @JvmField val MACRO = createTextAttributesKey("PARADOX_SCRIPT.MACRO", DefaultLanguageHighlighterColors.KEYWORD) // org.rust.MACRO
    @JvmField val PATH_REFERENCE = createTextAttributesKey("PARADOX_SCRIPT.PATH_REFERENCE", CodeInsightColors.INACTIVE_HYPERLINK_ATTRIBUTES)
    @JvmField val SHADER_EFFECT_REFERENCE = createTextAttributesKey("PARADOX_SCRIPT.SHADER_EFFECT_REFERENCE", DefaultLanguageHighlighterColors.FUNCTION_CALL)
    @JvmField val MESH_LOCATOR_REFERENCE = createTextAttributesKey("PARADOX_SCRIPT.MESH_LOCATOR_REFERENCE", DefaultLanguageHighlighterColors.FUNCTION_CALL)
}
