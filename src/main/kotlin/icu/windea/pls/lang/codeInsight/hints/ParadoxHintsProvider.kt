package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.MenuOnClickPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.lang.Language
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.fileInfo

@Suppress("UnstableApiUsage")
abstract class ParadoxHintsProvider : InlayHintsProvider<ParadoxHintsSettings> {
    open val showTypeInfo: Boolean get() = false
    open val showScopeContextInfo: Boolean get() = false
    open val renderIcon: Boolean get() = false
    open val renderLocalisation: Boolean get() = false

    override val previewText: String? get() = null

    override fun createSettings() = ParadoxHintsSettings()

    override fun createConfigurable(settings: ParadoxHintsSettings) = ParadoxHintsSettingsConfigurable(this, settings)

    override fun isLanguageSupported(language: Language) = language is ParadoxBaseLanguage

    override fun getCollectorFor(file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): InlayHintsCollector? {
        if (file.fileInfo == null) return null
        return object : FactoryInlayHintsCollector(editor) {
            // NOTE 这里需要尽可能返回 `true`，并不是注释所说的“返回 `false` 的话就不遍历子节点”
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                ProgressManager.checkCanceled()
                return factory.collectFromElement(element, file, editor, settings, sink)
            }
        }
    }

    protected abstract fun PresentationFactory.collectFromElement(element: PsiElement, file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): Boolean

    /**
     * 将内嵌提示处理为最终要显示的内嵌注释（加上背景、左偏移、默认点击操作等）。
     */
    protected fun InlayPresentation.toFinalPresentation(factory: PresentationFactory, project: Project?, smaller: Boolean = false): InlayPresentation {
        var presentation: InlayPresentation = if (smaller) {
            factory.roundWithBackgroundAndSmallInset(this)
        } else {
            factory.roundWithBackground(this)
        }
        if (project != null) {
            presentation = MenuOnClickPresentation(presentation, project) {
                InlayHintsUtils.getDefaultInlayHintsProviderPopupActions(key) { name }
            }
        }
        return presentation
    }
}

