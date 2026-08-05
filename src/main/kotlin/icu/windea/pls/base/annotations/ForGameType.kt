package icu.windea.pls.base.annotations

import icu.windea.pls.model.ParadoxGameType

/**
 * 注明这里的代码仅适用于特定的游戏类型。仅作标记。
 *
 * @property value 一组指定的游戏类型。
 *
 * @see ParadoxGameType
 */
@MustBeDocumented
annotation class ForGameType(
    vararg val value: ParadoxGameType
)
