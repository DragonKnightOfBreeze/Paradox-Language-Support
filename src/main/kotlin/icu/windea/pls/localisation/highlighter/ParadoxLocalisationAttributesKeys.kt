package icu.windea.pls.localisation.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.*
import java.awt.*

@Suppress("DEPRECATION")
object ParadoxLocalisationAttributesKeys {
	@JvmField val OPERATOR_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.operator"), OPERATION_SIGN)
	@JvmField val MARKER_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.marker"), KEYWORD)
	@JvmField val COMMENT_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.comment"), LINE_COMMENT)
	@JvmField val NUMBER_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.number"), NUMBER)
	@JvmField val LOCALE_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.locale"), KEYWORD)
	@JvmField val PROPERTY_KEY_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.propertyKey"), KEYWORD)
	@JvmField val STRING_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.string"), STRING)
	@JvmField val PROPERTY_REFERENCE_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.propertyReference"), KEYWORD)
	@JvmField val ICON_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.icon"), IDENTIFIER)
	@JvmField val ICON_PARAMETER_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.iconParameter"), IDENTIFIER)
	@JvmField val COMMAND_SCOPE_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.commandScope"), IDENTIFIER)
	@JvmField val COMMAND_FIELD_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.commandField"), IDENTIFIER)
	@JvmField val COLOR_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.color"), IDENTIFIER)
	@JvmField val VALID_ESCAPE_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.validEscape"), VALID_STRING_ESCAPE)
	@JvmField val INVALID_ESCAPE_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.invalidEscape"), INVALID_STRING_ESCAPE)
	@JvmField val BAD_CHARACTER_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.badCharacter"), BAD_CHARACTER)
	
	@JvmField val LOCALISATION_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.localisation"), PROPERTY_KEY_KEY)
	@JvmField val SYNCED_LOCALISATION_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.syncedLocalisation"), PROPERTY_KEY_KEY)
	
	private val colorKeyCache = createCache<Color, TextAttributesKey> { color ->
		createTextAttributesKey("${PlsBundle.message("localisation.externalName.color")}_${color.rgb}", IDENTIFIER.defaultAttributes.clone().apply {
			foregroundColor = color
		})
	}
	
	@JvmStatic
	fun getColorKey(id: String, project: Project): TextAttributesKey? {
		val color = InternalConfigHandler.getColor(id, project)?.color ?: return null
		return colorKeyCache.get(color)
	}
	
	private val colorOnlyKeyCache = createCache<Color, TextAttributesKey> { color ->
		createTextAttributesKey("${PlsBundle.message("localisation.externalName.color")}_${color.rgb}", TextAttributes().apply {
			foregroundColor = color
		})
	}
	
	@JvmStatic
	fun getColorOnlyKey(id: String, project: Project): TextAttributesKey? {
		val color = InternalConfigHandler.getColor(id, project)?.color ?: return null
		return colorOnlyKeyCache.get(color)
	}
}
