package icu.windea.pls.inject.annotations

import icu.windea.pls.inject.support.InlinedDelegateFieldCodeInjectorSupport

/**
 * 内联目标类中所有属性委托字段（`*$delegate`）。
 *
 * @see InlinedDelegateField
 * @see InlinedDelegateFieldCodeInjectorSupport
 */
@Target(AnnotationTarget.CLASS)
annotation class InlinedDelegateFields
