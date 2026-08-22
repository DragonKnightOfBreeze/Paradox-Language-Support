package icu.windea.pls.lang.inspections.script.complexExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.resolve.complexExpression.ParadoxArrayDefineReferenceExpression

/**
 * 检查是否存在不正确的数组定值引用表达式（[ParadoxArrayDefineReferenceExpression]）。不适用于嵌套的此类复杂表达式。
 */
class IncorrectArrayDefineReferenceExpressionInspection : IncorrectComplexExpressionInspectionBase() {
    override fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean {
        val dataType = config.configExpression.type
        return dataType == CwtDataTypes.ArrayDefineReference
    }
}
