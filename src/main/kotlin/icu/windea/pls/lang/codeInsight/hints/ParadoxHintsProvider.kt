package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.ImmediateConfigurable
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
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.fileInfo
import javax.swing.JComponent
import kotlin.reflect.KMutableProperty0

@Suppress("UnstableApiUsage")
abstract class ParadoxHintsProvider : InlayHintsProvider<ParadoxHintsSettings> {
    open val renderIcon = false
    open val renderLocalisation = false

    override val previewText: String? get() = null

    override fun createConfigurable(settings: ParadoxHintsSettings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent = panel {
                if(renderLocalisation) createTextLengthLimitRow(settings)
                if(renderIcon)  createIconHeightLimitRow(settings)
            }
        }
    }

    private fun Panel.createTextLengthLimitRow(settings: ParadoxHintsSettings) {
        // 这里不能直接绑定 Kotlin 属性，否则无法追踪更改
        row {
            label(PlsBundle.message("hints.settings.textLengthLimit")).widthGroup("left")
                .applyToComponent { toolTipText = PlsBundle.message("hints.settings.textLengthLimit.tooltip") }
            textField()
                .bindIntText(settings::textLengthLimit.toAtomicProperty())
                .errorOnApply(PlsBundle.message("error.shouldBePositiveOrZero")) { (it.text.toIntOrNull() ?: 0) < 0 }
        }
    }

    private fun Panel.createIconHeightLimitRow(settings: ParadoxHintsSettings) {
        // 这里不能直接绑定 Kotlin 属性，否则无法追踪更改
        row {
            label(PlsBundle.message("hints.settings.iconHeightLimit")).widthGroup("left")
                .applyToComponent { toolTipText = PlsBundle.message("hints.settings.iconHeightLimit.tooltip") }
            textField()
                .bindIntText(settings::iconHeightLimit.toAtomicProperty())
                .errorOnApply(PlsBundle.message("error.shouldBePositive")) { (it.text.toIntOrNull() ?: 0) <= 0 }
        }
    }

    override fun isLanguageSupported(language: Language): Boolean {
        return language is ParadoxBaseLanguage
    }

    /**
     * 这里需要尽可能返回true，并不是注释所说的“返回false的话就不遍历子节点”。
     */
    override fun getCollectorFor(file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): InlayHintsCollector? {
        if (file.fileInfo == null) return null // skip it
        return object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                ProgressManager.checkCanceled()
                return factory.collect(element, file, editor, settings, sink)
            }
        }
    }

    protected abstract fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): Boolean

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

    protected fun Panel.createTypeInfoRow(subtypeProperty: KMutableProperty0<Boolean>) {
        row {
            checkBox(PlsBundle.message("hints.settings.showTypes")).selected(true).enabled(false)
            checkBox(PlsBundle.message("hints.settings.showSubtypes")).bindSelected(subtypeProperty)
        }
    }
}
