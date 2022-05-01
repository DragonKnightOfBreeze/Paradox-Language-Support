package icu.windea.pls.localisation.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import com.intellij.openapi.editor.markup.*
import icu.windea.pls.*
import java.awt.*

@Suppress("DEPRECATION")
object ParadoxLocalisationAttributesKeys {
	@JvmField val SEPARATOR_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.separator"), OPERATION_SIGN)
	@JvmField val NUMBER_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.number"), NUMBER)
	@JvmField val LOCALE_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.locale"), KEYWORD)
	@JvmField val PROPERTY_KEY_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.propertyKey"), KEYWORD)
	@JvmField val STRING_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.string"), STRING)
	@JvmField val COMMENT_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.comment"), LINE_COMMENT)
	@JvmField val MARKER_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.marker"), KEYWORD)
	@JvmField val PROPERTY_REFERENCE_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.propertyReference"), KEYWORD)
	@JvmField val PARAMETER_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.parameter"), IDENTIFIER)
	@JvmField val ICON_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.icon"), IDENTIFIER)
	@JvmField val SEQUENTIAL_NUMBER_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.sequentialNumber"), IDENTIFIER)
	@JvmField val COMMAND_SCOPE_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.commandScope"), IDENTIFIER)
	@JvmField val COMMAND_FIELD_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.commandField"), IDENTIFIER)
	@JvmField val COLOR_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.color"), IDENTIFIER)
	@JvmField val VALID_ESCAPE_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.validEscape"), VALID_STRING_ESCAPE)
	@JvmField val INVALID_ESCAPE_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.invalidEscape"), INVALID_STRING_ESCAPE)
	@JvmField val BAD_CHARACTER_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.badCharacter"), BAD_CHARACTER)
	@JvmField val LOCALISATION_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.localisation"), PROPERTY_KEY_KEY)
	@JvmField val SYNCED_LOCALISATION_KEY = createTextAttributesKey(PlsBundle.message("localisation.externalName.syncedLocalisation"), PROPERTY_KEY_KEY)
	
	val COLOR_KEYS by lazy {
		val colorPrefix = PlsBundle.message("localisation.externalName.color")
		getInternalConfig().colorMap.mapValues { (_, color) ->
			createTextAttributesKey("${colorPrefix}_${color.id}", IDENTIFIER.defaultAttributes.clone().apply {
				foregroundColor = color.color
			})
		}
	}
}
