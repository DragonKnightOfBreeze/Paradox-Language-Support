package icu.windea.pls.localisation.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.localisation.*

class ParadoxLocalisationCodeStylePanel(
	currentSettings: CodeStyleSettings,
	settings: CodeStyleSettings
) : TabbedLanguageCodeStylePanel(ParadoxLocalisationLanguage, currentSettings, settings) {
	override fun initTabs(settings: CodeStyleSettings) {
		addIndentOptionsTab(settings)
		addTab(GenerationCodeStylePanel(settings, ParadoxLocalisationLanguage))
	}
}