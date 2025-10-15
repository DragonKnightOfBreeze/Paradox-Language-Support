@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.MenuOnClickPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.toAtomicProperty
import kotlin.reflect.KMutableProperty0

abstract class ParadoxHintsProvider<T : Any> : InlayHintsProvider<T> {
    open val renderIcon = false
    open val renderLocalisation = false

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

    protected fun Panel.createTypeInfoRow(subtypeProperty: KMutableProperty0<Boolean>) {
        row {
            checkBox(PlsBundle.message("hints.settings.showTypes")).selected(true).enabled(false)
            checkBox(PlsBundle.message("hints.settings.showSubtypes")).bindSelected(subtypeProperty)
        }
    }

    protected fun Panel.createTextLengthLimitRow(property: KMutableProperty0<Int>) {
        // 这里不能直接绑定 Kotlin 属性，否则无法追踪更改
        row {
            label(PlsBundle.message("hints.settings.textLengthLimit")).widthGroup("left")
                .applyToComponent { toolTipText = PlsBundle.message("hints.settings.textLengthLimit.tooltip") }
            textField()
                .bindIntText(property.toAtomicProperty())
                .errorOnApply(PlsBundle.message("error.shouldBePositiveOrZero")) { (it.text.toIntOrNull() ?: 0) < 0 }
        }
    }

    protected fun Panel.createIconHeightLimitRow(property: KMutableProperty0<Int>) {
        // 这里不能直接绑定 Kotlin 属性，否则无法追踪更改
        row {
            label(PlsBundle.message("hints.settings.iconHeightLimit")).widthGroup("left")
                .applyToComponent { toolTipText = PlsBundle.message("hints.settings.iconHeightLimit.tooltip") }
            textField()
                .bindIntText(property.toAtomicProperty())
                .errorOnApply(PlsBundle.message("error.shouldBePositive")) { (it.text.toIntOrNull() ?: 0) <= 0 }
        }
    }
}
