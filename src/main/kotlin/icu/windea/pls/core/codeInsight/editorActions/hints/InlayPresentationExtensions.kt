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
