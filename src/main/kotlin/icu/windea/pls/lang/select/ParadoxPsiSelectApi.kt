@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.lang.select

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry

class ParadoxPsiSelectScope(
    val context: ParadoxPsiSelectContext = ParadoxPsiSelectContext()
)

class ParadoxPsiSelectContext : UserDataHolderBase() {
    object Keys : KeyRegistry()

    @JvmInline
    value class Builder(val context: ParadoxPsiSelectContext) {
        inline operator fun <T> plus(value: T): T = value
    }
}

inline fun <R> selectScope(scope: ParadoxPsiSelectScope = ParadoxPsiSelectScope(), block: context(ParadoxPsiSelectScope) () -> R): R {
    return block.invoke(scope)
}

context(scope: ParadoxPsiSelectScope)
inline fun currentContext(): ParadoxPsiSelectContext {
    return scope.context
}

context(scope: ParadoxPsiSelectScope)
inline fun updateContext(block: ParadoxPsiSelectContext.Builder.() -> Unit) {
    block(ParadoxPsiSelectContext.Builder(scope.context))
}
