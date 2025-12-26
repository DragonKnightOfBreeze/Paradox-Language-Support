package icu.windea.pls.config.config.delegated

/**
 * 用于标记此属性来自规则文件中的特定成员规则的键。如果规则是单独的值，则直接来自这个值。
 *
 * @property value 匹配模式。`*` 匹配任意字符， `$` 匹配此属性的值。
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
annotation class FromKey(
    val value: String = "$"
)

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
annotation class FromOption(
    val value: String,
    val defaultValue: String = "",
    val allowedValues: Array<String> = [],
    val multiple: Boolean = false
)

/**
 * 用于标记此属性来自规则文件中的特定成员规则中的特定成员（属性或单独的值）。
 *
 * @property value 名字与类型的声明。格式为 `{name}: {type}`。如果规则为属性且 `{name}` 为空，则直接来自规则的值。如果规则是单独的值，则直接为 `{name}`。
 * @property defaultValue 默认值。如果为空则表示未声明。
 * @property allowedValues 允许的值。如果为空则表示未声明。
 * @property multiple 是否可以有多个。
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
annotation class FromProperty(
    val value: String,
    val defaultValue: String = "",
    val allowedValues: Array<String> = [],
    val multiple: Boolean = false
)
