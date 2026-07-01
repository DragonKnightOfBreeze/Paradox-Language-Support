package icu.windea.pls.cwt.editor

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
        AttributesDescriptor(ChronicleBundle.message("cwt.displayName.braces"), CwtHighlighterColors.BRACES),
        AttributesDescriptor(ChronicleBundle.message("cwt.displayName.operator"), CwtHighlighterColors.OPERATOR),
        AttributesDescriptor(ChronicleBundle.message("cwt.displayName.comment"), CwtHighlighterColors.COMMENT),
        AttributesDescriptor(ChronicleBundle.message("cwt.displayName.optionComment"), CwtHighlighterColors.OPTION_COMMENT),
        AttributesDescriptor(ChronicleBundle.message("cwt.displayName.documentationComment"), CwtHighlighterColors.DOC_COMMENT),
        AttributesDescriptor(ChronicleBundle.message("cwt.displayName.keyword"), CwtHighlighterColors.KEYWORD),
        AttributesDescriptor(ChronicleBundle.message("cwt.displayName.propertyKey"), CwtHighlighterColors.PROPERTY_KEY),
        AttributesDescriptor(ChronicleBundle.message("cwt.displayName.optionKey"), CwtHighlighterColors.OPTION_KEY),
        AttributesDescriptor(ChronicleBundle.message("cwt.displayName.number"), CwtHighlighterColors.NUMBER),
        AttributesDescriptor(ChronicleBundle.message("cwt.displayName.string"), CwtHighlighterColors.STRING),
        AttributesDescriptor(ChronicleBundle.message("cwt.displayName.validEscape"), CwtHighlighterColors.VALID_ESCAPE),
        AttributesDescriptor(ChronicleBundle.message("cwt.displayName.invalidEscape"), CwtHighlighterColors.INVALID_ESCAPE),
        AttributesDescriptor(ChronicleBundle.message("cwt.displayName.badCharacter"), CwtHighlighterColors.BAD_CHARACTER)
    )

    override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(CwtLanguage, null, null)

    override fun getAdditionalHighlightingTagToDescriptorMap() = null

    override fun getIcon() = ChronicleIcons.FileTypes.Cwt

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = ChronicleBundle.message("cwt.settings.name")

    override fun getDemoText() = CwtConstants.colorSettingsText
}
