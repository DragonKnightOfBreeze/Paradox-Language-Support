package icu.windea.pls.localisation.editor

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import icu.windea.pls.script.editor.ParadoxScriptHighlighterColors

object ParadoxLocalisationHighlighterColors {
    @JvmField val OPERATOR = createTextAttributesKey("PARADOX_LOCALISATION.OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    @JvmField val MARKER = createTextAttributesKey("PARADOX_LOCALISATION.MARKER", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val COMMENT = createTextAttributesKey("PARADOX_LOCALISATION.COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    @JvmField val KEYWORD = createTextAttributesKey("PARADOX_LOCALISATION.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val NUMBER = createTextAttributesKey("PARADOX_LOCALISATION.NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    @JvmField val LOCALE = createTextAttributesKey("PARADOX_LOCALISATION.LOCALE", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val PROPERTY_KEY = createTextAttributesKey("PARADOX_LOCALISATION.PROPERTY_KEY", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val AT_SIGN = createTextAttributesKey("PARADOX_LOCALISATION.AT_SIGN", ParadoxScriptHighlighterColors.AT_SIGN)
    @JvmField val SCRIPTED_VARIABLE_REFERENCE = createTextAttributesKey("PARADOX_LOCALISATION.SCRIPTED_VARIABLE_REFERENCE", ParadoxScriptHighlighterColors.SCRIPTED_VARIABLE_REFERENCE)
    @JvmField val PARAMETER = createTextAttributesKey("PARADOX_LOCALISATION.PARAMETER", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val ARGUMENT = createTextAttributesKey("PARADOX_LOCALISATION.ARGUMENT") // KOTLIN_NAMED_ARGUMENT
    @JvmField val TEXT = createTextAttributesKey("PARADOX_LOCALISATION.TEXT", DefaultLanguageHighlighterColors.STRING)
    @JvmField val COLOR = createTextAttributesKey("PARADOX_LOCALISATION.COLOR", DefaultLanguageHighlighterColors.IDENTIFIER)
    @JvmField val ICON = createTextAttributesKey("PARADOX_LOCALISATION.ICON", DefaultLanguageHighlighterColors.IDENTIFIER) // #5C8AE6
    @JvmField val COMMAND = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND", DefaultLanguageHighlighterColors.IDENTIFIER)
    @JvmField val CONCEPT = createTextAttributesKey("PARADOX_LOCALISATION.CONCEPT", DefaultLanguageHighlighterColors.IDENTIFIER) // #008080
    @JvmField val TEXT_ICON = createTextAttributesKey("PARADOX_LOCALISATION.TEXT_ICON", DefaultLanguageHighlighterColors.IDENTIFIER)
    @JvmField val TEXT_FORMAT = createTextAttributesKey("PARADOX_LOCALISATION.TEXT_FORMAT", DefaultLanguageHighlighterColors.IDENTIFIER)
    @JvmField val VALID_ESCAPE = createTextAttributesKey("PARADOX_LOCALISATION.VALID_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    @JvmField val INVALID_ESCAPE = createTextAttributesKey("PARADOX_LOCALISATION.INVALID_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
    @JvmField val BAD_CHARACTER = createTextAttributesKey("PARADOX_LOCALISATION.BAD_CHARACTER", com.intellij.openapi.editor.HighlighterColors.BAD_CHARACTER)

    @JvmField val SEMANTIC_OPERATOR = createTextAttributesKey("PARADOX_LOCALISATION.SEMANTIC_OPERATOR", ParadoxScriptHighlighterColors.SEMANTIC_OPERATOR)
    @JvmField val SEMANTIC_MARKER = createTextAttributesKey("PARADOX_LOCALISATION.SEMANTIC_MARKER", ParadoxScriptHighlighterColors.SEMANTIC_MARKER)
    @JvmField val SEMANTIC_KEYWORD = createTextAttributesKey("PARADOX_LOCALISATION.SEMANTIC_KEYWORD", ParadoxScriptHighlighterColors.SEMANTIC_KEYWORD)
    @JvmField val SEMANTIC_STRING = createTextAttributesKey("PARADOX_LOCALISATION.SEMANTIC_STRING", ParadoxScriptHighlighterColors.SEMANTIC_STRING)

    @JvmField val DEFINITION_REFERENCE = createTextAttributesKey("PARADOX_LOCALISATION.DEFINITION_REFERENCE", ParadoxScriptHighlighterColors.DEFINITION_REFERENCE)
    @JvmField val LOCALISATION_REFERENCE = createTextAttributesKey("PARADOX_LOCALISATION.LOCALISATION_REFERENCE", ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE)
    @JvmField val DYNAMIC_VALUE = createTextAttributesKey("PARADOX_LOCALISATION.DYNAMIC_VALUE", ParadoxScriptHighlighterColors.DYNAMIC_VALUE)
    @JvmField val VARIABLE = createTextAttributesKey("PARADOX_LOCALISATION.VARIABLE", ParadoxScriptHighlighterColors.VARIABLE)
    @JvmField val SYSTEM_COMMAND_SCOPE = createTextAttributesKey("PARADOX_LOCALISATION.SYSTEM_COMMAND_SCOPE", ParadoxScriptHighlighterColors.SYSTEM_COMMAND_SCOPE)
    @JvmField val COMMAND_SCOPE = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND_SCOPE", ParadoxScriptHighlighterColors.COMMAND_SCOPE)
    @JvmField val COMMAND_SCOPE_PREFIX = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND_SCOPE_PREFIX", ParadoxScriptHighlighterColors.COMMAND_SCOPE_PREFIX)
    @JvmField val COMMAND_FIELD = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND_FIELD", ParadoxScriptHighlighterColors.COMMAND_FIELD)
    @JvmField val COMMAND_FIELD_PREFIX = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND_FIELD_PREFIX", ParadoxScriptHighlighterColors.COMMAND_FIELD_PREFIX)
    @JvmField val DATABASE_OBJECT_TYPE = createTextAttributesKey("PARADOX_LOCALISATION.DATABASE_OBJECT_TYPE", ParadoxScriptHighlighterColors.DATABASE_OBJECT_TYPE)
    @JvmField val DATABASE_OBJECT = createTextAttributesKey("PARADOX_LOCALISATION.DATABASE_OBJECT", ParadoxScriptHighlighterColors.DATABASE_OBJECT)
}
