@file:Suppress("UnstableApiUsage")

package icu.windea.pls.localisation.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.hints.*
import icu.windea.pls.localisation.*
import kotlin.reflect.*


abstract class ParadoxLocalisationHintsProvider<T : Any> : ParadoxHintsProvider<T>() {
    override val previewText: String? get() = null

    override fun isLanguageSupported(language: Language): Boolean {
        return language is ParadoxLocalisationLanguage
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


    /**
     * 将内嵌提示处理为最终要显示的内嵌注释（加上背景、左偏移等）
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

    protected fun Panel.createTextLengthLimitRow(property: KMutableProperty0<Int>) {
        row {
            label(PlsBundle.message("localisation.hints.settings.textLengthLimit")).widthGroup("left")
                .applyToComponent { toolTipText = PlsBundle.message("localisation.hints.settings.textLengthLimit.tooltip") }
            textField()
                .bindIntText(property)
                .bindIntTextWhenChanged(property)
                .errorOnApply(PlsBundle.message("error.shouldBePositiveOrZero")) { (it.text.toIntOrNull() ?: 0) < 0 }
        }
    }

    protected fun Panel.createIconHeightLimitRow(property: KMutableProperty0<Int>) {
        row {
            label(PlsBundle.message("localisation.hints.settings.iconHeightLimit")).widthGroup("left")
                .applyToComponent { toolTipText = PlsBundle.message("localisation.hints.settings.iconHeightLimit.tooltip") }
            textField()
                .bindIntText(property)
                .bindIntTextWhenChanged(property)
                .errorOnApply(PlsBundle.message("error.shouldBePositive")) { (it.text.toIntOrNull() ?: 0) <= 0 }
        }
    }
}
