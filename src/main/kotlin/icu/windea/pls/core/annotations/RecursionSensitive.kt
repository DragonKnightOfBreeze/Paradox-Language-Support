package icu.windea.pls.core.annotations

import icu.windea.pls.core.util.recursion.RecursionGuard
import icu.windea.pls.core.util.recursion.RecursionService

/**
 * 注明这里的代码在调用处或者实现中可能发生意外的递归，并且被认为不会默认地自动使用递归守卫。
 * 这意味着需要在必要时，在调用处或实现中显式使用递归守卫。
 *
 * @see RecursionGuard
 * @see RecursionService
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class RecursionSensitive
