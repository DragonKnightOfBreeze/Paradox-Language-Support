@file:Suppress("unused", "RedundantWith", "UnusedReceiverParameter", "NOTHING_TO_INLINE")

package icu.windea.pls.config.select

import icu.windea.pls.config.config.CwtMemberConfig

@DslMarker
annotation class CwtConfigSelectDsl

@CwtConfigSelectDsl
inline fun <R> selectScope(
    scope: CwtConfigSelectScope = CwtConfigSelectScope(),
    block: context(CwtConfigSelectScope) () -> R
): R = block.invoke(scope)

// NOTE 2.1.1 cannot be inline or runtime ClassCastException
@CwtConfigSelectDsl
fun <T : CwtMemberConfig<*>, R> T.select(
    scope: CwtConfigSelectScope = CwtConfigSelectScope(),
    block: context(CwtConfigSelectScope) T.() -> R
): R = block.invoke(scope, this@select)

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
inline fun <T : CwtMemberConfig<*>> Sequence<T>.one(): T? = firstOrNull()

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
inline fun <T : CwtMemberConfig<*>> Sequence<T>.all(): List<T> = toList()

