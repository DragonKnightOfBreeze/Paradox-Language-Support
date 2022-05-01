package icu.windea.pls.cwt.highlighter

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.colors.*
import icu.windea.pls.*
import icu.windea.pls.cwt.*

class CwtColorSettingsPage : ColorSettingsPage {
	companion object {
		private val _separatorName = PlsBundle.message("cwt.displayName.separator")
		private val _bracesName = PlsBundle.message("cwt.displayName.braces")
		private val _propertyKeyName = PlsBundle.message("cwt.displayName.propertyKey")
		private val _optionKeyName = PlsBundle.message("cwt.displayName.optionKey")
		private val _keywordName = PlsBundle.message("cwt.displayName.keyword")
		private val _numberName = PlsBundle.message("cwt.displayName.number")
		private val _stringName = PlsBundle.message("cwt.displayName.string")
		private val _commentName = PlsBundle.message("cwt.displayName.comment")
		private val _optionCommentName = PlsBundle.message("cwt.displayName.optionComment")
		private val _documentationCommentName = PlsBundle.message("cwt.displayName.documentationComment")
		private val _validEscapeName = PlsBundle.message("cwt.displayName.validEscape")
		private val _invalidEscapeName = PlsBundle.message("cwt.displayName.invalidEscape")
		private val _badCharacterName = PlsBundle.message("cwt.displayName.badCharacter")
	}
	
	private val attributesDescriptors = arrayOf(
		AttributesDescriptor(_separatorName, CwtAttributesKeys.SEPARATOR_KEY),
		AttributesDescriptor(_bracesName, CwtAttributesKeys.BRACES_KEY),
		AttributesDescriptor(_propertyKeyName, CwtAttributesKeys.PROPERTY_KEY_KEY),
		AttributesDescriptor(_optionKeyName, CwtAttributesKeys.OPTION_KEY_KEY),
		AttributesDescriptor(_keywordName, CwtAttributesKeys.KEYWORD_KEY),
		AttributesDescriptor(_numberName, CwtAttributesKeys.NUMBER_KEY),
		AttributesDescriptor(_stringName, CwtAttributesKeys.STRING_KEY),
		AttributesDescriptor(_commentName, CwtAttributesKeys.COMMENT_KEY),
		AttributesDescriptor(_optionCommentName, CwtAttributesKeys.OPTION_COMMENT_KEY),
		AttributesDescriptor(_documentationCommentName, CwtAttributesKeys.DOCUMENTATION_COMMENT_KEY),
		AttributesDescriptor(_validEscapeName, CwtAttributesKeys.VALID_ESCAPE_KEY),
		AttributesDescriptor(_invalidEscapeName, CwtAttributesKeys.INVALID_ESCAPE_KEY),
		AttributesDescriptor(_badCharacterName, CwtAttributesKeys.BAD_CHARACTER_KEY)
	)
	
	override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(CwtLanguage, null, null)
	
	override fun getAdditionalHighlightingTagToDescriptorMap() = null
	
	override fun getIcon() = PlsIcons.cwtFileIcon
	
	override fun getAttributeDescriptors() = attributesDescriptors
	
	override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
	
	override fun getDisplayName() = cwtName
	
	override fun getDemoText() = cwtDemoText
}