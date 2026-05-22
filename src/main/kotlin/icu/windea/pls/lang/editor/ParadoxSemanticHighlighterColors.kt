package icu.windea.pls.lang.editor

import com.intellij.lang.Language
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.ColorUtil
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.editor.ParadoxCsvHighlighterColors
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.editor.ParadoxLocalisationHighlighterColors
import icu.windea.pls.script.editor.ParadoxScriptHighlighterColors
import java.awt.Color

object ParadoxSemanticHighlighterColors {
    // NOTE [compatibility] `TextAttributesKey.createTextAttributesKey(String, TextAttributesKey)` is deprecated - But necessary and intended here
    @Suppress("DEPRECATION")
    private val colorKeyCache = CacheBuilder().build { color: Color ->
        val hex = ColorUtil.toHex(color).uppercase()
        val externalName = "PARADOX_LOCALISATION.COLOR_$hex"
        val defaultAttributes = DefaultLanguageHighlighterColors.IDENTIFIER.defaultAttributes.clone().apply { foregroundColor = color }
        createTextAttributesKey(externalName, defaultAttributes)
    }
    @Suppress("DEPRECATION")
    private val colorOnlyKeyCache = CacheBuilder().build { color: Color ->
        val hex = ColorUtil.toHex(color).uppercase()
        val externalName = "PARADOX_LOCALISATION.COLOR_ONLY_$hex"
        val defaultAttributes = TextAttributes().apply { foregroundColor = color }
        createTextAttributesKey(externalName, defaultAttributes)
    }

    fun color(color: Color): TextAttributesKey {
        return colorKeyCache.get(color)
    }

    fun colorOnly(color: Color): TextAttributesKey {
        return colorOnlyKeyCache.get(color)
    }

    fun operator(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.SEMANTIC_OPERATOR
            else -> ParadoxScriptHighlighterColors.SEMANTIC_OPERATOR
        }
    }

    fun marker(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.SEMANTIC_MARKER
            else -> ParadoxScriptHighlighterColors.SEMANTIC_MARKER
        }
    }

    fun keyword(language: Language? = null): TextAttributesKey {
        return when(language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.SEMANTIC_KEYWORD
            else ->ParadoxScriptHighlighterColors.SEMANTIC_KEYWORD
        }
    }

    fun argument(): TextAttributesKey {
        return ParadoxScriptHighlighterColors.SEMANTIC_ARGUMENT
    }

    fun string(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.SEMANTIC_STRING
            else -> ParadoxScriptHighlighterColors.SEMANTIC_STRING
        }
    }

    fun definitionReference(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.DEFINITION_REFERENCE
            ParadoxCsvLanguage -> ParadoxCsvHighlighterColors.DEFINITION_REFERENCE
            else -> ParadoxScriptHighlighterColors.DEFINITION_REFERENCE
        }
    }

    fun localisationReference(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.LOCALISATION_REFERENCE
            else -> ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE
        }
    }

    fun defineNamespace(): TextAttributesKey {
        return ParadoxScriptHighlighterColors.DEFINE_NAMESPACE
    }

    fun defineVariable(): TextAttributesKey {
        return ParadoxScriptHighlighterColors.DEFINE_VARIABLE
    }

    fun enumValue(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxCsvLanguage -> ParadoxCsvHighlighterColors.ENUM_VALUE
            else -> ParadoxScriptHighlighterColors.ENUM_VALUE
        }
    }

    fun complexEnumValue(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxCsvLanguage -> ParadoxCsvHighlighterColors.COMPLEX_ENUM_VALUE
            else -> ParadoxScriptHighlighterColors.COMPLEX_ENUM_VALUE
        }
    }

    fun dynamicValue(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.DYNAMIC_VALUE
            else -> ParadoxScriptHighlighterColors.DYNAMIC_VALUE
        }
    }

    fun variable(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.VARIABLE
            else -> ParadoxScriptHighlighterColors.VARIABLE
        }
    }

    fun systemScope(): TextAttributesKey {
        return ParadoxScriptHighlighterColors.SYSTEM_SCOPE
    }

    fun scope(): TextAttributesKey {
        return ParadoxScriptHighlighterColors.SCOPE
    }

    fun scopePrefix(): TextAttributesKey {
        return ParadoxScriptHighlighterColors.SCOPE_PREFIX
    }

    fun valueField(): TextAttributesKey {
        return ParadoxScriptHighlighterColors.VALUE_FIELD
    }

    fun valueFieldPrefix(): TextAttributesKey {
        return ParadoxScriptHighlighterColors.VALUE_FIELD_PREFIX
    }

    fun systemCommandScope(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.SYSTEM_COMMAND_SCOPE
            else -> ParadoxScriptHighlighterColors.SYSTEM_COMMAND_SCOPE
        }
    }

    fun commandScope(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.COMMAND_SCOPE
            else -> ParadoxScriptHighlighterColors.COMMAND_SCOPE
        }
    }

    fun commandScopePrefix(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.COMMAND_SCOPE_PREFIX
            else -> ParadoxScriptHighlighterColors.COMMAND_SCOPE_PREFIX
        }
    }

    fun commandField(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.COMMAND_FIELD
            else -> ParadoxScriptHighlighterColors.COMMAND_FIELD
        }
    }

    fun commandFieldPrefix(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.COMMAND_FIELD_PREFIX
            else -> ParadoxScriptHighlighterColors.COMMAND_FIELD_PREFIX
        }
    }

    fun databaseObjectType(language: Language? = null): TextAttributesKey {
        return when (language) {
            is ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.DATABASE_OBJECT_TYPE
            else -> ParadoxScriptHighlighterColors.DATABASE_OBJECT_TYPE
        }
    }

    fun databaseObject(language: Language? = null): TextAttributesKey {
        return when (language) {
            is ParadoxLocalisationLanguage -> ParadoxLocalisationHighlighterColors.DATABASE_OBJECT
            else -> ParadoxScriptHighlighterColors.DATABASE_OBJECT
        }
    }
}
