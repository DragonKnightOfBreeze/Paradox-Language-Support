package icu.windea.pls.ep.resolve.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.CwtDataExpression

abstract class ParadoxCsvExpressionSupportBase : ParadoxCsvExpressionSupport {
    override fun supports(config: CwtValueConfig, configExpression: CwtDataExpression): Boolean {
        return supports(configExpression.type)
    }

    protected open fun supports(dataType: CwtDataType): Boolean = false
}
