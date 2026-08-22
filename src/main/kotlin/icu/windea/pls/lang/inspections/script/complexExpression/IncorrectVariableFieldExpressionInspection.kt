package icu.windea.pls.lang.inspections.script.complexExpression

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression

/**
 * 检查是否存在不正确的变量字段表达式（[ParadoxVariableFieldExpression]）。不适用于嵌套的此类复杂表达式。
 */
class IncorrectVariableFieldExpressionInspection : IncorrectComplexExpressionInspectionBase() {
    override fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean {
        val dataType = config.configExpression.type
        return dataType in CwtDataTypeSets.VariableField
    }
}
