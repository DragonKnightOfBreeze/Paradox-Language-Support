@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.declarative.HintColorKind
import com.intellij.codeInsight.hints.declarative.HintFontSize
import com.intellij.codeInsight.hints.declarative.HintFormat
import com.intellij.codeInsight.hints.declarative.HintMarginPadding
import com.intellij.codeInsight.hints.declarative.InlayActionData
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.codeInsight.hints.declarative.PresentationTreeBuilder
import com.intellij.codeInsight.hints.declarative.PsiPointerInlayActionNavigationHandler
import com.intellij.codeInsight.hints.declarative.PsiPointerInlayActionPayload
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer

// region Declarative

private val HINT_FORMAT = HintFormat(
    HintColorKind.Default,
    HintFontSize.ABitSmallerThanInEditor,
    HintMarginPadding.OnlyPadding,
)

context(_: ParadoxDeclarativeHintsProvider)
fun InlayTreeSink.addInlinePresentation(
    offset: Int,
    relatedToPrevious: Boolean = true,
    priority: Int = 0,
    tooltip: String? = null,
    builder: PresentationTreeBuilder.() -> Unit,
) = addPresentation(InlineInlayPosition(offset, relatedToPrevious, priority), null, tooltip, HINT_FORMAT, builder)

fun PresentationTreeBuilder.text(text: String, pointer: SmartPsiElementPointer<out PsiElement>?) {
    val actionData = pointer?.let { InlayActionData(PsiPointerInlayActionPayload(it), PsiPointerInlayActionNavigationHandler.HANDLER_ID) }
    text(text, actionData)
}

// endregion

// region Non-Declarative

context(_: ParadoxHintsProvider, context: ParadoxHintsContext)
fun InlayHintsSink.addInlinePresentation(
    offset: Int,
    relatedToPrevious: Boolean = true,
    smaller: Boolean = false,
    builder: ParadoxHintsBuilder.() -> Unit
) {
    val presentation = ParadoxHintsBuilder().also { it.builder() }.build(smaller) ?: return
    addInlineElement(offset, relatedToPrevious, presentation, false)
}

// endregion
