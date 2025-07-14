package icu.windea.pls.csv.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.csv.*

class ParadoxCsvCodeStylePanel(
    currentSettings: CodeStyleSettings,
    settings: CodeStyleSettings
) : TabbedLanguageCodeStylePanel(ParadoxCsvLanguage, currentSettings, settings) {
    override fun initTabs(settings: CodeStyleSettings) {
        addBlankLinesTab(settings)
        addTab(GenerationCodeStylePanel(settings, ParadoxCsvLanguage))
    }
}
