package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import javax.swing.*

@Suppress("UnstableApiUsage")
class ParadoxScriptDefinitionHintsProvider: ParadoxScriptInlayHintsProvider<ParadoxScriptDefinitionHintsProvider.Settings>() {
	data class Settings(
		var definitionLocalizedName:Boolean = true,
		var definitionNameType: Boolean = true
	)
	
	override val name: String = message("paradox.script.hints.definition")
	
	override fun createConfigurable(settings: Settings): ImmediateConfigurable {
		return object : ImmediateConfigurable {
			override fun createComponent(listener: ChangeListener): JComponent = panel {}
			
			override val mainCheckboxText: String = message("paradox.script.hints.mainCheckboxText")
			
			override val cases: List<ImmediateConfigurable.Case>
				get() = listOf(
					ImmediateConfigurable.Case(
						message("paradox.script.hints.definition.definitionLocalizedName"),
						"hints.definition.definitionLocalizedName",
						settings::definitionLocalizedName
					),
					ImmediateConfigurable.Case(
						message("paradox.script.hints.definition.definitionNameType"),
						"hints.definition.definitionNameType",
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
			HintType.DEFINITION_LOCALIZED_NAME_HINT -> settings.definitionLocalizedName
			HintType.DEFINITION_NAME_TYPE_HINT -> settings.definitionNameType
			else -> false
		}
	}
}