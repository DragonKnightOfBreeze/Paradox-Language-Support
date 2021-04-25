package com.windea.plugin.idea.pls.cwt.formatter

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.script.*

class CwtCodeStyleSettingsProvider: CodeStyleSettingsProvider(){
	override fun createCustomSettings(settings: CodeStyleSettings) = CwtCodeStyleSettings(settings)
	
	override fun getConfigurableDisplayName() = cwtName
	
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