package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 封装变量引用信息的内嵌提示（变量的值）。
 */
@Suppress("UnstableApiUsage")
@Deprecated("UNNECESSARY")
class ParadoxScriptedVariableReferenceInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
	companion object {
		private val settingsKey: SettingsKey<NoSettings> = SettingsKey("ParadoxVariableReferenceInfoHintsSettingsKey")
	}
	
	override val name: String get() = PlsBundle.message("script.hints.scriptedVariableReferenceInfo")
	override val description: String get() = PlsBundle.message("script.hints.scriptedVariableReferenceInfo.description")
	override val key: SettingsKey<NoSettings> get() = settingsKey
	
	override fun createSettings() = NoSettings()
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, sink: InlayHintsSink): Boolean {
		if(element is ParadoxScriptVariableReference) {
			val referenceValue = element.referenceValue ?: return true //不检查值的类型
			val presentation = collectValue(referenceValue) ?: return true
			val finalPresentation = presentation.toFinalPresentation(this, file, element.project)
			val endOffset = element.endOffset
			sink.addInlineElement(endOffset, false, finalPresentation, false)
		}
		return true
	}
	
	private fun PresentationFactory.collectValue(value: ParadoxScriptValue): InlayPresentation? {
		val v = value.value
		if(v.isEmpty()) return null
		return smallText(v)
	}
}