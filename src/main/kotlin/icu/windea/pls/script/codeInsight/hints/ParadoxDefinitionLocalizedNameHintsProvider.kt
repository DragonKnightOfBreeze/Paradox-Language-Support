package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.tool.*
import icu.windea.pls.script.codeInsight.hints.ParadoxDefinitionLocalizedNameHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 定义的本地化名字的内嵌提示（最相关的本地化文本）。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionLocalizedNameHintsProvider : ParadoxScriptHintsProvider<Settings>() {
	companion object {
		private val settingsKey: SettingsKey<Settings> = SettingsKey("ParadoxDefinitionLocalizedNameHintsSettingsKey")
	}
	
	data class Settings(
		var textLengthLimit: Int = 20,
		var iconHeightLimit: Int = 32
	)
	
	override val name: String get() = PlsBundle.message("script.hints.definitionLocalizedName")
	override val description: String get() = PlsBundle.message("script.hints.definitionLocalizedName.description")
	override val key: SettingsKey<Settings> get() = settingsKey
	
	override val previewText: String get() = ParadoxScriptHintsPreviewProvider.civicPreview
	
	override fun createFile(project: Project, fileType: FileType, document: Document): PsiFile {
		return super.createFile(project, fileType, document)
			.also { file -> ParadoxScriptHintsPreviewProvider.handleCivicPreviewFile(file) }
	}
	
	override fun createSettings() = Settings()
	
	override fun createConfigurable(settings: Settings): ImmediateConfigurable {
		return object : ImmediateConfigurable {
			override fun createComponent(listener: ChangeListener): JComponent = panel {
				row {
					label(PlsBundle.message("script.hints.settings.textLengthLimit"))
						.applyToComponent { toolTipText = PlsBundle.message("script.hints.settings.textLengthLimit.tooltip") }
					textField()
						.bindIntText(settings::textLengthLimit)
						.errorOnApply("Int should be positive") { (it.text.toIntOrNull() ?: 0) <= 0 }
				}
				row {
					label(PlsBundle.message("script.hints.settings.iconHeightLimit"))
						.applyToComponent { toolTipText = PlsBundle.message("script.hints.settings.iconHeightLimit.tooltip") }
					textField()
						.bindIntText(settings::iconHeightLimit)
						.errorOnApply("Int should be positive") { (it.text.toIntOrNull() ?: 0) <= 0 }
				}
			}
		}
	}
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
		if(element is ParadoxScriptProperty) {
			val definitionInfo = element.definitionInfo
			if(definitionInfo != null) {
				val presentation = collectDefinition(element, definitionInfo, editor, settings) ?: return true
				val finalPresentation = presentation.toFinalPresentation(this, file.project)
				val endOffset = element.propertyKey.endOffset
				sink.addInlineElement(endOffset, true, finalPresentation, false)
			}
		}
		return true
	}
	
	private fun PresentationFactory.collectDefinition(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo, editor: Editor, settings: Settings): InlayPresentation? {
		val primaryLocalisation = definitionInfo.resolvePrimaryLocalisation(definition) ?: return null
		return ParadoxLocalisationTextHintsRenderer.render(primaryLocalisation, this, editor, settings.textLengthLimit, settings.iconHeightLimit)
	}
}
