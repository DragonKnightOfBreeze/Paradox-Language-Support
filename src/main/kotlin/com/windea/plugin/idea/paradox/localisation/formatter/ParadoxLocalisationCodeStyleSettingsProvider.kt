package com.windea.plugin.idea.paradox.localisation.formatter

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.*
import com.windea.plugin.idea.paradox.localisation.formatter.*

class ParadoxLocalisationCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
	override fun createCustomSettings(settings: CodeStyleSettings) = ParadoxLocalisationCodeStyleSettings(settings)

	override fun getConfigurableDisplayName() = paradoxLocalisationName

	override fun createConfigurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
		return object : CodeStyleAbstractConfigurable(settings, modelSettings, configurableDisplayName) {
			override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
				return object : TabbedLanguageCodeStylePanel(ParadoxLocalisationLanguage, currentSettings, settings) {
					override fun initTabs(settings: CodeStyleSettings?) {
						addIndentOptionsTab(settings)
						//DELAY 不清楚是否允许这种语法
						//addWrappingAndBracesTab(settings)
					}
				}
			}
		}
	}
}

