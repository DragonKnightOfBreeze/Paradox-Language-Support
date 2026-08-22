package icu.windea.pls.lang.inspections.script.complexExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression

/**
 * 检查是否存在不正确的定值引用表达式（[ParadoxDefineReferenceExpression]）。不适用于嵌套的此类复杂表达式。
 */
class IncorrectDefineReferenceExpressionInspection : IncorrectComplexExpressionInspectionBase() {
    override fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean {
        val dataType = config.configExpression.type
        return dataType == CwtDataTypes.DefineReference
    }
}
