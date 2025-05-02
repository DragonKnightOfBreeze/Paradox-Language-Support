package icu.windea.pls.cwt.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.cwt.*

class CwtCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
    override fun getLanguage() = CwtLanguage

    override fun createCustomSettings(settings: CodeStyleSettings) = CwtCodeStyleSettings(settings)

    override fun createConfigurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
        return object : CodeStyleAbstractConfigurable(settings, modelSettings, configurableDisplayName) {
            override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
                return CwtCodeStylePanel(currentSettings, settings)
            }
        }
    }
}
