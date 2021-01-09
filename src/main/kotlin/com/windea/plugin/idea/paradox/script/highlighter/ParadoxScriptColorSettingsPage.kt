package com.windea.plugin.idea.paradox.script.highlighter

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.colors.*
import com.intellij.openapi.options.colors.ColorDescriptor.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.message
import com.windea.plugin.idea.paradox.script.*

class ParadoxScriptColorSettingsPage : ColorSettingsPage {
	companion object {
		private val _separatorName = message("paradox.script.displayName.separator")
		private val _bracesName = message("paradox.script.displayName.braces")
		private val _variableName = message("paradox.script.displayName.variable")
		private val _propertyKeyName = message("paradox.script.displayName.propertyKey")
		private val _keywordName = message("paradox.script.displayName.keyword")
		private val _colorName = message("paradox.script.displayName.color")
		private val _numberName = message("paradox.script.displayName.number")
		private val _stringName = message("paradox.script.displayName.string")
		private val _codeName = message("paradox.script.displayName.code")
		private val _commentName = message("paradox.script.displayName.comment")
		private val _validEscapeName = message("paradox.script.displayName.validEscape")
		private val _invalidEscapeName = message("paradox.script.displayName.invalidEscape")
		private val _badCharacterName = message("paradox.script.displayName.badCharacter")
		
		private val attributesDescriptors = arrayOf(
			AttributesDescriptor(_separatorName, ParadoxScriptAttributesKeys.SEPARATOR_KEY),
			AttributesDescriptor(_bracesName, ParadoxScriptAttributesKeys.BRACES_KEY),
			AttributesDescriptor(_variableName, ParadoxScriptAttributesKeys.VARIABLE_KEY),
			AttributesDescriptor(_propertyKeyName, ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY),
			AttributesDescriptor(_keywordName, ParadoxScriptAttributesKeys.KEYWORD_KEY),
			AttributesDescriptor(_colorName,ParadoxScriptAttributesKeys.COLOR_KEY),
			AttributesDescriptor(_numberName,ParadoxScriptAttributesKeys. NUMBER_KEY),
			AttributesDescriptor(_stringName, ParadoxScriptAttributesKeys.STRING_KEY),
			AttributesDescriptor(_codeName, ParadoxScriptAttributesKeys.CODE_KEY),
			AttributesDescriptor(_commentName, ParadoxScriptAttributesKeys.COMMENT_KEY),
			AttributesDescriptor(_validEscapeName, ParadoxScriptAttributesKeys.VALID_ESCAPE_KEY),
			AttributesDescriptor(_invalidEscapeName, ParadoxScriptAttributesKeys.INVALID_ESCAPE_KEY),
			AttributesDescriptor(_badCharacterName, ParadoxScriptAttributesKeys.BAD_CHARACTER_KEY)
		)
	}

	override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxScriptLanguage, null, null)

	override fun getAdditionalHighlightingTagToDescriptorMap() = null

	override fun getIcon() = paradoxScriptFileIcon

	override fun getAttributeDescriptors() = attributesDescriptors

	override fun getColorDescriptors() = EMPTY_ARRAY

	override fun getDisplayName() = paradoxScriptName

	override fun getDemoText() = paradoxScriptSampleText
}
