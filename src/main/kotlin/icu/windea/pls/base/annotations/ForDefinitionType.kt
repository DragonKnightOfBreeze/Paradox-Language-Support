package icu.windea.pls.base.annotations

import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression

/**
 * 注明这里的代码仅适用于特定的定义类型。仅作标记。
 *
 * @property value 一组指定的定义类型表达式。
 *
 * @see ParadoxDefinitionTypeExpression
 */
@MustBeDocumented
annotation class ForDefinitionType(
    vararg val value: String
)
