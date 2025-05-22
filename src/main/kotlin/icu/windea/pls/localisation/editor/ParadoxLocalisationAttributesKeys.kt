@file:Suppress("DEPRECATION")

package icu.windea.pls.localisation.editor

import com.google.common.cache.*
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import com.intellij.openapi.editor.markup.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.editor.*
import java.awt.*

object ParadoxLocalisationAttributesKeys {
    @JvmField
    val OPERATOR_KEY = createTextAttributesKey("PARADOX_LOCALISATION.OPERATOR", OPERATION_SIGN)
    @JvmField
    val MARKER_KEY = createTextAttributesKey("PARADOX_LOCALISATION.MARKER", KEYWORD)
    @JvmField
    val COMMENT_KEY = createTextAttributesKey("PARADOX_LOCALISATION.COMMENT", LINE_COMMENT)
    @JvmField
    val NUMBER_KEY = createTextAttributesKey("PARADOX_LOCALISATION.NUMBER", NUMBER)
    @JvmField
    val LOCALE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.LOCALE", KEYWORD)
    @JvmField
    val PROPERTY_KEY_KEY = createTextAttributesKey("PARADOX_LOCALISATION.PROPERTY_KEY", KEYWORD)
    @JvmField
    val ARGUMENT_KEY = createTextAttributesKey("PARADOX_LOCALISATION.ARGUMENT_TOKEN", KEYWORD)
    @JvmField
    val COLOR_KEY = createTextAttributesKey("PARADOX_LOCALISATION.COLOR", IDENTIFIER)
    @JvmField
    val PARAMETER_KEY = createTextAttributesKey("PARADOX_LOCALISATION.PARAMETER", KEYWORD)
    @JvmField
    val SCRIPTED_VARIABLE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.SCRIPTED_VARIABLE", STATIC_FIELD)
    @JvmField
    val COMMAND_KEY = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND", IDENTIFIER)
    @JvmField
    val ICON_KEY = createTextAttributesKey("PARADOX_LOCALISATION.ICON", IDENTIFIER) //#5C8AE6
    @JvmField
    val CONCEPT_KEY = createTextAttributesKey("PARADOX_LOCALISATION.CONCEPT", IDENTIFIER) //#008080
    @JvmField
    val TEXT_FORMAT_KEY = createTextAttributesKey("PARADOX_LOCALISATION.TEXT_FORMAT", IDENTIFIER)
    @JvmField
    val TEXT_ICON_KEY = createTextAttributesKey("PARADOX_LOCALISATION.TEXT_ICON", IDENTIFIER)
    @JvmField
    val STRING_KEY = createTextAttributesKey("PARADOX_LOCALISATION.STRING", STRING)
    @JvmField
    val VALID_ESCAPE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.VALID_ESCAPE", VALID_STRING_ESCAPE)
    @JvmField
    val INVALID_ESCAPE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.INVALID_ESCAPE", INVALID_STRING_ESCAPE)
    @JvmField
    val BAD_CHARACTER_KEY = createTextAttributesKey("PARADOX_LOCALISATION.BAD_CHARACTER", BAD_CHARACTER)

    //unused
    //@JvmField val LOCALISATION_KEY = createTextAttributesKey("PARADOX_LOCALISATION.LOCALISATION", PROPERTY_KEY_KEY)
    //@JvmField val SYNCED_LOCALISATION_KEY = createTextAttributesKey("PARADOX_LOCALISATION.SYNCED_LOCALISATION", PROPERTY_KEY_KEY)

    @JvmField
    val SYSTEM_COMMAND_SCOPE_KEY = createTextAttributesKey("PARADOX_SCRIPT.SYSTEM_COMMAND_SCOPE", STATIC_METHOD)
    @JvmField
    val COMMAND_SCOPE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND_SCOPE", INSTANCE_METHOD)
    @JvmField
    val COMMAND_FIELD_KEY = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND_FIELD", IDENTIFIER)
    @JvmField
    val COMMAND_SCOPE_LINK_PREFIX_KEY = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND_SCOPE_LINK_PREFIX", KEYWORD)
    @JvmField
    val COMMAND_SCOPE_LINK_VALUE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND_SCOPE_LINK_VALUE")
    @JvmField
    val COMMAND_FIELD_PREFIX_KEY = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND_FIELD_PREFIX", KEYWORD)
    @JvmField
    val COMMAND_FIELD_VALUE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND_FIELD_VALUE")
    @JvmField
    val DYNAMIC_VALUE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.DYNAMIC_VALUE", GLOBAL_VARIABLE)
    @JvmField
    val VARIABLE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.VARIABLE", GLOBAL_VARIABLE) //italic

    @JvmField
    val DATABASE_OBJECT_TYPE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.DATABASE_OBJECT_TYPE", KEYWORD)
    @JvmField
    val DATABASE_OBJECT_KEY = createTextAttributesKey("PARADOX_LOCALISATION.DATABASE_OBJECT", ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY)

    private val colorKeyCache = CacheBuilder.newBuilder().buildCache { color: Color ->
        createTextAttributesKey("PARADOX_LOCALISATION.COLOR_${color.rgb}", IDENTIFIER.defaultAttributes.clone().apply {
            foregroundColor = color
        })
    }

    @JvmStatic
    fun getColorKey(color: Color): TextAttributesKey? {
        if(!getSettings().others.highlightLocalisationColorId) return null
        return colorKeyCache.get(color)
    }

    private val colorOnlyKeyCache = CacheBuilder.newBuilder().buildCache { color: Color ->
        createTextAttributesKey("PARADOX_LOCALISATION.COLOR_ONLY_${color.rgb}", TextAttributes().apply {
            foregroundColor = color
        })
    }

    @JvmStatic
    fun getColorOnlyKey(color: Color): TextAttributesKey? {
        if(!getSettings().others.highlightLocalisationColorId) return null
        return colorOnlyKeyCache.get(color)
    }
}
