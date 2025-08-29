package icu.windea.pls.script.codeStyle

import com.intellij.application.options.GenerationCodeStylePanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.psi.codeStyle.CodeStyleSettings
import icu.windea.pls.script.ParadoxScriptLanguage

class ParadoxScriptCodeStylePanel(
    currentSettings: CodeStyleSettings,
    settings: CodeStyleSettings
) : TabbedLanguageCodeStylePanel(ParadoxScriptLanguage, currentSettings, settings) {
    override fun initTabs(settings: CodeStyleSettings) {
        addIndentOptionsTab(settings)
        addSpacesTab(settings)
        addBlankLinesTab(settings)
        addTab(GenerationCodeStylePanel(settings, ParadoxScriptLanguage))
    }
}
