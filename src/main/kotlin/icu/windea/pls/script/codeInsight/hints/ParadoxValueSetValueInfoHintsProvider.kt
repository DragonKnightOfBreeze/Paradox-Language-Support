package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.psi.*

/**
 * 值集中的值的内嵌提示（值的类型即值集的名字）。
 */
@Suppress("UnstableApiUsage")
class ParadoxValueSetValueInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
	companion object {
		private val settingsKey = SettingsKey<NoSettings>("ParadoxValueSetValueInfoHintsSettingsKey")
	}
	
	override val name: String get() = PlsBundle.message("script.hints.valueSetValueInfo")
	override val description: String get() = PlsBundle.message("script.hints.valueSetValueInfo.description")
	override val key: SettingsKey<NoSettings> get() = settingsKey
	
	override fun createSettings() = NoSettings()
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): Boolean {
		if(element is ParadoxExpressionAwareElement && element is StubBasedPsiElement<*>) {
			//基于stub
			val config = ParadoxCwtConfigHandler.resolveConfig(element) ?: return true
			if(config.expression.type.let { it == CwtDataTypes.Value || it == CwtDataTypes.ValueSet }) {
				val valueSetName = config.expression.value ?: return true
				val presentation = collectDefinition(valueSetName)
				val finalPresentation = presentation.toFinalPresentation(this, file.project)
				val endOffset = element.endOffset
				sink.addInlineElement(endOffset, true, finalPresentation, false)
			}
		}
		return true
	}
	
	private fun PresentationFactory.collectDefinition(valueSetName: String): InlayPresentation {
		return smallText(": $valueSetName")
	}
}