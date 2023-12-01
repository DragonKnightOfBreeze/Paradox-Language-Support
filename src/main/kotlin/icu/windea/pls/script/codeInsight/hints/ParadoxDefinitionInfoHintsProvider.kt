package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.script.codeInsight.hints.ParadoxDefinitionInfoHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 定义信息的内嵌提示（定义的名字和类型）。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionInfoHintsProvider : ParadoxScriptHintsProvider<Settings>() {
	data class Settings(
		var showSubtypes: Boolean = true
	)
	
	private val settingsKey = SettingsKey<Settings>("ParadoxDefinitionInfoHintsSettingsKey")
	
	override val name: String get() = PlsBundle.message("script.hints.definitionInfo")
	override val description: String get() = PlsBundle.message("script.hints.definitionInfo.description")
	override val key: SettingsKey<Settings> get() = settingsKey
	
	override fun createSettings() = Settings()
	
	override fun createConfigurable(settings: Settings): ImmediateConfigurable {
		return object : ImmediateConfigurable {
			override fun createComponent(listener: ChangeListener): JComponent = panel {
				row {
					checkBox(PlsBundle.message("script.hints.settings.showSubtypes")).bindSelected(settings::showSubtypes)
				}
			}
		}
	}
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
		if(element is ParadoxScriptProperty) {
			val definitionInfo = element.definitionInfo
			if(definitionInfo != null) {
				//忽略类似event_namespace这样定义的值不是子句的定义
				if(definitionInfo.declarationConfig?.propertyConfig?.isBlock == false) return true
				
				val presentation = doCollect(definitionInfo, settings)
				val finalPresentation = presentation.toFinalPresentation(this, file.project)
				val endOffset = element.propertyKey.endOffset
				sink.addInlineElement(endOffset, true, finalPresentation, false)
			}
		}
		return true
	}
	
	private fun PresentationFactory.doCollect(definitionInfo: ParadoxDefinitionInfo, settings: Settings): InlayPresentation {
		val presentations: MutableList<InlayPresentation> = mutableListOf()
		//如果definitionName和rootKey相同，则省略definitionName
		val name = definitionInfo.name
		if(name.equals(definitionInfo.rootKey, true)) {
			presentations.add(smallText(": "))
		} else {
			presentations.add(smallText("$name: "))
		}
		val typeConfig = definitionInfo.typeConfig
		presentations.add(psiSingleReference(smallText(typeConfig.name)) { typeConfig.pointer.element })
		if(settings.showSubtypes) {
			val subtypeConfigs = definitionInfo.subtypeConfigs
			for(subtypeConfig in subtypeConfigs) {
				presentations.add(smallText(", "))
				presentations.add(psiSingleReference(smallText(subtypeConfig.name)) { subtypeConfig.pointer.element })
			}
		}
		return SequencePresentation(presentations)
	}
}

