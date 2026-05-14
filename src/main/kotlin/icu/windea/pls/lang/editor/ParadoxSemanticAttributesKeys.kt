package icu.windea.pls.lang.editor

import com.intellij.lang.Language
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.ColorUtil
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.editor.ParadoxCsvAttributesKeys
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys
import java.awt.Color

object ParadoxSemanticAttributesKeys {
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
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.SEMANTIC_OPERATOR
            else -> ParadoxScriptAttributesKeys.SEMANTIC_OPERATOR
        }
    }

    fun marker(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.SEMANTIC_MARKER
            else -> ParadoxScriptAttributesKeys.SEMANTIC_MARKER
        }
    }

    fun keyword(language: Language? = null): TextAttributesKey {
        return when(language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.SEMANTIC_KEYWORD
            else ->ParadoxScriptAttributesKeys.SEMANTIC_KEYWORD
        }
    }

    fun argument(): TextAttributesKey {
        return ParadoxScriptAttributesKeys.SEMANTIC_ARGUMENT
    }

    fun string(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.SEMANTIC_STRING
            else -> ParadoxScriptAttributesKeys.SEMANTIC_STRING
        }
    }

    fun definitionReference(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DEFINITION_REFERENCE
            ParadoxCsvLanguage -> ParadoxCsvAttributesKeys.DEFINITION_REFERENCE
            else -> ParadoxScriptAttributesKeys.DEFINITION_REFERENCE
        }
    }

    fun localisationReference(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.LOCALISATION_REFERENCE
            else -> ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE
        }
    }

    fun defineNamespace(): TextAttributesKey {
        return ParadoxScriptAttributesKeys.DEFINE_NAMESPACE
    }

    fun defineVariable(): TextAttributesKey {
        return ParadoxScriptAttributesKeys.DEFINE_VARIABLE
    }

    fun enumValue(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxCsvLanguage -> ParadoxCsvAttributesKeys.ENUM_VALUE
            else -> ParadoxScriptAttributesKeys.ENUM_VALUE
        }
    }

    fun complexEnumValue(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxCsvLanguage -> ParadoxCsvAttributesKeys.COMPLEX_ENUM_VALUE
            else -> ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE
        }
    }

    fun dynamicValue(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DYNAMIC_VALUE
            else -> ParadoxScriptAttributesKeys.DYNAMIC_VALUE
        }
    }

    fun variable(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.VARIABLE
            else -> ParadoxScriptAttributesKeys.VARIABLE
        }
    }

    fun systemScope(): TextAttributesKey {
        return ParadoxScriptAttributesKeys.SYSTEM_SCOPE
    }

    fun scope(): TextAttributesKey {
        return ParadoxScriptAttributesKeys.SCOPE
    }

    fun scopePrefix(): TextAttributesKey {
        return ParadoxScriptAttributesKeys.SCOPE_PREFIX
    }

    fun valueField(): TextAttributesKey {
        return ParadoxScriptAttributesKeys.VALUE_FIELD
    }

    fun valueFieldPrefix(): TextAttributesKey {
        return ParadoxScriptAttributesKeys.VALUE_FIELD_PREFIX
    }

    fun systemCommandScope(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.SYSTEM_COMMAND_SCOPE
            else -> ParadoxScriptAttributesKeys.SYSTEM_COMMAND_SCOPE
        }
    }

    fun commandScope(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.COMMAND_SCOPE
            else -> ParadoxScriptAttributesKeys.COMMAND_SCOPE
        }
    }

    fun commandScopePrefix(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_PREFIX
            else -> ParadoxScriptAttributesKeys.COMMAND_SCOPE_PREFIX
        }
    }

    fun commandField(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.COMMAND_FIELD
            else -> ParadoxScriptAttributesKeys.COMMAND_FIELD
        }
    }

    fun commandFieldPrefix(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.COMMAND_FIELD_PREFIX
            else -> ParadoxScriptAttributesKeys.COMMAND_FIELD_PREFIX
        }
    }

    fun databaseObjectType(language: Language? = null): TextAttributesKey {
        return when (language) {
            is ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_TYPE
            else -> ParadoxScriptAttributesKeys.DATABASE_OBJECT_TYPE
        }
    }

    fun databaseObject(language: Language? = null): TextAttributesKey {
        return when (language) {
            is ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DATABASE_OBJECT
            else -> ParadoxScriptAttributesKeys.DATABASE_OBJECT
        }
    }
}
