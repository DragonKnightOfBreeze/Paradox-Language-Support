package icu.windea.pls.lang.inspections.script.complexExpression

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression

/**
 * 检查是否存在不正确的值字段表达式（ [ParadoxValueFieldExpression]）。不适用于嵌套的此类复杂表达式。
 */
class IncorrectValueFieldExpressionInspection : IncorrectComplexExpressionInspectionBase() {
    override fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean {
        val dataType = config.configExpression.type
        return dataType in CwtDataTypeSets.ValueField
    }
}
