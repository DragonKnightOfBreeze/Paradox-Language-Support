package icu.windea.pls.script.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.*
import icu.windea.pls.script.*

class ParadoxScriptCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
    override fun getLanguage() = ParadoxScriptLanguage

    override fun getConfigurableDisplayName() = PlsBundle.message("options.script.displayName")

    override fun createCustomSettings(settings: CodeStyleSettings) = ParadoxScriptCodeStyleSettings(settings)

    override fun createConfigurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
        return object : CodeStyleAbstractConfigurable(settings, modelSettings, configurableDisplayName) {
            override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
                return ParadoxScriptCodeStylePanel(currentSettings, settings)
            }
        }
    }
}

