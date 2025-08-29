package icu.windea.pls.csv.codeStyle

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import icu.windea.pls.csv.ParadoxCsvLanguage

class ParadoxCsvCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
    override fun getLanguage() = ParadoxCsvLanguage

    override fun createCustomSettings(settings: CodeStyleSettings) = ParadoxCsvCodeStyleSettings(settings)

    override fun createConfigurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
        return object : CodeStyleAbstractConfigurable(settings, modelSettings, configurableDisplayName) {
            override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
                return ParadoxCsvCodeStylePanel(currentSettings, settings)
            }
        }
    }
}
