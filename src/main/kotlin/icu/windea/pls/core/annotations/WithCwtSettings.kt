package icu.windea.pls.core.annotations

import icu.windea.pls.config.config.*
import kotlin.reflect.*

/**
 * 注明此功能基于指定的CWT规则实现。
 * @property filePath 文件路径（相对于规则分组的根目录）。
 * @property configClass 对应的CWT规则类。
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class WithCwtSettings(
	val filePath: String,
	val configClass: KClass<out CwtConfig<*>>
)
