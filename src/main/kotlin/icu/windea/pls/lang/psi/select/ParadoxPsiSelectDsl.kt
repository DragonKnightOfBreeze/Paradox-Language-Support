@file:Suppress("unused", "RedundantWith", "UnusedReceiverParameter", "NOTHING_TO_INLINE")

package icu.windea.pls.lang.psi.select

import com.intellij.psi.PsiElement

@DslMarker
annotation class ParadoxPsiSelectDsl

@ParadoxPsiSelectDsl
inline fun <R> selectScope(
    scope: ParadoxPsiSelectScope = ParadoxPsiSelectScope(),
    block: context(ParadoxPsiSelectScope) () -> R
): R = block.invoke(scope)

// NOTE 2.1.1 cannot be inline or runtime ClassCastException
@ParadoxPsiSelectDsl
fun <T : PsiElement, R> T.select(
    scope: ParadoxPsiSelectScope = ParadoxPsiSelectScope(),
    block: context(ParadoxPsiSelectScope) T.() -> R
): R = block.invoke(scope, this@select)

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
inline fun <T : PsiElement> Sequence<T>.one(): T? = firstOrNull()

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
inline fun <T : PsiElement> Sequence<T>.all(): List<T> = toList()
