package icu.windea.pls.cwt.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.cwt.CwtConstants
import icu.windea.pls.cwt.CwtLanguage

class CwtColorSettingsPage : ColorSettingsPage {
    private val _attributesDescriptors = arrayOf(
        AttributesDescriptor(PlsBundle.message("cwt.displayName.braces"), CwtHighlighterColors.BRACES),
        AttributesDescriptor(PlsBundle.message("cwt.displayName.operator"), CwtHighlighterColors.OPERATOR),
        AttributesDescriptor(PlsBundle.message("cwt.displayName.comment"), CwtHighlighterColors.COMMENT),
        AttributesDescriptor(PlsBundle.message("cwt.displayName.optionComment"), CwtHighlighterColors.OPTION_COMMENT),
        AttributesDescriptor(PlsBundle.message("cwt.displayName.documentationComment"), CwtHighlighterColors.DOC_COMMENT),
        AttributesDescriptor(PlsBundle.message("cwt.displayName.keyword"), CwtHighlighterColors.KEYWORD),
        AttributesDescriptor(PlsBundle.message("cwt.displayName.propertyKey"), CwtHighlighterColors.PROPERTY_KEY),
        AttributesDescriptor(PlsBundle.message("cwt.displayName.optionKey"), CwtHighlighterColors.OPTION_KEY),
        AttributesDescriptor(PlsBundle.message("cwt.displayName.number"), CwtHighlighterColors.NUMBER),
        AttributesDescriptor(PlsBundle.message("cwt.displayName.string"), CwtHighlighterColors.STRING),
        AttributesDescriptor(PlsBundle.message("cwt.displayName.validEscape"), CwtHighlighterColors.VALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("cwt.displayName.invalidEscape"), CwtHighlighterColors.INVALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("cwt.displayName.badCharacter"), CwtHighlighterColors.BAD_CHARACTER)
    )

    override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(CwtLanguage, null, null)

    override fun getAdditionalHighlightingTagToDescriptorMap() = null

    override fun getIcon() = PlsIcons.FileTypes.Cwt

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = PlsBundle.message("cwt.settings.name")

    override fun getDemoText() = CwtConstants.colorSettingsText
}
