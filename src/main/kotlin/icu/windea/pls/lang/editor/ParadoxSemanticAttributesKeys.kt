package icu.windea.pls.lang.editor

import com.intellij.lang.Language
import com.intellij.openapi.editor.colors.TextAttributesKey
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.editor.ParadoxCsvAttributesKeys
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

object ParadoxSemanticAttributesKeys {
    fun marker(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.MARKER
            else -> ParadoxScriptAttributesKeys.MARKER
        }
    }

    fun operator(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.OPERATOR
            else -> ParadoxScriptAttributesKeys.OPERATOR
        }
    }

    fun text(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.TEXT
            else -> ParadoxScriptAttributesKeys.STRING
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

    @Suppress("unused")
    fun syncedLocalisationReference(): TextAttributesKey {
        return ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE
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

    fun valueField(): TextAttributesKey {
        return ParadoxScriptAttributesKeys.VALUE_FIELD
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

    fun commandField(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.COMMAND_FIELD
            else -> ParadoxScriptAttributesKeys.COMMAND_FIELD
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

    fun scopeLinkPrefix(): TextAttributesKey {
        return ParadoxScriptAttributesKeys.SCOPE_LINK_PREFIX
    }

    fun valueFieldPrefix(): TextAttributesKey {
        return ParadoxScriptAttributesKeys.VALUE_FIELD_PREFIX
    }

    fun commandScopeLinkPrefix(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_LINK_PREFIX
            else -> ParadoxScriptAttributesKeys.COMMAND_SCOPE_LINK_PREFIX
        }
    }

    fun commandFieldPrefix(language: Language? = null): TextAttributesKey {
        return when (language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.COMMAND_FIELD_PREFIX
            else -> ParadoxScriptAttributesKeys.COMMAND_FIELD_PREFIX
        }
    }

    fun definePrefix(): TextAttributesKey {
        return ParadoxScriptAttributesKeys.DEFINE_PREFIX
    }
}
