package icu.windea.pls.localisation.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*

class ParadoxLocalisationCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
	override fun getLanguage() = ParadoxLocalisationLanguage
	
	override fun getConfigurableDisplayName() = PlsBundle.message("options.localisation.displayName")
	
	override fun createCustomSettings(settings: CodeStyleSettings) = ParadoxLocalisationCodeStyleSettings(settings)
	
	override fun createConfigurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
		return object : CodeStyleAbstractConfigurable(settings, modelSettings, configurableDisplayName) {
			override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
				return ParadoxLocalisationCodeStylePanel(currentSettings, settings)
			}
		}
	}
}