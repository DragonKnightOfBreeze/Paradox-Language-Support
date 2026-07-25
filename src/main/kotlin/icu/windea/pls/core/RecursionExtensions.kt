@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core

import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.core.util.recursion.RecursionGuard
import icu.windea.pls.core.util.recursion.RecursionService

/**
 * @see RecursionService.withRecursionGuard
 */
inline fun <T> withRecursionGuard(name: String, action: RecursionGuard.() -> T): T? {
    return RecursionService.withRecursionGuard(name, action)
}

/**
 * @see RecursionService.withRecursionGuard
 * @see RecursionGuard.withRecursionCheck
 */
inline fun <T> runWithRecursionGuard(name: String, key: String, action: () -> T): T? {
    return RecursionService.withRecursionGuard(name) { withRecursionCheck(key) { action() } }
}

/**
 * @see RecursionService.withContextRecursionGuard
 */
inline fun <T> withContextRecursionGuard(context: UserDataHolder, name: String, action: RecursionGuard.() -> T): T? {
    return RecursionService.withContextRecursionGuard(context, name, action)
}

/**
 * @see RecursionService.withContextRecursionGuard
 * @see RecursionGuard.withRecursionCheck
 */
inline fun <T> runWithContextRecursionGuard(context: UserDataHolder, name: String, key: String, action: () -> T): T? {
    return RecursionService.withContextRecursionGuard(context, name) { withRecursionCheck(key) { action() } }
}
