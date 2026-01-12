@file:Suppress("unused", "RedundantWith", "UnusedReceiverParameter", "NOTHING_TO_INLINE")

package icu.windea.pls.lang.psi.select

import com.intellij.psi.PsiElement

@DslMarker
annotation class ParadoxPsiSelectDsl

@ParadoxPsiSelectDsl
inline fun <R> selectScope(
    scope: ParadoxPsiSelectScope = ParadoxPsiSelectScope(),
    block: context(ParadoxPsiSelectScope) () -> R
): R = with(scope) { block() }

@ParadoxPsiSelectDsl
inline fun <T : PsiElement, R> T.select(
    scope: ParadoxPsiSelectScope = ParadoxPsiSelectScope(),
    block: context(ParadoxPsiSelectScope) T.() -> R
): R = with(scope) { block() }

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
inline fun <T : PsiElement> Sequence<T>.one(): T? = firstOrNull()

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
inline fun <T : PsiElement> Sequence<T>.all(): List<T> = toList()
