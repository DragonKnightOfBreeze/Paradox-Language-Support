package icu.windea.pls.lang.inspections.localisation.complexExpression

import com.intellij.codeInspection.LocalQuickFix
import icu.windea.pls.lang.quickfix.EscapeCommandFix
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement

/**
 * 不正确的 [ParadoxCommandExpression] 的代码检查。
 */
class IncorrectCommandExpressionInspection : IncorrectComplexExpressionInspectionBase() {
    override fun getFixes(element: ParadoxLocalisationExpressionElement, complexExpression: ParadoxComplexExpression, errors: List<ParadoxComplexExpressionError>): Array<LocalQuickFix> {
        if (errors.any { !it.isUnresolvedError() }) return arrayOf(EscapeCommandFix(element))
        return LocalQuickFix.EMPTY_ARRAY
    }
}
