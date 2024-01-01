package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 值集值的内嵌提示（值的类型即值集的名字）。
 */
@Suppress("UnstableApiUsage")
class ParadoxDynamicValueInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
	private val settingsKey = SettingsKey<NoSettings>("ParadoxDynamicValueInfoHintsSettingsKey")
	
	override val name: String get() = PlsBundle.message("script.hints.dynamicValueInfo")
	override val description: String get() = PlsBundle.message("script.hints.dynamicValueInfo.description")
	override val key: SettingsKey<NoSettings> get() = settingsKey
	
	override fun createSettings() = NoSettings()
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): Boolean {
		if(element !is ParadoxScriptStringExpressionElement) return true
		if(!element.isExpression()) return true
        val config = CwtConfigHandler.getConfigs(element).firstOrNull() ?: return true
		val type = config.expression.type
		if(type.isDynamicValueType()) {
			val dynamicValueType = config.expression.value ?: return true
			val presentation = doCollect(dynamicValueType)
			val finalPresentation = presentation.toFinalPresentation(this, file.project)
			val endOffset = element.endOffset
			sink.addInlineElement(endOffset, true, finalPresentation, false)
		}
		return true
	}
	
	private fun PresentationFactory.doCollect(dynamicValueType: String): InlayPresentation {
		return smallText(": $dynamicValueType")
	}
}

