package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.declarative.InlayHintsCollector
import com.intellij.codeInsight.hints.declarative.InlayHintsProvider
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.SharedBypassCollector
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.ParadoxBaseFile

// org.jetbrains.kotlin.idea.k2.codeinsight.hints.AbstractKtInlayHintsProvider


abstract class ParadoxDeclarativeHintsProvider : InlayHintsProvider {
    private val logger = thisLogger()

    final override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector? {
        val project = editor.project ?: file.project
        if (project.isDefault || file !is ParadoxBaseFile) return null
        if (file.fileInfo == null) return null

        return object : SharedBypassCollector {
            override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
                try {
                    this@ParadoxDeclarativeHintsProvider.collectFromElement(element, sink)
                } catch (e: ProcessCanceledException) {
                    throw e
                } catch (e: IndexNotReadyException) {
                    throw e
                } catch (e: Exception) {
                    logger.warn(e)
                }
            }
        }
    }

    protected abstract fun collectFromElement(element: PsiElement, sink: InlayTreeSink)
}
