package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import javax.swing.*

@Suppress("UnstableApiUsage")
abstract class ParadoxScriptInlayHintsProvider<T:Any> : InlayHintsProvider<T> {
	override val key: SettingsKey<T> = SettingsKey(this::class.simpleName!!)
	override val previewText: String? = null
	
	override fun createConfigurable(settings: T): ImmediateConfigurable {
		return object : ImmediateConfigurable {
			override fun createComponent(listener: ChangeListener): JComponent = panel {}
		}
	}
	
	override fun getCollectorFor(file: PsiFile, editor: Editor, settings: T, sink: InlayHintsSink): InlayHintsCollector? {
		return object : FactoryInlayHintsCollector(editor) {
			override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
				val resolved = HintType.resolve(element) ?: return true
				
				val presentations = resolved.provideHints(element).map { info -> convert(info, editor.project) }
				if (presentations.isNotEmpty()) {
					handlePresentations(presentations, editor, sink)
				} else {
					handleAfterLineEndHintsRemoval(editor, resolved, element)
				}
				
				return true
			}
			
			fun convert(inlayInfo: InlayInfo, project: Project?): PresentationAndSettings {
				val inlayText = inlayInfo.text
				val presentation = factory.roundWithBackground(factory.smallText(inlayText))
				
				val finalPresentation = if (project == null) presentation else
					InsetPresentation(
						MenuOnClickPresentation(presentation, project) {
							val provider = this@ParadoxScriptInlayHintsProvider
							listOf(
								InlayProviderDisablingAction(provider.name, file.language, project, provider.key),
								ShowInlayHintsSettings()
							)
						}, left = 1
					)
				
				return PresentationAndSettings(finalPresentation, inlayInfo.offset, inlayInfo.relatesToPrecedingText)
			}
		}
	}
	
	protected open fun handlePresentations(presentations: List<PresentationAndSettings>, editor: Editor, sink: InlayHintsSink) {
		presentations.forEach { p ->
			sink.addInlineElement(p.offset, p.relatesToPrecedingText, p.presentation,false)
		}
	}
	
	protected open fun handleAfterLineEndHintsRemoval(editor: Editor, resolved: HintType, element: PsiElement) {
		
	}
	
	data class PresentationAndSettings(val presentation: InlayPresentation, val offset: Int, val relatesToPrecedingText: Boolean)
}