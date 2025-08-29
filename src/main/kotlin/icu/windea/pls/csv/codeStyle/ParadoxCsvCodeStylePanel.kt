package icu.windea.pls.csv.codeStyle

import com.intellij.application.options.GenerationCodeStylePanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.psi.codeStyle.CodeStyleSettings
import icu.windea.pls.csv.ParadoxCsvLanguage

class ParadoxCsvCodeStylePanel(
    currentSettings: CodeStyleSettings,
    settings: CodeStyleSettings
) : TabbedLanguageCodeStylePanel(ParadoxCsvLanguage, currentSettings, settings) {
    override fun initTabs(settings: CodeStyleSettings) {
        addBlankLinesTab(settings)
        addTab(GenerationCodeStylePanel(settings, ParadoxCsvLanguage))
    }
}
