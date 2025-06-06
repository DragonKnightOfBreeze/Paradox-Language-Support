package icu.windea.pls.inject.annotations

import icu.windea.pls.inject.*

/**
 * 注明注入目标信息。
 *
 * @property value 目标类名。
 * @property pluginId 目标类所属的插件ID。如果不需要通过插件类加载器加载，则为空字符串。
 * @see CodeInjector
 * @see CodeInjectorBase
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class InjectTarget(
    val value: String,
    val pluginId: String = ""
)
