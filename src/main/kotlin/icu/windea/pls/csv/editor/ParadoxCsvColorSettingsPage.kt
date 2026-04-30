package icu.windea.pls.csv.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.model.constants.PlsPreviewTexts

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

        AttributesDescriptor(PlsBundle.message("csv.displayName.definitionReference"), ParadoxCsvAttributesKeys.DEFINITION_REFERENCE_KEY),
        AttributesDescriptor(PlsBundle.message("csv.displayName.enumValue"), ParadoxCsvAttributesKeys.ENUM_VALUE_KEY),
        AttributesDescriptor(PlsBundle.message("csv.displayName.complexEnumValue"), ParadoxCsvAttributesKeys.COMPLEX_ENUM_VALUE_KEY),
    )

    private val _tagToDescriptorMap = mapOf(
        "KEYWORD" to ParadoxCsvAttributesKeys.KEYWORD_KEY,
        "HEADER" to ParadoxCsvAttributesKeys.HEADER_KEY,
        "NUMBER" to ParadoxCsvAttributesKeys.NUMBER_KEY,
        "DR" to ParadoxCsvAttributesKeys.DEFINITION_REFERENCE_KEY,
        "EV" to ParadoxCsvAttributesKeys.ENUM_VALUE_KEY,
        "CEV" to ParadoxCsvAttributesKeys.COMPLEX_ENUM_VALUE_KEY,
    )

    override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxCsvLanguage, null, null)

    override fun getAdditionalHighlightingTagToDescriptorMap() = _tagToDescriptorMap

    override fun getIcon() = PlsIcons.FileTypes.ParadoxCsv

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = PlsBundle.message("csv.settings.name")

    override fun getDemoText() = PlsPreviewTexts.csvColorSettings
}
