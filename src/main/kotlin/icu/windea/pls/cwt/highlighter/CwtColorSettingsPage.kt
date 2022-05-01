package icu.windea.pls.cwt.highlighter

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.colors.*
import icu.windea.pls.*
import icu.windea.pls.cwt.*

private val attributesDescriptors = arrayOf(
	AttributesDescriptor(PlsBundle.message("cwt.displayName.separator"), CwtAttributesKeys.SEPARATOR_KEY),
	AttributesDescriptor(PlsBundle.message("cwt.displayName.braces"), CwtAttributesKeys.BRACES_KEY),
	AttributesDescriptor(PlsBundle.message("cwt.displayName.propertyKey"), CwtAttributesKeys.PROPERTY_KEY_KEY),
	AttributesDescriptor(PlsBundle.message("cwt.displayName.optionKey"), CwtAttributesKeys.OPTION_KEY_KEY),
	AttributesDescriptor(PlsBundle.message("cwt.displayName.keyword"), CwtAttributesKeys.KEYWORD_KEY),
	AttributesDescriptor(PlsBundle.message("cwt.displayName.number"), CwtAttributesKeys.NUMBER_KEY),
	AttributesDescriptor(PlsBundle.message("cwt.displayName.string"), CwtAttributesKeys.STRING_KEY),
	AttributesDescriptor(PlsBundle.message("cwt.displayName.comment"), CwtAttributesKeys.COMMENT_KEY),
	AttributesDescriptor(PlsBundle.message("cwt.displayName.optionComment"), CwtAttributesKeys.OPTION_COMMENT_KEY),
	AttributesDescriptor(PlsBundle.message("cwt.displayName.documentationComment"), CwtAttributesKeys.DOCUMENTATION_COMMENT_KEY),
	AttributesDescriptor(PlsBundle.message("cwt.displayName.validEscape"), CwtAttributesKeys.VALID_ESCAPE_KEY),
	AttributesDescriptor(PlsBundle.message("cwt.displayName.invalidEscape"), CwtAttributesKeys.INVALID_ESCAPE_KEY),
	AttributesDescriptor(PlsBundle.message("cwt.displayName.badCharacter"), CwtAttributesKeys.BAD_CHARACTER_KEY)
)

class CwtColorSettingsPage : ColorSettingsPage {
	override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(CwtLanguage, null, null)
	
	override fun getAdditionalHighlightingTagToDescriptorMap() = null
	
	override fun getIcon() = PlsIcons.cwtFileIcon
	
	override fun getAttributeDescriptors() = attributesDescriptors
	
	override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
	
	override fun getDisplayName() = cwtName
	
	override fun getDemoText() = cwtDemoText
}