package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.match.CwtConfigExpressionMatchService
import icu.windea.pls.core.orNull

/**
 * 行规则的综合属性的评估器。
 *
 * @see CwtRowConfig
 * @see CwtRowConfigAttributes
 */
class CwtRowConfigAttributesEvaluator {
    private var declareComplexEnum = false
    private var involveDynamicValue = false

    fun evaluate(config: CwtRowConfig): CwtRowConfigAttributes {
        for (columnConfig in config.columns) {
            processColumnConfig(columnConfig)
        }
        return buildAttributes()
    }

    private fun processColumnConfig(columnConfig: CwtPropertyConfig) {
        if (!declareComplexEnum) {
            val r = columnConfig.optionData.declareComplexEnum?.orNull() != null
            if (r) declareComplexEnum = true
        }
        val valueConfig = columnConfig.valueConfig ?: return
        val dataExpression = valueConfig.configExpression
        if (!involveDynamicValue) {
            val r = CwtConfigExpressionMatchService.matchesDynamicValue(dataExpression)
            if (r) involveDynamicValue = true
        }
    }

    private fun buildAttributes(): CwtRowConfigAttributes {
        val result = CwtRowConfigAttributes(
            declareComplexEnum,
            involveDynamicValue,
        )
        if (result == CwtTypeConfigAttributes.EMPTY) return CwtRowConfigAttributes.EMPTY
        return result
    }
}
