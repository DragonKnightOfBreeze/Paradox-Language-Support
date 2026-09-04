package icu.windea.pls.cwt.highlighting

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.cwt.CwtConstants
import icu.windea.pls.cwt.CwtLanguage

class CwtColorSettingsPage : ColorSettingsPage {
    private val _attributesDescriptors = arrayOf(
        AttributesDescriptor(ChronicleBundle.message("cwt.color.option.braces"), CwtHighlighterColors.BRACES),
        AttributesDescriptor(ChronicleBundle.message("cwt.color.option.operator"), CwtHighlighterColors.OPERATOR),
        AttributesDescriptor(ChronicleBundle.message("cwt.color.option.comment"), CwtHighlighterColors.COMMENT),
        AttributesDescriptor(ChronicleBundle.message("cwt.color.option.optionComment"), CwtHighlighterColors.OPTION_COMMENT),
        AttributesDescriptor(ChronicleBundle.message("cwt.color.option.documentationComment"), CwtHighlighterColors.DOC_COMMENT),
        AttributesDescriptor(ChronicleBundle.message("cwt.color.option.keyword"), CwtHighlighterColors.KEYWORD),
        AttributesDescriptor(ChronicleBundle.message("cwt.color.option.propertyKey"), CwtHighlighterColors.PROPERTY_KEY),
        AttributesDescriptor(ChronicleBundle.message("cwt.color.option.optionKey"), CwtHighlighterColors.OPTION_KEY),
        AttributesDescriptor(ChronicleBundle.message("cwt.color.option.number"), CwtHighlighterColors.NUMBER),
        AttributesDescriptor(ChronicleBundle.message("cwt.color.option.string"), CwtHighlighterColors.STRING),
        AttributesDescriptor(ChronicleBundle.message("cwt.color.option.validEscape"), CwtHighlighterColors.VALID_ESCAPE),
        AttributesDescriptor(ChronicleBundle.message("cwt.color.option.invalidEscape"), CwtHighlighterColors.INVALID_ESCAPE),
        AttributesDescriptor(ChronicleBundle.message("cwt.color.option.badCharacter"), CwtHighlighterColors.BAD_CHARACTER)
    )

    override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(CwtLanguage, null, null)

    override fun getAdditionalHighlightingTagToDescriptorMap() = null

    override fun getIcon() = ChronicleIcons.FileTypes.Cwt

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = ChronicleBundle.message("cwt.settings.name")

    override fun getDemoText() = CwtConstants.colorSettingsText
}
