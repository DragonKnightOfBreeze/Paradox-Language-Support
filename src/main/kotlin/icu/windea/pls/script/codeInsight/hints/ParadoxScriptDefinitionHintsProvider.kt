package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import javax.swing.*

@Suppress("UnstableApiUsage")
class ParadoxScriptDefinitionHintsProvider: ParadoxScriptInlayHintsProvider<ParadoxScriptDefinitionHintsProvider.Settings>() {
	data class Settings(
		var definitionNameType: Boolean = true
	)
	
	override val name: String = message("paradox.script.hints.settings.definition")
	
	override fun createConfigurable(settings: Settings): ImmediateConfigurable {
		return object : ImmediateConfigurable {
			override fun createComponent(listener: ChangeListener): JComponent = panel {}
			
			override val mainCheckboxText: String = message("paradox.script.hints.settings.mainCheckboxText")
			
			override val cases: List<ImmediateConfigurable.Case>
				get() = listOf(
					ImmediateConfigurable.Case(
						message("paradox.script.hints.settings.definition.definitionLocalizedName"),
						"hints.type.property",
						settings::definitionNameType
					)
				)
		}
	}
	
	override fun createSettings(): Settings {
		return Settings()
	}
	
	override fun isElementSupported(resolved: HintType?, settings: Settings): Boolean {
		return when (resolved) {
			HintType.DEFINITION_LOCALIZED_NAME_HINT -> settings.definitionNameType
			else -> false
		}
	}
}