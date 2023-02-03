package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.codeInsight.hints.ParadoxModifierLocalizedNameHintsProvider.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.localisation.*
import javax.swing.*

/**
 * 修饰符的本地化名字的内嵌提示
 */
@Suppress("UnstableApiUsage")
class ParadoxModifierLocalizedNameHintsProvider: ParadoxScriptHintsProvider<Settings>(){
	companion object {
		private val settingsKey = SettingsKey<Settings>("ParadoxModifierLocalizedNameHintsSettingsKey")
	}
	
	data class Settings(
		var textLengthLimit: Int = 30,
		var iconHeightLimit: Int = 32
	)
	
	override val name: String get() = PlsBundle.message("script.hints.modifierLocalizedName")
	override val description: String get() = PlsBundle.message("script.hints.modifierLocalizedName.description")
	override val key: SettingsKey<Settings> get() = settingsKey
	
	override fun createSettings() = Settings()
	
	override fun createConfigurable(settings: Settings): ImmediateConfigurable {
		return object : ImmediateConfigurable {
			override fun createComponent(listener: ChangeListener): JComponent = panel {
				row {
					label(PlsBundle.message("script.hints.settings.textLengthLimit")).widthGroup("left")
						.applyToComponent { toolTipText = PlsBundle.message("script.hints.settings.textLengthLimit.tooltip") }
					textField()
						.bindIntText(settings::textLengthLimit)
						.errorOnApply(PlsBundle.message("script.hints.error.shouldBePositive")) { (it.text.toIntOrNull() ?: 0) <= 0 }
				}
				row {
					label(PlsBundle.message("script.hints.settings.iconHeightLimit")).widthGroup("left")
						.applyToComponent { toolTipText = PlsBundle.message("script.hints.settings.iconHeightLimit.tooltip") }
					textField()
						.bindIntText(settings::iconHeightLimit)
						.errorOnApply(PlsBundle.message("script.hints.error.shouldBePositive")) { (it.text.toIntOrNull() ?: 0) <= 0 }
				}
			}
		}
	}
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
		if(element is ParadoxScriptStringExpressionElement) {
			//基于stub
			val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull() ?: return true
			val type = config.expression.type
			if(type == CwtDataType.Modifier) {
				val name = element.value
				val configGroup = config.info.configGroup
				val keys = ParadoxModifierHandler.getModifierNameKeys(name, configGroup)
				val selector = localisationSelector().gameType(configGroup.gameType).preferRootFrom(element).preferLocale(preferredParadoxLocale())
				val localisation = keys.firstNotNullOfOrNull {
					ParadoxLocalisationSearch.search(it, configGroup.project, selector = selector).find()
				} ?: return true
				val presentation = collectLocalisation(localisation, editor, settings)
				val finalPresentation = presentation?.toFinalPresentation(this, file.project) ?: return true
				val endOffset = element.endOffset
				sink.addInlineElement(endOffset, true, finalPresentation, false)
			}
		}
		return true
	}
	
	private fun PresentationFactory.collectLocalisation(localisation: ParadoxLocalisationProperty, editor: Editor, settings: Settings): InlayPresentation? {
		return ParadoxLocalisationTextHintsRenderer.render(localisation, this, editor, settings.textLengthLimit, settings.iconHeightLimit)
	}
}

