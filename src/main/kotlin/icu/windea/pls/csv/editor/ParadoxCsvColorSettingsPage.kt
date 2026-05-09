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
        AttributesDescriptor(PlsBundle.message("csv.displayName.separator"), ParadoxCsvAttributesKeys.SEPARATOR),
        AttributesDescriptor(PlsBundle.message("csv.displayName.keyword"), ParadoxCsvAttributesKeys.KEYWORD),
        AttributesDescriptor(PlsBundle.message("csv.displayName.comment"), ParadoxCsvAttributesKeys.COMMENT),
        AttributesDescriptor(PlsBundle.message("csv.displayName.header"), ParadoxCsvAttributesKeys.HEADER),
        AttributesDescriptor(PlsBundle.message("csv.displayName.number"), ParadoxCsvAttributesKeys.NUMBER),
        AttributesDescriptor(PlsBundle.message("csv.displayName.string"), ParadoxCsvAttributesKeys.STRING),
        AttributesDescriptor(PlsBundle.message("csv.displayName.validEscape"), ParadoxCsvAttributesKeys.VALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("csv.displayName.invalidEscape"), ParadoxCsvAttributesKeys.INVALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("csv.displayName.badCharacter"), ParadoxCsvAttributesKeys.BAD_CHARACTER),

        AttributesDescriptor(PlsBundle.message("csv.displayName.definitionReference"), ParadoxCsvAttributesKeys.DEFINITION_REFERENCE),
        AttributesDescriptor(PlsBundle.message("csv.displayName.enumValue"), ParadoxCsvAttributesKeys.ENUM_VALUE),
        AttributesDescriptor(PlsBundle.message("csv.displayName.complexEnumValue"), ParadoxCsvAttributesKeys.COMPLEX_ENUM_VALUE),
    )

    private val _tagToDescriptorMap = mapOf(
        "KEYWORD" to ParadoxCsvAttributesKeys.KEYWORD,
        "HEADER" to ParadoxCsvAttributesKeys.HEADER,
        "NUMBER" to ParadoxCsvAttributesKeys.NUMBER,
        "DR" to ParadoxCsvAttributesKeys.DEFINITION_REFERENCE,
        "EV" to ParadoxCsvAttributesKeys.ENUM_VALUE,
        "CEV" to ParadoxCsvAttributesKeys.COMPLEX_ENUM_VALUE,
    )

    override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxCsvLanguage, null, null)

    override fun getAdditionalHighlightingTagToDescriptorMap() = _tagToDescriptorMap

    override fun getIcon() = PlsIcons.FileTypes.ParadoxCsv

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = PlsBundle.message("csv.settings.name")

    override fun getDemoText() = PlsPreviewTexts.csvColorSettings
}
