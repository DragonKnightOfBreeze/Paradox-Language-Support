package icu.windea.pls.cwt.formatter

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.*
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.pass
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.model.constants.PlsPreviewTexts
import kotlin.reflect.KMutableProperty1

class CwtCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage() = CwtLanguage

    override fun createCustomSettings(settings: CodeStyleSettings) = CwtCodeStyleSettings(settings)

    override fun createConfigurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
        return object : CodeStyleAbstractConfigurable(settings, modelSettings, configurableDisplayName) {
            override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
                return CwtCodeStylePanel(currentSettings, settings)
            }
        }
    }

    override fun getCodeSample(settingsType: SettingsType) = PlsPreviewTexts.cwtCodeStyleSettings

    override fun getIndentOptionsEditor(): IndentOptionsEditor {
        // 需要重载这个方法以显示 indentOptions 设置页面
        return IndentOptionsEditor(this)
    }

    override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
        indentOptions.INDENT_SIZE = 4
        indentOptions.CONTINUATION_INDENT_SIZE = 4
        indentOptions.KEEP_INDENTS_ON_EMPTY_LINES = false
        indentOptions.USE_TAB_CHARACTER = false
        commonSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
        commonSettings.LINE_COMMENT_ADD_SPACE = true
    }

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        when (settingsType) {
            SettingsType.INDENT_SETTINGS -> customizeIndentSettings(consumer)
            SettingsType.SPACING_SETTINGS -> customizeSpacingSettings(consumer)
            SettingsType.BLANK_LINES_SETTINGS -> customizeBlankLinesSettings(consumer)
            SettingsType.COMMENTER_SETTINGS -> customizeCommenterSettings(consumer)
            else -> pass()
        }
    }

    private fun customizeIndentSettings(consumer: CodeStyleSettingsCustomizable) {
        consumer.showStandardOptions(
            IndentOption.INDENT_SIZE.name,
            IndentOption.CONTINUATION_INDENT_SIZE.name,
            IndentOption.KEEP_INDENTS_ON_EMPTY_LINES.name,
            IndentOption.USE_TAB_CHARACTER.name,
        )
    }

    private fun customizeSpacingSettings(consumer: CodeStyleSettingsCustomizable) {
        val spacesAroundOperatorsGroup = CodeStyleSettingsCustomizableOptions.getInstance().SPACES_AROUND_OPERATORS
        consumer.showCustomOption(CwtCodeStyleSettings::SPACE_AROUND_OPTION_SEPARATOR, PlsBundle.message("cwt.codeStyleSettings.spacing.around.optionSeparator"), spacesAroundOperatorsGroup)
        consumer.showCustomOption(CwtCodeStyleSettings::SPACE_AROUND_PROPERTY_SEPARATOR, PlsBundle.message("cwt.codeStyleSettings.spacing.around.propertySeparator"), spacesAroundOperatorsGroup)

        val spacesWithinGroup = CodeStyleSettingsCustomizableOptions.getInstance().SPACES_WITHIN
        consumer.showCustomOption(CwtCodeStyleSettings::SPACE_WITHIN_BRACES, PlsBundle.message("cwt.codeStyleSettings.spacing.withIn.braces"), spacesWithinGroup)
        consumer.showCustomOption(CwtCodeStyleSettings::SPACE_WITHIN_EMPTY_BRACES, PlsBundle.message("cwt.codeStyleSettings.spacing.withIn.emptyBraces"), spacesWithinGroup)
    }

    private fun customizeBlankLinesSettings(consumer: CodeStyleSettingsCustomizable) {
        consumer.showStandardOptions(
            BlankLinesOption.KEEP_BLANK_LINES_IN_CODE.name
        )
    }

    private fun customizeCommenterSettings(consumer: CodeStyleSettingsCustomizable) {
        consumer.showStandardOptions(
            CommenterOption.LINE_COMMENT_AT_FIRST_COLUMN.name,
            CommenterOption.LINE_COMMENT_ADD_SPACE.name,
        )
        val commentsGroup = CodeStyleSettingsCustomizableOptions.getInstance().WRAPPING_COMMENTS
        consumer.showCustomOption(CwtCodeStyleSettings::OPTION_COMMENT_ADD_SPACE, PlsBundle.message("cwt.codeStyleSettings.commenter.optionComment.addSpace"), commentsGroup)
        consumer.showCustomOption(CwtCodeStyleSettings::DOC_COMMENT_ADD_SPACE, PlsBundle.message("cwt.codeStyleSettings.commenter.documentationComment.addSpace"), commentsGroup)
    }

    fun CodeStyleSettingsCustomizable.showCustomOption(property: KMutableProperty1<CwtCodeStyleSettings, Boolean>, title: @NlsContexts.Label String, groupName: @NlsContexts.Label String?) {
        showCustomOption(CwtCodeStyleSettings::class.java, property.name, title, groupName)
    }

    class IndentOptionsEditor(
        provider: LanguageCodeStyleSettingsProvider
    ) : SmartIndentOptionsEditor(provider)
}
