@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.lang.Language
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.script.ParadoxScriptLanguage

abstract class ParadoxScriptHintsProvider<T : Any> : ParadoxHintsProvider<T>() {
    override val previewText: String? get() = null

    override fun isLanguageSupported(language: Language): Boolean {
        return language is ParadoxScriptLanguage
    }

    override fun createConfigurable(settings: T): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener) = panel { }
        }
    }

    /**
     * 这里需要尽可能返回true，并不是注释所说的“返回false的话就不遍历子节点”。
     */
    override fun getCollectorFor(file: PsiFile, editor: Editor, settings: T, sink: InlayHintsSink): InlayHintsCollector? {
        if (file.fileInfo == null) return null // skip it
        return object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                ProgressManager.checkCanceled()
                return factory.collect(element, file, editor, settings, sink)
            }
        }
    }

    protected abstract fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: T, sink: InlayHintsSink): Boolean

}
