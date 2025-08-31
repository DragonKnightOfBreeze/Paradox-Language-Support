package icu.windea.pls.config.config.delegated

/**
 * 用于标记此属性来自规则文件中的特定规则的键。如果规则是单独的值，则直接来自这个值。
 *
 * @property value 用于匹配键的模式。`*` 匹配任意字符， `$` 匹配此属性的值。
 *
 * @see icu.windea.pls.cwt.psi.CwtPropertyKey
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
annotation class FromKey(
    val value: String = "$"
)

/**
 * 用于标记此属性来自规则文件中的特定规则上的特定选项。
 *
 * @property value 名字与类型的声明。格式为 `{name}: {type}`。如果规则是单独的值，则直接为 `{name}`。
 * @property defaultValue 默认值。如果为空则表示未声明。
 * @property allowedValues 允许的值。如果为空则表示未声明。
 * @property multiple 作为来源的规则是否可以重复。
 *
 * @see icu.windea.pls.cwt.psi.CwtOption
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
 * 用于标记此属性来自规则文件中的特定规则中的特定属性。
 *
 * @property value 名字与类型的声明。格式为 `{name}: {type}`。如果规则是单独的值，则直接为 `{name}`。如果 `{name}` 为空，则直接来自规则的值。
 * @property defaultValue 默认值。如果为空则表示未声明。
 * @property allowedValues 允许的值。如果为空则表示未声明。
 * @property multiple 作为来源的规则是否可以重复。
 *
 * @see icu.windea.pls.cwt.psi.CwtProperty
 * @see icu.windea.pls.cwt.psi.CwtMemberElement
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
annotation class FromProperty(
    val value: String,
    val defaultValue: String = "",
    val allowedValues: Array<String> = [],
    val multiple: Boolean = false
)
