package icu.windea.pls.core.annotations

import kotlin.reflect.*

/**
 * 注明此功能基于指定的CWT规则文件中的设置实现。
 * @property filePath 文件路径（相对于规则分组的根目录）。
 * @property settingsClass 对应的设置类。
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class WithCwtSettings(
	val filePath: String,
	val settingsClass: KClass<out Any>
)
