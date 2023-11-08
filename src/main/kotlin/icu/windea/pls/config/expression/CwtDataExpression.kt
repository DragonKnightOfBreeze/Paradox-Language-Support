package icu.windea.pls.config.expression

import icu.windea.pls.lang.expression.*

/**
 * @property type 表达式类型，即CWT规则中的dataType。
 * @see CwtDataExpressionResolver
 */
sealed interface CwtDataExpression : CwtExpression {
    val type: CwtDataType
    val value: String?
    val extraValue: Any?
    
    @Suppress("UNCHECKED_CAST")
    fun <T> extraValue() = extraValue?.let { it as T }
}
