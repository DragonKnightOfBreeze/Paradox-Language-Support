package icu.windea.pls.lang.data.annotated

import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.joinToStringFast
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.model.type.ParadoxExpressionType

/**
 * （表达式）类型的注解信息。
 *
 * 格式：
 * - `## @type type_1;type_2` - 对于 [ForColumns]
 */
@Optimized
abstract class ParadoxTypeAnnotatedData: ParadoxAnnotatedData.Base("type") {
    class ForColumns(val types: List<ParadoxExpressionType>): ParadoxTypeAnnotatedData()  {
        override fun render(): String {
            return types.joinToStringFast(";") { it.text.quoteIfNeeded() }
        }
    }
}

/**
 * 规则表达式的注解信息。
 *
 * 格式：
 * - `## @type expression_1;expression_2` - 对于 [ForColumns]
 */
abstract class ParadoxConfigExpressionAnnotatedData: ParadoxAnnotatedData.Base("config_expression") {
    class ForColumns(val configExpressions: List<CwtDataExpression>): ParadoxConfigExpressionAnnotatedData() {
        override fun render(): String {
            return configExpressions.joinToStringFast(";") { it.expressionString.quoteIfNeeded() }
        }
    }
}
