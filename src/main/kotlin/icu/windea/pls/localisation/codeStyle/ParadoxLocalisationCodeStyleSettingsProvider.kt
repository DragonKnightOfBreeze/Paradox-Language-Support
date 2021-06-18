package icu.windea.pls.localisation.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.*

class ParadoxLocalisationCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
	override fun createCustomSettings(settings: CodeStyleSettings) = ParadoxLocalisationCodeStyleSettings(settings)
	
	override fun getConfigurableDisplayName() = paradoxLocalisationName
	
	override fun createConfigurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
		return object : CodeStyleAbstractConfigurable(settings, modelSettings, configurableDisplayName) {
			override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
				return ParadoxLocalisationCodeStylePanel(currentSettings, settings)
			}
		}
	}
}