@file:Suppress("unused", "RedundantWith", "UnusedReceiverParameter", "NOTHING_TO_INLINE")

package icu.windea.pls.lang.psi.select

import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import icu.windea.pls.core.util.KeyRegistry

@DslMarker
annotation class ParadoxPsiSelectDsl

class ParadoxPsiSelectScope {
    val context = ParadoxPsiSelectContext()
}

class ParadoxPsiSelectContext : UserDataHolderBase() {
    object Keys : KeyRegistry()

    operator fun plus(other: ParadoxPsiSelectContext): ParadoxPsiSelectContext {
        Keys.copy(other, this)
        return this
    }

    inline operator fun <T> plus(value: T): T = value
}

@ParadoxPsiSelectDsl
fun <R> selectScope(scope: ParadoxPsiSelectScope = ParadoxPsiSelectScope(), block: context(ParadoxPsiSelectScope) () -> R): R {
    return block.invoke(scope)
}

// NOTE 2.1.1 cannot be inline or runtime ClassCastException
// @ParadoxPsiSelectDsl
// fun <T : PsiElement, R> T.select(scope: ParadoxPsiSelectScope = ParadoxPsiSelectScope(), block: context(ParadoxPsiSelectScope) T.() -> R): R {
//     return block.invoke(scope, this@select)
// }

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
inline fun currentContext(): ParadoxPsiSelectContext = scope.context

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
inline fun updateContext(block: () -> ParadoxPsiSelectContext): ParadoxPsiSelectContext = scope.context + block()

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
inline fun <T : PsiElement> Sequence<T>.one(): T? = firstOrNull()

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
inline fun <T : PsiElement> Sequence<T>.all(): List<T> = toList()
