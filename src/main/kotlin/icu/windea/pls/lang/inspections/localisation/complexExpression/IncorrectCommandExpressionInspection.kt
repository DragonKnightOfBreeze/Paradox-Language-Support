package icu.windea.pls.lang.inspections.localisation.complexExpression

import com.intellij.codeInspection.LocalQuickFix
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.lang.fixes.EscapeCommandFix
import icu.windea.pls.lang.psi.isCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement

/**
 * 检查是否存在不正确的（本地化）命令表达式（[ParadoxCommandExpression]）。不适用于嵌套的此类复杂表达式。。
 */
class IncorrectCommandExpressionInspection : IncorrectComplexExpressionInspectionBase() {
    override fun isAvailable(element: ParadoxLocalisationExpressionElement): Boolean {
        return element.isCommandExpression()
    }

    override fun getFixes(element: ParadoxLocalisationExpressionElement, complexExpression: ParadoxComplexExpression, errors: List<ParadoxComplexExpressionError>): Array<LocalQuickFix> {
        if (errors.anyFast { !it.isUnresolvedError() }) return arrayOf(EscapeCommandFix(element))
        return LocalQuickFix.EMPTY_ARRAY
    }
}
