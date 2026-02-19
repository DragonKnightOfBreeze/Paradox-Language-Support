package icu.windea.pls.csv.formatter

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.*
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import icu.windea.pls.core.pass
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.model.constants.PlsPreviewTexts

class ParadoxCsvCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage() = ParadoxCsvLanguage

    override fun createCustomSettings(settings: CodeStyleSettings) = ParadoxCsvCodeStyleSettings(settings)

    override fun createConfigurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
        return object : CodeStyleAbstractConfigurable(settings, modelSettings, configurableDisplayName) {
            override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
                return ParadoxCsvCodeStylePanel(currentSettings, settings)
            }
        }
    }

    override fun getCodeSample(settingsType: SettingsType) = PlsPreviewTexts.csvCodeStyleSettings

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
