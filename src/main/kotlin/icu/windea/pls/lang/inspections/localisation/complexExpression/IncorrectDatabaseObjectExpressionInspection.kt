package icu.windea.pls.lang.inspections.localisation.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression

/**
 * 不正确的 [ParadoxDatabaseObjectExpression] 的代码检查。
 */
class IncorrectDatabaseObjectExpressionInspection : IncorrectComplexExpressionBase() {
    override fun resolveComplexExpression(element: ParadoxLocalisationExpressionElement, configGroup: CwtConfigGroup): ParadoxComplexExpression? {
        if (!element.isDatabaseObjectExpression(strict = true)) return null
        val value = element.value
        val textRange = TextRange.create(0, value.length)
        return ParadoxDatabaseObjectExpression.resolve(value, textRange, configGroup)
    }
}
