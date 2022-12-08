package icu.windea.pls.localisation.editor

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.colors.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.highlighter.*

class ParadoxLocalisationColorSettingsPage : ColorSettingsPage {
	companion object {
		private val attributesDescriptors = arrayOf(
			AttributesDescriptor(PlsBundle.message("localisation.displayName.operator"), ParadoxLocalisationAttributesKeys.OPERATOR_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.marker"), ParadoxLocalisationAttributesKeys.MARKER_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.comment"), ParadoxLocalisationAttributesKeys.COMMENT_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.scriptedVariable"), ParadoxLocalisationAttributesKeys.SCRIPTED_VARIABLE_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.number"), ParadoxLocalisationAttributesKeys.NUMBER_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.locale"), ParadoxLocalisationAttributesKeys.LOCALE_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.propertyKey"), ParadoxLocalisationAttributesKeys.PROPERTY_KEY_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.string"), ParadoxLocalisationAttributesKeys.STRING_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.propertyReference"), ParadoxLocalisationAttributesKeys.PROPERTY_REFERENCE_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.propertyReferenceParameter"), ParadoxLocalisationAttributesKeys.PROPERTY_REFERENCE_PARAMETER_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.icon"), ParadoxLocalisationAttributesKeys.ICON_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.commandScope"), ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.commandField"), ParadoxLocalisationAttributesKeys.COMMAND_FIELD_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.color"), ParadoxLocalisationAttributesKeys.COLOR_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.validEscape"), ParadoxLocalisationAttributesKeys.VALID_ESCAPE_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.invalidEscape"), ParadoxLocalisationAttributesKeys.INVALID_ESCAPE_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.badCharacter"), ParadoxLocalisationAttributesKeys.BAD_CHARACTER_KEY),
			
			//unused in localisation files
			AttributesDescriptor(PlsBundle.message("localisation.displayName.localisation"), ParadoxLocalisationAttributesKeys.LOCALISATION_KEY),
			AttributesDescriptor(PlsBundle.message("localisation.displayName.syncedLocalisation"), ParadoxLocalisationAttributesKeys.SYNCED_LOCALISATION_KEY),
			
			//for stellaris
			AttributesDescriptor(PlsBundle.message("localisation.displayName.stellarisNamePart"), ParadoxLocalisationAttributesKeys.STELLARIS_NAME_PART__KEY)
		)
	}
	
	override fun getHighlighter() =
		SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxLocalisationLanguage, null, null)
	
	override fun getAdditionalHighlightingTagToDescriptorMap() = null
	
	override fun getIcon() = PlsIcons.ParadoxLocalisationFile
	
	override fun getAttributeDescriptors() = attributesDescriptors
	
	override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
	
	override fun getDisplayName() = PlsBundle.message("options.localisation.displayName")
	
	override fun getDemoText() = PlsConstants.paradoxLocalisationColorSettingsDemoText
}
