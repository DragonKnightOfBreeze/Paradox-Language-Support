package icu.windea.pls.base.annotations

import icu.windea.pls.config.config.CwtDetachedConfig
import kotlin.reflect.KClass

/**
 * 注明这里的实现代码基于特定的内部规则。这些规则不支持自定义（或是目前尚不支持）。
 *
 * @property filePath 规则文件的文件路径（相对于规则分组的根目录）。
 * @property type 规则的类型。
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class FromInternalConfig(
    val filePath: String,
    val type: KClass<out CwtDetachedConfig>
)
