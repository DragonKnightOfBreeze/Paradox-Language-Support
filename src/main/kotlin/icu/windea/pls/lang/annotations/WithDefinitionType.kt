package icu.windea.pls.lang.annotations

import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression

/**
 * 注明此功能仅适用于特定的定义类型。
 *
 * @property value 一组指定的定义类型表达式。
 *
 * @see ParadoxDefinitionTypeExpression
 */
annotation class WithDefinitionType(
    vararg val value: String
)
