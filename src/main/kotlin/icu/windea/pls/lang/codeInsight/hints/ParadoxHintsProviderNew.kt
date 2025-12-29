package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.declarative.InlayHintsCollector
import com.intellij.codeInsight.hints.declarative.InlayHintsProvider
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.SharedBypassCollector
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.psi.ParadoxBaseFile

// org.jetbrains.kotlin.idea.k2.codeinsight.hints.AbstractKtInlayHintsProvider

abstract class ParadoxHintsProviderNew : InlayHintsProvider {
    private val log = Logger.getInstance(this::class.java)

    final override fun createCollector(
        file: PsiFile,
        editor: Editor
    ): InlayHintsCollector? {
        val project = editor.project ?: file.project
        if (project.isDefault || file !is ParadoxBaseFile) return null

        return object : SharedBypassCollector {
            override fun collectFromElement(
                element: PsiElement,
                sink: InlayTreeSink
            ) {
                try {
                    this@ParadoxHintsProviderNew.collectFromElement(element, sink)
                } catch (e: ProcessCanceledException) {
                    throw e
                } catch (e: IndexNotReadyException) {
                    throw e
                } catch (e: Exception) {
                    log.warn(e)
                }
            }
        }
    }

    protected abstract fun collectFromElement(element: PsiElement, sink: InlayTreeSink)
}
