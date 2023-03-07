package icu.windea.pls.core.annotations

import icu.windea.pls.lang.model.*

/**
 * 注明此功能仅限于指定的游戏类型。
 * @property value 游戏类型。
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class WithGameType(
	vararg val value: ParadoxGameType
)

/**
 * 注明此功能仅限同时启用了指定的插件时才会启用，而相关代码并未放到`icu.windea.pls.extension`中对应的包中。
 * @property value 插件名。
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class WithExtension(
	vararg val value: String
)