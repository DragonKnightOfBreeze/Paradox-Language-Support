package icu.windea.pls.cwt.codeStyle

import com.intellij.application.options.GenerationCodeStylePanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.psi.codeStyle.CodeStyleSettings
import icu.windea.pls.cwt.CwtLanguage

class CwtCodeStylePanel(
    currentSettings: CodeStyleSettings,
    settings: CodeStyleSettings
) : TabbedLanguageCodeStylePanel(CwtLanguage, currentSettings, settings) {
    override fun initTabs(settings: CodeStyleSettings) {
        addIndentOptionsTab(settings)
        addSpacesTab(settings)
        addBlankLinesTab(settings)
        addTab(GenerationCodeStylePanel(settings, CwtLanguage))
    }
}
