package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.core.optimized
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsProvider
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.model.constraints.ParadoxReferenceConstraint

/**
 * 通过内嵌提示显示动态值信息，即类型。
 */
class ParadoxDynamicValueInfoHintsProvider : ParadoxDeclarativeHintsProvider() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is ParadoxCsvColumn) return
        val expression = element.name
        if (expression.isEmpty()) return

        val resolveConstraint = ParadoxReferenceConstraint.DynamicValueReference
        if (!resolveConstraint.canResolveReference(element)) return
        val reference = element.reference ?: return
        if (!resolveConstraint.canResolve(reference)) return
        val resolved = reference.resolve() ?: return
        if (resolved !is ParadoxDynamicValueLightElement) return

        val type = resolved.presentableType
        sink.addInlinePresentation(element.endOffset, priority = 1) {
            text(": $type".optimized())
        }
    }
}
