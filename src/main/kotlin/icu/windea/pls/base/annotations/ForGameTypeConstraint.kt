package icu.windea.pls.base.annotations

import icu.windea.pls.model.constraints.ParadoxGameTypeConstraint

/**
 * 注明这里的代码仅适用于特定的游戏类型约束。仅作标记。
 *
 * @property value 一组指定的游戏类型约束。
 *
 * @see ParadoxGameTypeConstraint
 */
@MustBeDocumented
annotation class ForGameTypeConstraint(
    vararg val value: ParadoxGameTypeConstraint
)
