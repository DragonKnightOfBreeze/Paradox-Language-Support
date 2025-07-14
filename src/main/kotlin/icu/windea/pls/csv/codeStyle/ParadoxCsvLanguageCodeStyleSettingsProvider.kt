package icu.windea.pls.csv.codeStyle

import com.intellij.psi.codeStyle.*
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.*
import icu.windea.pls.model.constants.*

class ParadoxCsvLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage() = ParadoxCsvLanguage

    override fun getCodeSample(settingsType: SettingsType) = PlsStringConstants.paradoxCsvCodeStyleSettingsSample

    override fun createCustomSettings(settings: CodeStyleSettings) = ParadoxCsvCodeStyleSettings(settings)

    override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
        commonSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
        commonSettings.LINE_COMMENT_ADD_SPACE = true
    }

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        when (settingsType) {
            SettingsType.BLANK_LINES_SETTINGS -> customizeBlankLinesSettings(consumer)
            SettingsType.COMMENTER_SETTINGS -> customizeCommenterSettings(consumer)
            else -> pass()
        }
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
}
