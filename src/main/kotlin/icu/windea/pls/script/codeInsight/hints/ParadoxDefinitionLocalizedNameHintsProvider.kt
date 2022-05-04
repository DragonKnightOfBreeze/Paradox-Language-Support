package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 定义的本地化名字的内嵌提示（最相关的本地化文本）。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionLocalizedNameHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
	companion object {
		private val settingsKey = SettingsKey<NoSettings>("ParadoxDefinitionLocalizedNameHintsSettingsKey")
	}
	
	override val name: String get() = PlsBundle.message("script.hints.definitionLocalizedName")
	override val description: String get() = PlsBundle.message("script.hints.definitionLocalizedName.description")
	override val key: SettingsKey<NoSettings> get() = settingsKey
	
	override fun createSettings() = NoSettings()
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, sink: InlayHintsSink): Boolean {
		val elementType = element.elementType ?: return false
		if(elementType == ParadoxScriptElementTypes.ROOT_BLOCK) return true
		if(element is ParadoxScriptProperty) {
			val definitionInfo = element.definitionInfo
			if(definitionInfo != null) {
				val presentation = collectDefinition(element, definitionInfo) ?: return true
				val finalPresentation = presentation.toFinalPresentation(this, file, element.project)
				val endOffset = element.propertyKey.endOffset
				sink.addInlineElement(endOffset, false, finalPresentation, false)
			}
		}
		return true
	}
	
	private fun PresentationFactory.collectDefinition(element: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo): InlayPresentation? {
		val project = element.project
		val primaryLocalisationConfigs = definitionInfo.primaryLocalisationConfigs
		for(primaryLocalisationConfig in primaryLocalisationConfigs) {
			val resolved = primaryLocalisationConfig.locationExpression.resolve(definitionInfo.name, inferParadoxLocale(), project)
			val localisation = resolved.second
			if(localisation != null) {
				val localizedName = localisation.extractText().truncate(truncateLimit) //TODO 渲染成富文本
				return smallText(localizedName)
			}
		}
		return null
	}
}