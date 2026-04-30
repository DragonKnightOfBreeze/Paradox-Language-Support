package icu.windea.pls.localisation.editor

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

object ParadoxLocalisationAttributesKeys {
    @JvmField val OPERATOR = create("PARADOX_LOCALISATION.OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    @JvmField val MARKER = create("PARADOX_LOCALISATION.MARKER", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val COMMENT = create("PARADOX_LOCALISATION.COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    @JvmField val NUMBER = create("PARADOX_LOCALISATION.NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    @JvmField val LOCALE = create("PARADOX_LOCALISATION.LOCALE", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val PROPERTY_KEY = create("PARADOX_LOCALISATION.PROPERTY_KEY", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val ARGUMENT = create("PARADOX_LOCALISATION.ARGUMENT_TOKEN", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val AT_SIGN = create("PARADOX_LOCALISATION.AT_SIGN", ParadoxScriptAttributesKeys.AT_SIGN)
    @JvmField val SCRIPTED_VARIABLE_REFERENCE = create("PARADOX_LOCALISATION.SCRIPTED_VARIABLE_REFERENCE", ParadoxScriptAttributesKeys.SCRIPTED_VARIABLE_REFERENCE)
    @JvmField val COLOR = create("PARADOX_LOCALISATION.COLOR", DefaultLanguageHighlighterColors.IDENTIFIER)
    @JvmField val PARAMETER = create("PARADOX_LOCALISATION.PARAMETER", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val ICON = create("PARADOX_LOCALISATION.ICON", DefaultLanguageHighlighterColors.IDENTIFIER) // #5C8AE6
    @JvmField val COMMAND = create("PARADOX_LOCALISATION.COMMAND", DefaultLanguageHighlighterColors.IDENTIFIER)
    @JvmField val CONCEPT = create("PARADOX_LOCALISATION.CONCEPT", DefaultLanguageHighlighterColors.IDENTIFIER) // #008080
    @JvmField val TEXT_ICON = create("PARADOX_LOCALISATION.TEXT_ICON", DefaultLanguageHighlighterColors.IDENTIFIER)
    @JvmField val TEXT_FORMAT = create("PARADOX_LOCALISATION.TEXT_FORMAT", DefaultLanguageHighlighterColors.IDENTIFIER)
    @JvmField val TEXT = create("PARADOX_LOCALISATION.TEXT", DefaultLanguageHighlighterColors.STRING)
    @JvmField val VALID_ESCAPE = create("PARADOX_LOCALISATION.VALID_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    @JvmField val INVALID_ESCAPE = create("PARADOX_LOCALISATION.INVALID_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
    @JvmField val BAD_CHARACTER = create("PARADOX_LOCALISATION.BAD_CHARACTER", com.intellij.openapi.editor.HighlighterColors.BAD_CHARACTER)

    @JvmField val DEFINITION_REFERENCE = create("PARADOX_LOCALISATION.DEFINITION_REFERENCE", ParadoxScriptAttributesKeys.DEFINITION_REFERENCE)
    @JvmField val LOCALISATION_REFERENCE = create("PARADOX_LOCALISATION.LOCALISATION_REFERENCE", ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE)
    @JvmField val DYNAMIC_VALUE = create("PARADOX_LOCALISATION.DYNAMIC_VALUE", ParadoxScriptAttributesKeys.DYNAMIC_VALUE)
    @JvmField val VARIABLE = create("PARADOX_LOCALISATION.VARIABLE", ParadoxScriptAttributesKeys.VARIABLE)
    @JvmField val SYSTEM_COMMAND_SCOPE = create("PARADOX_SCRIPT.SYSTEM_COMMAND_SCOPE", DefaultLanguageHighlighterColors.STATIC_METHOD)
    @JvmField val COMMAND_SCOPE = create("PARADOX_LOCALISATION.COMMAND_SCOPE", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
    @JvmField val COMMAND_FIELD = create("PARADOX_LOCALISATION.COMMAND_FIELD", DefaultLanguageHighlighterColors.IDENTIFIER)
    @JvmField val DATABASE_OBJECT_TYPE = create("PARADOX_LOCALISATION.DATABASE_OBJECT_TYPE", ParadoxScriptAttributesKeys.DATABASE_OBJECT_TYPE)
    @JvmField val DATABASE_OBJECT = create("PARADOX_LOCALISATION.DATABASE_OBJECT", ParadoxScriptAttributesKeys.DATABASE_OBJECT)

    @JvmField val COMMAND_SCOPE_LINK_PREFIX = create("PARADOX_LOCALISATION.COMMAND_SCOPE_LINK_PREFIX", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val COMMAND_FIELD_PREFIX = create("PARADOX_LOCALISATION.COMMAND_FIELD_PREFIX", DefaultLanguageHighlighterColors.KEYWORD)

    private fun create(name: String, fallback: TextAttributesKey? = null): TextAttributesKey {
        if (fallback == null) return TextAttributesKey.createTextAttributesKey(name)
        return TextAttributesKey.createTextAttributesKey(name, fallback)
    }
}
