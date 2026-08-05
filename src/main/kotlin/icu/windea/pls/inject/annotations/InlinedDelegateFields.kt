package icu.windea.pls.inject.annotations

import icu.windea.pls.inject.support.InlinedDelegateFieldCodeInjectorSupport

/**
 * 内联目标类中所有属性委托字段（`*$delegate`）。
 *
 * 注意：这是一种字节码层面的优化，只适用于委托表达式可安全视为“静态且无副作用”的场景。
 *
 * @see InlinedDelegateField
 * @see InlinedDelegateFieldCodeInjectorSupport
 */
@Target(AnnotationTarget.CLASS)
annotation class InlinedDelegateFields
