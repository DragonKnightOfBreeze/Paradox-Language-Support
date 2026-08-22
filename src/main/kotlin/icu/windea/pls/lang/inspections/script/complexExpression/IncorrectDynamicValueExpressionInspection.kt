package icu.windea.pls.lang.inspections.script.complexExpression

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression

/**
 * 检查是否存在不正确的动态值表达式（[ParadoxDynamicValueExpression]）。不适用于嵌套的此类复杂表达式。
 */
class IncorrectDynamicValueExpressionInspection : IncorrectComplexExpressionInspectionBase() {
    override fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean {
        val dataType = config.configExpression.type
        return dataType in CwtDataTypeSets.DynamicValue
    }
}
