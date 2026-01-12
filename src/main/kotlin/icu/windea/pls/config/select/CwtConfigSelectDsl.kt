@file:Suppress("unused", "RedundantWith", "UnusedReceiverParameter", "NOTHING_TO_INLINE")

package icu.windea.pls.config.select

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.util.KeyRegistry

@DslMarker
annotation class CwtConfigSelectDsl

class CwtConfigSelectScope {
    val context = CwtConfigSelectContext()
}

class CwtConfigSelectContext : UserDataHolderBase() {
    object Keys : KeyRegistry()

    operator fun plus(other: CwtConfigSelectContext): CwtConfigSelectContext {
        Keys.copy(other, this)
        return this
    }

    operator fun <T> plus(value: T): T {
        return value
    }
}

@CwtConfigSelectDsl
inline fun <R> selectScope(scope: CwtConfigSelectScope = CwtConfigSelectScope(), block: context(CwtConfigSelectScope) () -> R): R {
    return block.invoke(scope)
}

// NOTE 2.1.1 cannot be inline or runtime ClassCastException
@CwtConfigSelectDsl
fun <T : CwtMemberConfig<*>, R> T.select(scope: CwtConfigSelectScope = CwtConfigSelectScope(), block: context(CwtConfigSelectScope) T.() -> R): R {
    return block.invoke(scope, this@select)
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
inline fun currentContext(): CwtConfigSelectContext {
    return scope.context
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
inline fun updateContext(block: () -> CwtConfigSelectContext): CwtConfigSelectContext {
    return scope.context + block()
}

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
inline fun <T : CwtMemberConfig<*>> Sequence<T>.one(): T? = firstOrNull()

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
inline fun <T : CwtMemberConfig<*>> Sequence<T>.all(): List<T> = toList()

