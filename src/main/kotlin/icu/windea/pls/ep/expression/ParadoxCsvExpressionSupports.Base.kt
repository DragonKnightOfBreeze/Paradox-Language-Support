package icu.windea.pls.ep.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.config.CwtValueConfig

abstract class ParadoxCsvExpressionSupportBase : ParadoxCsvExpressionSupport {
    override fun supports(config: CwtValueConfig): Boolean {
        val dataType = config.configExpression.type
        return supports(dataType)
    }

    protected open fun supports(dataType: CwtDataType): Boolean = false
}
