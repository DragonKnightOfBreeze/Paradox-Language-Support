package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsFacade
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsProvider
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.codeInsight.hints.text
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.model.constraints.ParadoxResolveConstraint

/**
 * 通过内嵌提示显示复杂枚举值信息，即枚举名。
 */
class ParadoxComplexEnumValueInfoHintsProvider : ParadoxDeclarativeHintsProvider() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is ParadoxCsvColumn) return
        val expression = element.name
        if (expression.isEmpty()) return

        val resolveConstraint = ParadoxResolveConstraint.ComplexEnumValue
        if (!resolveConstraint.canResolveReference(element)) return
        val reference = element.reference ?: return
        if (!resolveConstraint.canResolve(reference)) return
        val resolved = reference.resolve() ?: return
        if (resolved !is ParadoxComplexEnumValueElement) return

        val enumName = resolved.enumName
        val configGroup = PlsFacade.getConfigGroup(resolved.project, resolved.gameType)
        val config = configGroup.complexEnums[enumName] ?: return
        sink.addInlinePresentation(element.endOffset, priority = 1) {
            text(": ")
            text(enumName, config.pointer)
        }
    }
}
