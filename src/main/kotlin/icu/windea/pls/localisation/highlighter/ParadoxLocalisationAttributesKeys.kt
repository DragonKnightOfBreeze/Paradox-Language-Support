@file:Suppress("DEPRECATION")

package icu.windea.pls.localisation.highlighter

import com.google.common.cache.*
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import com.intellij.openapi.editor.markup.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import java.awt.*

object ParadoxLocalisationAttributesKeys {
	@JvmField val OPERATOR_KEY = createTextAttributesKey("PARADOX_LOCALISATION.OPERATOR", OPERATION_SIGN)
	@JvmField val MARKER_KEY = createTextAttributesKey("PARADOX_LOCALISATION.MARKER", KEYWORD)
	@JvmField val COMMENT_KEY = createTextAttributesKey("PARADOX_LOCALISATION.COMMENT", LINE_COMMENT)
	@JvmField val NUMBER_KEY = createTextAttributesKey("PARADOX_LOCALISATION.NUMBER", NUMBER)
	@JvmField val LOCALE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.LOCALE", KEYWORD)
	@JvmField val PROPERTY_KEY_KEY = createTextAttributesKey("PARADOX_LOCALISATION.PROPERTY_KEY", KEYWORD)
	@JvmField val STRING_KEY = createTextAttributesKey("PARADOX_LOCALISATION.STRING", STRING)
	@JvmField val PROPERTY_REFERENCE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.PROPERTY_REFERENCE", KEYWORD)
	@JvmField val PROPERTY_REFERENCE_PARAMETER_KEY = createTextAttributesKey("PARADOX_LOCALISATION.PROPERTY_REFERENCE_PARAMETER", KEYWORD)
	@JvmField val ICON_KEY = createTextAttributesKey("PARADOX_LOCALISATION.ICON", IDENTIFIER) //#5C8AE6
	@JvmField val COMMAND_SCOPE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND_SCOPE", IDENTIFIER)
	@JvmField val COMMAND_FIELD_KEY = createTextAttributesKey("PARADOX_LOCALISATION.COMMAND_FIELD", IDENTIFIER)
	@JvmField val COLOR_KEY = createTextAttributesKey("PARADOX_LOCALISATION.COLOR", IDENTIFIER)
	@JvmField val VALID_ESCAPE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.VALID_ESCAPE", VALID_STRING_ESCAPE)
	@JvmField val INVALID_ESCAPE_KEY = createTextAttributesKey("PARADOX_LOCALISATION.INVALID_ESCAPE", INVALID_STRING_ESCAPE)
	@JvmField val BAD_CHARACTER_KEY = createTextAttributesKey("PARADOX_LOCALISATION.BAD_CHARACTER", BAD_CHARACTER)
	
	@JvmField val LOCALISATION_KEY = createTextAttributesKey("PARADOX_LOCALISATION.LOCALISATION", PROPERTY_KEY_KEY) //underscored
	@JvmField val SYNCED_LOCALISATION_KEY = createTextAttributesKey("PARADOX_LOCALISATION.SYNCED_LOCALISATION", PROPERTY_KEY_KEY) //underscored
	
	private val colorKeyCache = CacheBuilder.newBuilder().buildCache { color: Color ->
		createTextAttributesKey("PARADOX_LOCALISATION.COLOR_${color.rgb}", IDENTIFIER.defaultAttributes.clone().apply {
			foregroundColor = color
		})
	}
	
	@JvmStatic
	fun getColorKey(color: Color): TextAttributesKey? {
		return colorKeyCache.get(color)
	}
	
	private val colorOnlyKeyCache = CacheBuilder.newBuilder().buildCache { color: Color ->
		createTextAttributesKey("PARADOX_LOCALISATION.COLOR_${color.rgb}", TextAttributes().apply {
			foregroundColor = color
		})
	}
	
	@JvmStatic
	fun getColorOnlyKey(color: Color): TextAttributesKey? {
		return colorOnlyKeyCache.get(color)
	}
}
