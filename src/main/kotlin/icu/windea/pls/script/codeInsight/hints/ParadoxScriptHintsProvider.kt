package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.script.*
import javax.swing.*

@Suppress("UnstableApiUsage")
abstract class ParadoxScriptHintsProvider<T : Any> : InlayHintsProvider<T> {
	override val previewText: String? get() = null
	
	override fun createConfigurable(settings: T): ImmediateConfigurable {
		return object : ImmediateConfigurable {
			override fun createComponent(listener: ChangeListener) = JPanel()
		}
	}
	
	override fun getCollectorFor(file: PsiFile, editor: Editor, settings: T, sink: InlayHintsSink): InlayHintsCollector? {
		if(file.fileType != ParadoxScriptFileType) return null
		return object : FactoryInlayHintsCollector(editor) {
			override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
				return factory.collect(element, file, editor, sink)
			}
		}
	}
	
	protected abstract fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, sink: InlayHintsSink): Boolean
	
	/**
	 * 将内嵌提示处理为最终要显示的内嵌注释（加上背景、左偏移等）
	 */
	protected fun InlayPresentation.toFinalPresentation(factory: PresentationFactory, file: PsiFile, project: Project?): InlayPresentation {
		var presentation = factory.roundWithBackground(this)
		if(project == null) return presentation
		presentation = MenuOnClickPresentation(presentation, project) {
			listOf(InlayProviderDisablingAction(name, file.language, project, key), ShowInlayHintsSettings())
		}
		presentation = InsetPresentation(presentation, left = 1)
		return presentation
	}
}