@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.csv.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.hints.*

abstract class ParadoxCsvHintsProvider<T : Any> : ParadoxHintsProvider<T>() {
    override val previewText: String? get() = null

    override fun isLanguageSupported(language: Language): Boolean {
        return language is ParadoxCsvLanguage
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
