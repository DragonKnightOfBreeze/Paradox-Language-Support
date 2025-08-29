package icu.windea.pls.localisation.codeStyle

import com.intellij.application.options.GenerationCodeStylePanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.psi.codeStyle.CodeStyleSettings
import icu.windea.pls.localisation.ParadoxLocalisationLanguage

class ParadoxLocalisationCodeStylePanel(
    currentSettings: CodeStyleSettings,
    settings: CodeStyleSettings
) : TabbedLanguageCodeStylePanel(ParadoxLocalisationLanguage, currentSettings, settings) {
    override fun initTabs(settings: CodeStyleSettings) {
        addIndentOptionsTab(settings)
        addBlankLinesTab(settings)
        addTab(GenerationCodeStylePanel(settings, ParadoxLocalisationLanguage))
    }
}
