package icu.windea.pls.lang.codeInsight.highlighting

import com.intellij.lang.Language
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
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

object ParadoxAttributesKeysManager {
    // region Color Highlights

    @Suppress("DEPRECATION")
    private val colorKeyCache = CacheBuilder().build { color: Color ->
        val hex = ColorUtil.toHex(color).uppercase()
        createTextAttributesKey("PARADOX_LOCALISATION.COLOR_$hex", IDENTIFIER.defaultAttributes.clone().apply {
            foregroundColor = color
        })
    }
    @Suppress("DEPRECATION")
    private val colorOnlyKeyCache = CacheBuilder().build { color: Color ->
        val hex = ColorUtil.toHex(color).uppercase()
        createTextAttributesKey("PARADOX_LOCALISATION.COLOR_ONLY_$hex", TextAttributes().apply {
            foregroundColor = color
        })
    }

    fun getColorKey(color: Color): TextAttributesKey {
        return colorKeyCache.get(color)
    }

    fun getColorOnlyKey(color: Color): TextAttributesKey {
        return colorOnlyKeyCache.get(color)
    }

    // endregion

    // region Semantic Highlights

    fun getDefinitionReferenceKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DEFINITION_REFERENCE_KEY
            ParadoxCsvLanguage -> ParadoxCsvAttributesKeys.DEFINITION_REFERENCE_KEY
            else -> ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
        }
    }

    fun getLocalisationReferenceKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.LOCALISATION_REFERENCE_KEY
            else -> ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        }
    }

    fun getEnumValueKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxCsvLanguage -> ParadoxCsvAttributesKeys.ENUM_VALUE_KEY
            else -> ParadoxScriptAttributesKeys.ENUM_VALUE_KEY
        }
    }

    fun getComplexEnumValueKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxCsvLanguage -> ParadoxCsvAttributesKeys.COMPLEX_ENUM_VALUE_KEY
            else -> ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE_KEY
        }
    }

    fun getDynamicValueKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DYNAMIC_VALUE_KEY
            else -> ParadoxScriptAttributesKeys.DYNAMIC_VALUE_KEY
        }
    }

    fun getVariableKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.VARIABLE_KEY
            else -> ParadoxScriptAttributesKeys.VARIABLE_KEY
        }
    }

    fun getSystemCommandScopeKey(): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.SYSTEM_COMMAND_SCOPE_KEY
    }

    fun getCommandScopeKey(): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_KEY
    }

    fun getCommandFieldKey(): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.COMMAND_FIELD_KEY
    }

    fun getDatabaseObjectTypeKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            is ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_TYPE_KEY
            else -> ParadoxScriptAttributesKeys.DATABASE_OBJECT_TYPE_KEY
        }
    }

    fun getDatabaseObjectKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            is ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_KEY
            else -> ParadoxScriptAttributesKeys.DATABASE_OBJECT_KEY
        }
    }

    fun getCommandScopeLinkPrefixKey(): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_LINK_PREFIX_KEY
    }

    fun getCommandFieldPrefixKey(): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.COMMAND_FIELD_PREFIX_KEY
    }

    // endregion
}
