package icu.windea.pls.config.annotations

/**
 * 用于标记此属性来自规则文件中的特定成员规则上的特定选项成员（选项或选项值）。
 *
 * @property value 名字与类型的声明。格式为 `{name}: {type}`。如果规则是单独的值，则直接为 `{name}`。
 * @property defaultValue 默认值。如果为空则表示未声明。
 * @property allowedValues 允许的值。如果为空则表示未声明。
 * @property multiple 是否可以有多个。
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
annotation class FromOptionMember(
    val value: String,
    val defaultValue: String = "",
    val allowedValues: Array<String> = [],
    val multiple: Boolean = false
)
