package icu.windea.pls.lang.annotations

import icu.windea.pls.model.ParadoxGameType

/**
 * 注明此功能仅适用于特定的游戏类型。
 *
 * @property value 一组指定的游戏类型。
 *
 * @see ParadoxGameType
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class WithGameType(
    vararg val value: ParadoxGameType
)
