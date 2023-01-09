package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

/**
 * 定义信息的内嵌提示（定义的名字和类型）。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
	companion object {
		private val settingsKey = SettingsKey<NoSettings>("ParadoxDefinitionInfoHintsSettingsKey")
	}
	
	override val name: String get() = PlsBundle.message("script.hints.definitionInfo")
	override val description: String get() = PlsBundle.message("script.hints.definitionInfo.description")
	override val key: SettingsKey<NoSettings> get() = settingsKey
	
	override fun createSettings() = NoSettings()
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): Boolean {
		if(element is ParadoxScriptProperty) {
			val definitionInfo = element.definitionInfo
			if(definitionInfo != null) {
				val presentation = collectDefinition(definitionInfo)
				val finalPresentation = presentation.toFinalPresentation(this, file.project)
				val endOffset = element.propertyKey.endOffset
				sink.addInlineElement(endOffset, true, finalPresentation, false)
			}
		}
		return true
	}
	
	private fun PresentationFactory.collectDefinition(definitionInfo: ParadoxDefinitionInfo): InlayPresentation {
		val presentations: MutableList<InlayPresentation> = SmartList()
		val name = definitionInfo.name
		//如果definitionName和rootKey相同，则省略definitionName
		if(name.equals(definitionInfo.rootKey, true)) {
			presentations.add(smallText(": "))
		} else {
			presentations.add(smallText("$name: "))
		}
		val typeConfig = definitionInfo.typeConfig
		presentations.add(psiSingleReference(smallText(typeConfig.name)) { typeConfig.pointer.element })
		val subtypeConfigs = definitionInfo.subtypeConfigs
		for(subtypeConfig in subtypeConfigs) {
			presentations.add(smallText(", "))
			presentations.add(psiSingleReference(smallText(subtypeConfig.name)) { subtypeConfig.pointer.element })
		}
		return SequencePresentation(presentations)
	}
}

