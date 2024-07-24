package icu.windea.pls.localisation.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.*

class ParadoxLocalisationLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage() = ParadoxLocalisationLanguage
    
    override fun getConfigurableDisplayName() = PlsBundle.message("options.localisation.displayName")
    
    override fun getCodeSample(settingsType: SettingsType) = PlsConstants.Samples.paradoxLocalisationCodeStyleSettings
    
    override fun createCustomSettings(settings: CodeStyleSettings) = ParadoxLocalisationCodeStyleSettings(settings)
    
    //需要重载这个方法以显示indentOptions设置页面
    override fun getIndentOptionsEditor() = IndentOptionsEditor(this)
    
    override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
        indentOptions.INDENT_SIZE = 1
        indentOptions.KEEP_INDENTS_ON_EMPTY_LINES = false
        commonSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
        commonSettings.LINE_COMMENT_ADD_SPACE = true
    }
    
    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        when(settingsType) {
            SettingsType.INDENT_SETTINGS -> customizeIndentSettings(consumer)
            SettingsType.BLANK_LINES_SETTINGS -> customizeBlankLinesSettings(consumer)
            SettingsType.COMMENTER_SETTINGS -> customizeCommenterSettings(consumer)
            else -> pass()
        }
    }
    
    private fun customizeIndentSettings(consumer: CodeStyleSettingsCustomizable) {
        consumer.showStandardOptions(
            IndentOption.INDENT_SIZE.name,
            IndentOption.KEEP_INDENTS_ON_EMPTY_LINES.name
        )
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
    }
    
    class IndentOptionsEditor(
        provider: LanguageCodeStyleSettingsProvider
    ) : SmartIndentOptionsEditor(provider)
}
