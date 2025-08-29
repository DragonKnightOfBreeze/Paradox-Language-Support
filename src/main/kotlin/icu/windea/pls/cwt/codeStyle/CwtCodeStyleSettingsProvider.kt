package icu.windea.pls.cwt.codeStyle

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import icu.windea.pls.cwt.CwtLanguage

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
