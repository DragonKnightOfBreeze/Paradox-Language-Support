package icu.windea.pls.lang.inspections.script.complexExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.resolve.complexExpression.ParadoxNameFormatExpression

/**
 * 检查是否存在不正确的命名格式表达式（[ParadoxNameFormatExpression]）。不适用于嵌套的此类复杂表达式。
 */
class IncorrectNameFormatExpressionInspection : IncorrectComplexExpressionInspectionBase() {
    override fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean {
        val dataType = config.configExpression.type
        return dataType == CwtDataTypes.NameFormat
    }
}
