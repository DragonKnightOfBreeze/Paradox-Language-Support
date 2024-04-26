package icu.windea.pls.core.annotations

import icu.windea.pls.model.*

/**
 * 注明此功能仅适用于特定的游戏类型。
 * 
 * @property value 游戏类型。
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class WithGameType(
	vararg val value: ParadoxGameType
)
