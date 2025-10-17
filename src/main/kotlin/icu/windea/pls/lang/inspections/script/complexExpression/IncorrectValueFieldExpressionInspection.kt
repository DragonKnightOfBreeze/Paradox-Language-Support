package icu.windea.pls.lang.inspections.script.complexExpression

import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression

/**
 * 不正确的 [ParadoxValueFieldExpression] 的检查。
 */
class IncorrectValueFieldExpressionInspection : IncorrectComplexExpressionBase() {
    override fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean {
        val dataType = config.configExpression.type
        return dataType in CwtDataTypeGroups.ValueField
    }
}
