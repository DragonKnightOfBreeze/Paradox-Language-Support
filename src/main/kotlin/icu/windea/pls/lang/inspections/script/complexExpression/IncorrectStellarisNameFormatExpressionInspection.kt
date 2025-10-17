package icu.windea.pls.lang.inspections.script.complexExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression

/**
 * 不正确的 [StellarisNameFormatExpression] 的检查。
 */
class IncorrectStellarisNameFormatExpressionInspection : IncorrectComplexExpressionBase() {
    override fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean {
        val dataType = config.configExpression.type
        return dataType == CwtDataTypes.StellarisNameFormat
    }
}
