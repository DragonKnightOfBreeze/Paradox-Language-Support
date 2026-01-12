@file:Suppress("unused", "RedundantWith", "UnusedReceiverParameter")

package icu.windea.pls.config.select

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.util.KeyRegistry

@DslMarker
annotation class CwtConfigSelectDsl

class CwtConfigSelectScope : UserDataHolderBase() {
    object Keys : KeyRegistry()

    operator fun plus(other: CwtConfigSelectScope) {
        Keys.copy(other, this, ifPresent = false)
    }
}

@CwtConfigSelectDsl
inline fun <R> withSelect(
    scope: CwtConfigSelectScope = CwtConfigSelectScope(),
    block: context(CwtConfigSelectScope) () -> R
): R {
    return with(scope) { block() }
}

@CwtConfigSelectDsl
inline fun <R> withSelectOne(
    scope: CwtConfigSelectScope = CwtConfigSelectScope(),
    block: context(CwtConfigSelectScope) () -> Sequence<R>
): R? {
    return with(scope) { block().firstOrNull() }
}

@CwtConfigSelectDsl
inline fun <T : CwtMemberConfig<*>, R> T.select(
    scope: CwtConfigSelectScope = CwtConfigSelectScope(),
    block: context(CwtConfigSelectScope) T.() -> R
): R {
    return with(scope) { block() }
}

@CwtConfigSelectDsl
inline fun <T : CwtMemberConfig<*>, R> T.selectOne(
    scope: CwtConfigSelectScope = CwtConfigSelectScope(),
    block: context(CwtConfigSelectScope) T.() -> Sequence<R>
): R? {
    return with(scope) { block().firstOrNull() }
}
