package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.codeInsight.hints.ParadoxDefinitionLocalizedNameHintsProvider.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.localisation.*
import javax.swing.*

/**
 * 定义的本地化名字的内嵌提示（最相关的本地化文本）。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionLocalizedNameHintsProvider : ParadoxScriptHintsProvider<Settings>() {
	data class Settings(
		var textLengthLimit: Int = 30,
		var iconHeightLimit: Int = 32
	)
	
	private val settingsKey: SettingsKey<Settings> = SettingsKey("ParadoxDefinitionLocalizedNameHintsSettingsKey")
	
	override val name: String get() = PlsBundle.message("script.hints.definitionLocalizedName")
	override val description: String get() = PlsBundle.message("script.hints.definitionLocalizedName.description")
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
						.bindIntWhenTextChanged(settings::textLengthLimit)
						.errorOnApply(PlsBundle.message("error.shouldBePositiveOrZero")) { (it.text.toIntOrNull() ?: 0) < 0 }
				}
				row {
					label(PlsBundle.message("script.hints.settings.iconHeightLimit")).widthGroup("left")
						.applyToComponent { toolTipText = PlsBundle.message("script.hints.settings.iconHeightLimit.tooltip") }
					textField()
						.bindIntText(settings::iconHeightLimit)
						.bindIntWhenTextChanged(settings::iconHeightLimit)
						.errorOnApply(PlsBundle.message("error.shouldBePositive")) { (it.text.toIntOrNull() ?: 0) <= 0 }
				}
			}
		}
	}
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
		if(element is ParadoxScriptProperty) {
			val presentation = doCollect(element, editor, settings) ?: return true
			val finalPresentation = presentation.toFinalPresentation(this, file.project)
			val endOffset = element.propertyKey.endOffset
			sink.addInlineElement(endOffset, true, finalPresentation, false)
		}
		return true
	}
	
	private fun PresentationFactory.doCollect(element: ParadoxScriptDefinitionElement, editor: Editor, settings: Settings): InlayPresentation? {
		val primaryLocalisation = ParadoxDefinitionHandler.getPrimaryLocalisation(element) ?: return null
		return ParadoxLocalisationTextInlayRenderer.render(primaryLocalisation, this, editor, settings.textLengthLimit, settings.iconHeightLimit)
	}
}
