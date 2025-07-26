@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.project.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import kotlin.reflect.*

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
        row {
            label(PlsBundle.message("hints.settings.textLengthLimit")).widthGroup("left")
                .applyToComponent { toolTipText = PlsBundle.message("hints.settings.textLengthLimit.tooltip") }
            textField()
                .bindIntText(property)
                .bindIntTextWhenChanged(property)
                .errorOnApply(PlsBundle.message("error.shouldBePositiveOrZero")) { (it.text.toIntOrNull() ?: 0) < 0 }
        }
    }

    protected fun Panel.createIconHeightLimitRow(property: KMutableProperty0<Int>) {
        row {
            label(PlsBundle.message("hints.settings.iconHeightLimit")).widthGroup("left")
                .applyToComponent { toolTipText = PlsBundle.message("hints.settings.iconHeightLimit.tooltip") }
            textField()
                .bindIntText(property)
                .bindIntTextWhenChanged(property)
                .errorOnApply(PlsBundle.message("error.shouldBePositive")) { (it.text.toIntOrNull() ?: 0) <= 0 }
        }
    }
}
