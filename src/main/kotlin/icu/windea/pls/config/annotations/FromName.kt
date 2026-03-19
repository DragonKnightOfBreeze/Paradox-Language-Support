package icu.windea.pls.config.annotations

/**
 * 用于标记此属性来自规则文件中的特定成员规则的名字。
 *
 * 如果是属性，则来自属性的键；如果是单独的值，则直接来自这个值。
 *
 * @property value 匹配模式。`*` 匹配任意字符， `$` 匹配此属性的值。
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
annotation class FromName(
    val value: String = "$"
)
