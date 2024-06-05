package icu.windea.pls.cwt.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.codeStyle.CwtCodeStyleSettings as Settings

class CwtLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage() = CwtLanguage
    
    override fun getConfigurableDisplayName() = PlsBundle.message("options.cwt.displayName")
    
    override fun getCodeSample(settingsType: SettingsType) = PlsConstants.cwtCodeStyleSettingsSample
    
    override fun createCustomSettings(settings: CodeStyleSettings) = Settings(settings)
    
    //需要重载这个方法以显示indentOptions设置页面
    override fun getIndentOptionsEditor() = IndentOptionsEditor(this)
    
    override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
        indentOptions.INDENT_SIZE = 4
        indentOptions.CONTINUATION_INDENT_SIZE = 4
        indentOptions.KEEP_INDENTS_ON_EMPTY_LINES = false
        indentOptions.USE_TAB_CHARACTER = false
        commonSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
        commonSettings.LINE_COMMENT_ADD_SPACE = true
    }
    
    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        when(settingsType) {
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
            IndentOption.USE_TAB_CHARACTER.name
        )
    }
    
    private fun customizeSpacingSettings(consumer: CodeStyleSettingsCustomizable) {
        val spacesAroundOperatorsGroup = CodeStyleSettingsCustomizableOptions.getInstance().SPACES_AROUND_OPERATORS
        consumer.showCustomOption(Settings::class.java, Settings::SPACE_AROUND_OPTION_SEPARATOR.name, PlsBundle.message("cwt.codeStyleSettings.spacing.around.optionSeparator"), spacesAroundOperatorsGroup)
        consumer.showCustomOption(Settings::class.java, Settings::SPACE_AROUND_PROPERTY_SEPARATOR.name, PlsBundle.message("cwt.codeStyleSettings.spacing.around.propertySeparator"), spacesAroundOperatorsGroup)
        
        val spacesWithinGroup = CodeStyleSettingsCustomizableOptions.getInstance().SPACES_WITHIN
        consumer.showCustomOption(Settings::class.java, Settings::SPACE_WITHIN_BRACES.name, PlsBundle.message("cwt.codeStyleSettings.spacing.withIn.braces"), spacesWithinGroup)
    }
    
    private fun customizeBlankLinesSettings(consumer: CodeStyleSettingsCustomizable) {
        consumer.showStandardOptions(
            BlankLinesOption.KEEP_BLANK_LINES_IN_CODE.name
        )
    }
    
    private fun customizeCommenterSettings(consumer: CodeStyleSettingsCustomizable) {
        consumer.showStandardOptions(
            CommenterOption.LINE_COMMENT_AT_FIRST_COLUMN.name,
            CommenterOption.LINE_COMMENT_ADD_SPACE.name
        )
        val commentsGroup = CodeStyleSettingsCustomizableOptions.getInstance().WRAPPING_COMMENTS
        consumer.showCustomOption(Settings::class.java, Settings::OPTION_COMMENT_ADD_SPACE.name, PlsBundle.message("cwt.codeStyleSettings.commenter.optionComment.addSpace"), commentsGroup)
        consumer.showCustomOption(Settings::class.java, Settings::DOCUMENTATION_COMMENT_ADD_SPACE.name, PlsBundle.message("cwt.codeStyleSettings.commenter.documentationComment.addSpace"), commentsGroup)
    }
    
    class IndentOptionsEditor(
        provider: LanguageCodeStyleSettingsProvider
    ) : SmartIndentOptionsEditor(provider)
}