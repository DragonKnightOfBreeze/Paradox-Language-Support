package icu.windea.pls.lang.inspections.script.complexExpression

import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression

/**
 * 不正确的 [ParadoxDynamicValueExpression] 的代码检查。
 */
class IncorrectDynamicValueExpressionInspection : IncorrectComplexExpressionBase() {
    override fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean {
        val dataType = config.configExpression.type
        return dataType in CwtDataTypeGroups.DynamicValue
    }
}
