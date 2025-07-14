package icu.windea.pls.csv.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.csv.*

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
