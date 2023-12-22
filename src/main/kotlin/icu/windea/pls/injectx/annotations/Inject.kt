package icu.windea.pls.injectx.annotations

/**
 * 注明此代码注入器的相关信息。
 * @property value 目标类的完整类名。
 * @property pluginId 目标类所属的插件ID。如果不需要通过插件类加载器加载，则为空字符串。
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class Inject(
    val value: String,
    val pluginId: String = ""
)
