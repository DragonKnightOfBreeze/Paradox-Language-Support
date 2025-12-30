package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.lang.Language
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.ParadoxBaseFile

@Suppress("UnstableApiUsage")
abstract class ParadoxHintsProvider : InlayHintsProvider<ParadoxHintsSettings> {
    private val logger = thisLogger()

    open val showScopeContextInfo: Boolean get() = false
    open val renderIcon: Boolean get() = false
    open val renderLocalisation: Boolean get() = false

    override val previewText: String? get() = null

    override fun createSettings() = ParadoxHintsSettings()

    override fun createConfigurable(settings: ParadoxHintsSettings) = ParadoxHintsSettingsConfigurable(this, settings)

    override fun isLanguageSupported(language: Language) = language is ParadoxBaseLanguage

    override fun getCollectorFor(file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): InlayHintsCollector? {
        val project = editor.project ?: file.project
        if (project.isDefault || file !is ParadoxBaseFile) return null
        if (file.fileInfo == null) return null

        return object : FactoryInlayHintsCollector(editor) {
            private val context = ParadoxHintsContext(file, editor, settings, factory)

            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                ProgressManager.checkCanceled()
                try {
                    with(context) { collectFromElement(element, sink) }
                } catch (e: ProcessCanceledException) {
                    throw e
                } catch (e: IndexNotReadyException) {
                    throw e
                } catch (e: Exception) {
                    logger.warn(e)
                }
                // NOTE 这里需要尽可能返回 `true`，并不是注释所说的“返回 `false` 的话就不遍历子节点”
                return true
            }
        }
    }

    context(context: ParadoxHintsContext)
    protected abstract fun collectFromElement(element: PsiElement, sink: InlayHintsSink)
}
