package icu.windea.pls.lang.codeInsight.highlighting

import com.intellij.lang.Language
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.colors.TextAttributesKey
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
        TextAttributesKey.createTextAttributesKey("PARADOX_LOCALISATION.COLOR_$hex", IDENTIFIER.defaultAttributes.clone().apply {
            foregroundColor = color
        })
    }
    @Suppress("DEPRECATION")
    private val colorOnlyKeyCache = CacheBuilder().build { color: Color ->
        val hex = ColorUtil.toHex(color).uppercase()
        TextAttributesKey.createTextAttributesKey("PARADOX_LOCALISATION.COLOR_ONLY_$hex", TextAttributes().apply {
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
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DEFINITION_REFERENCE
            ParadoxCsvLanguage -> ParadoxCsvAttributesKeys.DEFINITION_REFERENCE
            else -> ParadoxScriptAttributesKeys.DEFINITION_REFERENCE
        }
    }

    fun getLocalisationReferenceKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.LOCALISATION_REFERENCE
            else -> ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE
        }
    }

    fun getEnumValueKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxCsvLanguage -> ParadoxCsvAttributesKeys.ENUM_VALUE
            else -> ParadoxScriptAttributesKeys.ENUM_VALUE
        }
    }

    fun getComplexEnumValueKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxCsvLanguage -> ParadoxCsvAttributesKeys.COMPLEX_ENUM_VALUE
            else -> ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE
        }
    }

    fun getDynamicValueKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DYNAMIC_VALUE
            else -> ParadoxScriptAttributesKeys.DYNAMIC_VALUE
        }
    }

    fun getVariableKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.VARIABLE
            else -> ParadoxScriptAttributesKeys.VARIABLE
        }
    }

    fun getSystemCommandScopeKey(): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.SYSTEM_COMMAND_SCOPE
    }

    fun getCommandScopeKey(): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.COMMAND_SCOPE
    }

    fun getCommandFieldKey(): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.COMMAND_FIELD
    }

    fun getDatabaseObjectTypeKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            is ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_TYPE
            else -> ParadoxScriptAttributesKeys.DATABASE_OBJECT_TYPE
        }
    }

    fun getDatabaseObjectKey(language: Language? = null): TextAttributesKey {
        return when (language) {
            is ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DATABASE_OBJECT
            else -> ParadoxScriptAttributesKeys.DATABASE_OBJECT
        }
    }

    fun getCommandScopeLinkPrefixKey(): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_LINK_PREFIX
    }

    fun getCommandFieldPrefixKey(): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.COMMAND_FIELD_PREFIX
    }

    // endregion
}
