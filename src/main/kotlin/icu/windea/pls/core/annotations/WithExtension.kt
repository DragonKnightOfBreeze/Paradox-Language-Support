package icu.windea.pls.core.annotations

/**
 * 注明此功能仅限同时启用了指定的插件时才会启用，而相关代码并未放到`icu.windea.pls.extension`中对应的包中。
 * @property value 插件ID。
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class WithExtension(
	vararg val value: String
)