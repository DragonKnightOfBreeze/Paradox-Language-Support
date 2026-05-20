package icu.windea.pls.csv.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.csv.ParadoxCsvConstants
import icu.windea.pls.csv.ParadoxCsvLanguage

class ParadoxCsvColorSettingsPage : ColorSettingsPage {
    private val _attributesDescriptors = arrayOf(
        AttributesDescriptor(PlsBundle.message("csv.displayName.separator"), ParadoxCsvHighlighterColors.SEPARATOR),
        AttributesDescriptor(PlsBundle.message("csv.displayName.keyword"), ParadoxCsvHighlighterColors.KEYWORD),
        AttributesDescriptor(PlsBundle.message("csv.displayName.comment"), ParadoxCsvHighlighterColors.COMMENT),
        AttributesDescriptor(PlsBundle.message("csv.displayName.header"), ParadoxCsvHighlighterColors.HEADER),
        AttributesDescriptor(PlsBundle.message("csv.displayName.number"), ParadoxCsvHighlighterColors.NUMBER),
        AttributesDescriptor(PlsBundle.message("csv.displayName.string"), ParadoxCsvHighlighterColors.STRING),
        AttributesDescriptor(PlsBundle.message("csv.displayName.validEscape"), ParadoxCsvHighlighterColors.VALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("csv.displayName.invalidEscape"), ParadoxCsvHighlighterColors.INVALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("csv.displayName.badCharacter"), ParadoxCsvHighlighterColors.BAD_CHARACTER),

        AttributesDescriptor(PlsBundle.message("semantic.displayName.definitionReference"), ParadoxCsvHighlighterColors.DEFINITION_REFERENCE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.enumValue"), ParadoxCsvHighlighterColors.ENUM_VALUE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.complexEnumValue"), ParadoxCsvHighlighterColors.COMPLEX_ENUM_VALUE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.dynamicValue"), ParadoxCsvHighlighterColors.DYNAMIC_VALUE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.variable"), ParadoxCsvHighlighterColors.VARIABLE),
    )

    private val _tagToDescriptorMap = mapOf(
        "KEYWORD" to ParadoxCsvHighlighterColors.KEYWORD,
        "HEADER" to ParadoxCsvHighlighterColors.HEADER,
        "NUMBER" to ParadoxCsvHighlighterColors.NUMBER,

        "DEFINITION_REFERENCE" to ParadoxCsvHighlighterColors.DEFINITION_REFERENCE,
        "ENUM_VALUE" to ParadoxCsvHighlighterColors.ENUM_VALUE,
        "COMPLEX_ENUM_VALUE" to ParadoxCsvHighlighterColors.COMPLEX_ENUM_VALUE,
        "DYNAMIC_VALUE" to ParadoxCsvHighlighterColors.DYNAMIC_VALUE,
        "VARIABLE" to ParadoxCsvHighlighterColors.VARIABLE,
    )

    override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxCsvLanguage, null, null)

    override fun getAdditionalHighlightingTagToDescriptorMap() = _tagToDescriptorMap

    override fun getIcon() = PlsIcons.FileTypes.ParadoxCsv

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = PlsBundle.message("csv.settings.name")

    override fun getDemoText() = ParadoxCsvConstants.colorSettingsText
}
