@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.config.select

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry

class CwtConfigSelectScope(
    val context: CwtConfigSelectContext = CwtConfigSelectContext()
)

class CwtConfigSelectContext : UserDataHolderBase() {
    object Keys : KeyRegistry()

    @JvmInline
    value class Builder(val context: CwtConfigSelectContext) {
        inline operator fun <T> plus(value: T): T = value
    }
}

inline fun <R> selectConfigScope(scope: CwtConfigSelectScope = CwtConfigSelectScope(), block: context(CwtConfigSelectScope) () -> R): R {
    return block.invoke(scope)
}

context(scope: CwtConfigSelectScope)
inline fun currentContext(): CwtConfigSelectContext {
    return scope.context
}

context(scope: CwtConfigSelectScope)
inline fun updateContext(block: CwtConfigSelectContext.Builder.() -> Unit) {
    block(CwtConfigSelectContext.Builder(scope.context))
}
