package icu.windea.pls.cwt.highlighter

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.colors.*
import icu.windea.pls.*
import icu.windea.pls.cwt.*

class CwtColorSettingsPage:ColorSettingsPage {
	companion object{
		private val _separatorName = message("cwt.displayName.separator")
		private val _bracesName = message("cwt.displayName.braces")
		private val _propertyKeyName = message("cwt.displayName.propertyKey")
		private val _optionKeyName = message("cwt.displayName.optionKey")
		private val _keywordName = message("cwt.displayName.keyword")
		private val _numberName = message("cwt.displayName.number")
		private val _stringName = message("cwt.displayName.string")
		private val _commentName = message("cwt.displayName.comment")
		private val _optionCommentName = message("cwt.displayName.optionComment")
		private val _documentationCommentName = message("cwt.displayName.documentationComment")
		private val _validEscapeName = message("cwt.displayName.validEscape")
		private val _invalidEscapeName = message("cwt.displayName.invalidEscape")
		private val _badCharacterName = message("cwt.displayName.badCharacter")
	}
	
	private val attributersDescriptors = arrayOf(
		AttributesDescriptor(_separatorName,CwtAttributeKeys.SEPARATOR_KEY),
		AttributesDescriptor(_bracesName,CwtAttributeKeys.BRACES_KEY),
		AttributesDescriptor(_propertyKeyName,CwtAttributeKeys.PROPERTY_KEY_KEY),
		AttributesDescriptor(_optionKeyName,CwtAttributeKeys.OPTION_KEY_KEY),
		AttributesDescriptor(_keywordName,CwtAttributeKeys.KEYWORD_KEY),
		AttributesDescriptor(_numberName,CwtAttributeKeys.NUMBER_KEY),
		AttributesDescriptor(_stringName,CwtAttributeKeys.STRING_KEY),
		AttributesDescriptor(_commentName,CwtAttributeKeys.COMMENT_KEY),
		AttributesDescriptor(_optionCommentName,CwtAttributeKeys.OPTION_COMMENT_KEY),
		AttributesDescriptor(_documentationCommentName,CwtAttributeKeys.DOCUMENTATION_COMMENT_KEY),
		AttributesDescriptor(_validEscapeName,CwtAttributeKeys.VALID_ESCAPE_KEY),
		AttributesDescriptor(_invalidEscapeName,CwtAttributeKeys.INVALID_ESCAPE_KEY),
		AttributesDescriptor(_badCharacterName,CwtAttributeKeys.BAD_CHARACTER_KEY)
	)
	
	override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(CwtLanguage,null,null)
	
	override fun getAdditionalHighlightingTagToDescriptorMap() = null
	
	override fun getIcon() = cwtFileIcon
	
	override fun getAttributeDescriptors() = attributersDescriptors
	
	override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
	
	override fun getDisplayName() = cwtName
	
	override fun getDemoText() = cwtDemoText
}