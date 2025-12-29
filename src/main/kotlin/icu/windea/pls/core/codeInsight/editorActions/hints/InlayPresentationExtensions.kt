@file:Suppress("UnstableApiUsage")

package icu.windea.pls.core.codeInsight.editorActions.hints

import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.SequencePresentation
import icu.windea.pls.core.optimized

fun List<InlayPresentation>.mergePresentations(): InlayPresentation? {
    return when {
        isEmpty() -> null
        size == 1 -> this.first()
        else -> SequencePresentation(this.optimized())
    }
}
