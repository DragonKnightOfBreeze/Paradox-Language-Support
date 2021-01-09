package com.windea.plugin.idea.paradox.script.formatter

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.*
import com.windea.plugin.idea.paradox.script.formatter.*

class ParadoxScriptCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
	override fun createCustomSettings(settings: CodeStyleSettings) = ParadoxScriptCodeStyleSettings(settings)

	override fun getConfigurableDisplayName() = paradoxScriptName

	override fun createConfigurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
		return object : CodeStyleAbstractConfigurable(settings, modelSettings, configurableDisplayName) {
			override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
				return object : TabbedLanguageCodeStylePanel(ParadoxScriptLanguage, currentSettings, settings) {
					override fun initTabs(settings: CodeStyleSettings?) {
						addIndentOptionsTab(settings)
						addSpacesTab(settings)
						//addWrappingAndBracesTab(settings)
					}
				}
			}
		}
	}
}
