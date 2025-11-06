@file:Suppress("UnstableApiUsage")

package icu.windea.pls.core.codeInsight.editorActions.hints

import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.MenuOnClickPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.codeInsight.hints.presentation.SequencePresentation
import com.intellij.openapi.project.Project
import icu.windea.pls.core.optimized

fun List<InlayPresentation>.mergePresentations(): InlayPresentation? {
    return when {
        isEmpty() -> null
        size == 1 -> this.first()
        else -> SequencePresentation(this.optimized())
    }
}

/**
 * 将内嵌提示处理为最终要显示的内嵌注释（加上背景、左偏移等）。
 */
fun InlayPresentation.toFinalPresentation(
    factory: PresentationFactory,
    provider: InlayHintsProvider<*>,
    project: Project?,
    smaller: Boolean = false
): InlayPresentation {
    var presentation: InlayPresentation = if (smaller) {
        factory.roundWithBackgroundAndSmallInset(this)
    } else {
        factory.roundWithBackground(this)
    }
    if (project != null) {
        presentation = MenuOnClickPresentation(presentation, project) {
            InlayHintsUtils.getDefaultInlayHintsProviderPopupActions(provider.key) { provider.name }
        }
    }
    return presentation
}
