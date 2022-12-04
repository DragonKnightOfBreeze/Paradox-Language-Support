package icu.windea.pls.core.annotations

import icu.windea.pls.core.model.*

/**
 * 注明此功能仅限于指定的游戏类型。
 * @property value 游戏类型。
 */
annotation class WithGameType(
	val value: Array<ParadoxGameType>
)