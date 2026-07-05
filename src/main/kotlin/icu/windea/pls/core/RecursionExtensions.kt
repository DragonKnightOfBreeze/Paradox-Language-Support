@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core

import com.intellij.openapi.util.StackOverflowPreventedException
import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.core.util.recursion.RecursionGuard
import icu.windea.pls.core.util.recursion.RecursionService

/**
 * 执行一段代码，通过维护在当前线程中的命名的 [RecursionGuard] 尝试避免递归导致的堆栈溢出。
 * 捕获 [StackOverflowError] 和 [StackOverflowPreventedException] 并返回 `null`。
 */
inline fun <T> withRecursionGuard(name: String, action: RecursionGuard.() -> T): T? {
    return RecursionService.withRecursionGuard(name, action)
}

/**
 * 执行一段代码，通过维护在当前线程中的 [RecursionGuard] 尝试避免递归导致的堆栈溢出。
 * 捕获 [StackOverflowError] 和 [StackOverflowPreventedException] 并返回 `null`。
 *
 * 这个方法直接使用 [action] 的类名作为递归守卫实例的名字。
 */
inline fun <T> withRecursionGuard(noinline action: RecursionGuard.() -> T): T? {
    return RecursionService.withRecursionGuard(action::class.java.name, action)
}

/**
 * 执行一段代码，通过维护在当前上下文对象中的命名的 [RecursionGuard] 尝试避免递归导致的堆栈溢出。
 * 捕获 [StackOverflowError] 和 [StackOverflowPreventedException] 并返回 `null`。
 *
 * 适合在序列构建器和协程上下文中使用。
 */
inline fun <T> withContextRecursionGuard(context: UserDataHolder, name: String, action: RecursionGuard.() -> T): T? {
    return RecursionService.withContextRecursionGuard(context, name, action)
}

/**
 * 执行一段代码，通过维护在当前上下文对象中的 [RecursionGuard] 尝试避免递归导致的堆栈溢出。
 * 捕获 [StackOverflowError] 和 [StackOverflowPreventedException] 并返回 `null`。
 *
 * 这个方法直接使用 [action] 的类名作为递归守卫实例的名字。
 *
 * 适合在序列构建器和协程上下文中使用。
 */
inline fun <T> withContextRecursionGuard(context: UserDataHolder, noinline action: RecursionGuard.() -> T): T? {
    return RecursionService.withContextRecursionGuard(context, action::class.java.name, action)
}
