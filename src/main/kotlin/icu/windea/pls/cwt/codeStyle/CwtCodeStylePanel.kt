package icu.windea.pls.cwt.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.cwt.*

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
