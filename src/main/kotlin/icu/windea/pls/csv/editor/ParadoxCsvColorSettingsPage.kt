package icu.windea.pls.csv.editor

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.colors.*
import icu.windea.pls.*
import icu.windea.pls.csv.*
import icu.windea.pls.model.constants.*

class ParadoxCsvColorSettingsPage : ColorSettingsPage {
    private val _attributesDescriptors = arrayOf(
        AttributesDescriptor(PlsBundle.message("csv.displayName.separator"), ParadoxCsvAttributesKeys.SEPARATOR_KEY),
        AttributesDescriptor(PlsBundle.message("csv.displayName.keyword"), ParadoxCsvAttributesKeys.KEYWORD_KEY),
        AttributesDescriptor(PlsBundle.message("csv.displayName.comment"), ParadoxCsvAttributesKeys.COMMENT_KEY),
        AttributesDescriptor(PlsBundle.message("csv.displayName.header"), ParadoxCsvAttributesKeys.HEADER_KEY),
        AttributesDescriptor(PlsBundle.message("csv.displayName.number"), ParadoxCsvAttributesKeys.NUMBER_KEY),
        AttributesDescriptor(PlsBundle.message("csv.displayName.string"), ParadoxCsvAttributesKeys.STRING_KEY),
        AttributesDescriptor(PlsBundle.message("csv.displayName.validEscape"), ParadoxCsvAttributesKeys.VALID_ESCAPE_KEY),
        AttributesDescriptor(PlsBundle.message("csv.displayName.invalidEscape"), ParadoxCsvAttributesKeys.INVALID_ESCAPE_KEY),
        AttributesDescriptor(PlsBundle.message("csv.displayName.badCharacter"), ParadoxCsvAttributesKeys.BAD_CHARACTER_KEY),
    )

    private val _tagToDescriptorMap = mapOf(
        "HEADER" to ParadoxCsvAttributesKeys.HEADER_KEY,
        "KEYWORD" to ParadoxCsvAttributesKeys.KEYWORD_KEY,
        "NUMBER" to ParadoxCsvAttributesKeys.NUMBER_KEY,
    )

    override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxCsvLanguage, null, null)

    override fun getAdditionalHighlightingTagToDescriptorMap() = _tagToDescriptorMap

    override fun getIcon() = PlsIcons.FileTypes.ParadoxCsv

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = PlsBundle.message("language.name.csv")

    override fun getDemoText() = PlsStringConstants.paradoxCsvColorSettingsSample
}
