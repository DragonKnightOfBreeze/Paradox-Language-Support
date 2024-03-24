package icu.windea.pls.cwt.codeStyle

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.cwt.*

class CwtCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
	override fun getLanguage() = CwtLanguage
	
	override fun getConfigurableDisplayName() = PlsBundle.message("options.cwt.displayName")
	
	override fun createCustomSettings(settings: CodeStyleSettings) = CwtCodeStyleSettings(settings)
	
	override fun createConfigurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
		return object : CodeStyleAbstractConfigurable(settings, modelSettings, configurableDisplayName) {
			override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
				return CwtCodeStylePanel(currentSettings, settings)
			}
		}
	}
}