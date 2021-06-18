package icu.windea.pls.script.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.script.*

class ParadoxScriptCodeStylePanel(
	currentSettings: CodeStyleSettings,
	settings: CodeStyleSettings
) : TabbedLanguageCodeStylePanel(ParadoxScriptLanguage, currentSettings, settings) {
	override fun initTabs(settings: CodeStyleSettings) {
		addIndentOptionsTab(settings)
		addSpacesTab(settings)
		addTab(GenerationCodeStylePanel(settings, ParadoxScriptLanguage))
	}
}