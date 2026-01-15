package icu.windea.pls.inject.annotations

import icu.windea.pls.inject.support.InlinedDelegateFieldCodeInjectorSupport

/**
 * 要内联的属性委托字段的信息。
 *
 * 用于将 Kotlin 编译器为属性委托生成的实例字段（例如：`name$delegate`）替换为对一个静态委托表达式的直接访问，
 * 从而减少实例字段占用。
 *
 * 注意：这是一种字节码层面的优化，只适用于委托表达式可安全视为“静态且无副作用”的场景。
 *
 * @property value 属性名（将自动映射到字段名 `${value}$delegate`）。
 * @property delegateExpression 用于替换字段读取的委托表达式（Javassist 源码片段）。
 *
 * @see InlinedDelegateFieldCodeInjectorSupport
 */
@Target(AnnotationTarget.CLASS)
@Repeatable
annotation class InlinedDelegateField(
    val value: String,
    val delegateExpression: String,
)
