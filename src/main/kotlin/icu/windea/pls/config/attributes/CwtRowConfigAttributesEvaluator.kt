package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.match.CwtConfigExpressionMatchService

/**
 * 行规则的综合属性的评估器。
 *
 * @see CwtRowConfig
 * @see CwtRowConfigAttributes
 */
class CwtRowConfigAttributesEvaluator {
    private var involvesDynamicValue = false

    fun evaluate(config: CwtRowConfig): CwtRowConfigAttributes {
        for (columnConfig in config.columns.values) {
            val valueConfig = columnConfig.valueConfig ?: continue
            val dataExpression = valueConfig.configExpression
            processDataExpression(dataExpression)
        }
        return buildAttributes()
    }

    private fun processDataExpression(dataExpression: CwtDataExpression) {
        if (!involvesDynamicValue) {
            val r = CwtConfigExpressionMatchService.matchesDynamicValue(dataExpression)
            if (r) involvesDynamicValue = true
        }
    }

    private fun buildAttributes(): CwtRowConfigAttributes {
        val result = CwtRowConfigAttributes(
            involvesDynamicValue,
        )
        if (result == CwtTypeConfigAttributes.EMPTY) return CwtRowConfigAttributes.EMPTY
        return result
    }
}
