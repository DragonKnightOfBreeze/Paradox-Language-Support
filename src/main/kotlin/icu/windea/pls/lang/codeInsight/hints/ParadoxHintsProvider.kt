package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.MenuOnClickPresentation
import com.intellij.lang.Language
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
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
            private val context = ParadoxHintsContext(file, editor, settings, factory)

            // NOTE 这里需要尽可能返回 `true`，并不是注释所说的“返回 `false` 的话就不遍历子节点”
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                ProgressManager.checkCanceled()
                return context.run { context.collectFromElement(element, sink) }
            }
        }
    }

    protected abstract fun ParadoxHintsContext.collectFromElement(element: PsiElement, sink: InlayHintsSink): Boolean

    /**
     * 将内嵌提示处理为最终要显示的内嵌注释（加上背景、左偏移、默认点击操作等）。
     */
    context(context: ParadoxHintsContext)
    protected fun InlayPresentation.toFinalPresentation(smaller: Boolean = false): InlayPresentation {
        var result = this
        result = if (smaller) {
            context.factory.roundWithBackgroundAndSmallInset(result)
        } else {
            context.factory.roundWithBackground(result)
        }
        result = MenuOnClickPresentation(result, context.project) {
            InlayHintsUtils.getDefaultInlayHintsProviderPopupActions(key) { name }
        }
        return result
    }
}

