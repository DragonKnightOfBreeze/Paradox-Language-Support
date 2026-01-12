@file:Suppress("unused", "RedundantWith", "UnusedReceiverParameter", "NOTHING_TO_INLINE")

package icu.windea.pls.config.select

import icu.windea.pls.config.config.CwtMemberConfig

@DslMarker
annotation class CwtConfigSelectDsl

@CwtConfigSelectDsl
inline fun <R> selectScope(
    scope: CwtConfigSelectScope = CwtConfigSelectScope(),
    block: context(CwtConfigSelectScope) () -> R
): R = with(scope) { block() }

@CwtConfigSelectDsl
inline fun <T : CwtMemberConfig<*>, R> T.select(
    scope: CwtConfigSelectScope = CwtConfigSelectScope(),
    block: context(CwtConfigSelectScope) T.() -> R
): R = with(scope) { block() }

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
inline fun <T : CwtMemberConfig<*>> Sequence<T>.one(): T? = firstOrNull()

context(scope: CwtConfigSelectScope)
@CwtConfigSelectDsl
inline fun <T : CwtMemberConfig<*>> Sequence<T>.all(): List<T> = toList()

