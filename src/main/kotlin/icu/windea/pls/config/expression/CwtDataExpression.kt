package icu.windea.pls.config.expression

import icu.windea.pls.core.*
import icu.windea.pls.lang.expression.*

/**
 * @property type 表达式类型，即CWT规则中的dataType。
 * @see CwtDataExpressionResolver
 */
sealed interface CwtDataExpression : CwtExpression {
    val type: CwtDataType
    val value: String?
    val extraValue: Any?
}

inline fun <reified T> CwtDataExpression.extraValue() = extraValue?.cast<T>()
