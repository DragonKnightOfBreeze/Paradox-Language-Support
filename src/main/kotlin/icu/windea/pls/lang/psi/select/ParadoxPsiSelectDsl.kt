@file:Suppress("unused", "RedundantWith", "UnusedReceiverParameter")

package icu.windea.pls.lang.psi.select

import com.intellij.psi.PsiElement

@DslMarker
annotation class ParadoxPsiSelectDsl

class ParadoxPsiSelectScope private constructor(
    val ignoreCase: Boolean = false
) {
    companion object {
        @JvmStatic
        fun from(ignoreCase: Boolean = false): ParadoxPsiSelectScope {
            return ParadoxPsiSelectScope(ignoreCase)
        }
    }
}

@ParadoxPsiSelectDsl
inline fun <R> withSelect(
    ignoreCase: Boolean = false,
    block: context(ParadoxPsiSelectScope) () -> R,
): R {
    val scope = ParadoxPsiSelectScope.from(ignoreCase)
    return with(scope) { block() }
}

@ParadoxPsiSelectDsl
inline fun <R> withSelectOne(
    ignoreCase: Boolean = false,
    block: context(ParadoxPsiSelectScope) () -> Iterable<R>,
): R? {
    val scope = ParadoxPsiSelectScope.from(ignoreCase)
    return with(scope) { block().firstOrNull() }
}

@ParadoxPsiSelectDsl
inline fun <T : PsiElement, R> T.select(
    ignoreCase: Boolean = false,
    block: context(ParadoxPsiSelectScope) T.() -> R,
): R {
    val scope = ParadoxPsiSelectScope.from(ignoreCase)
    return with(scope) { block() }
}

@ParadoxPsiSelectDsl
inline fun <T : PsiElement, R> T.selectOne(
    ignoreCase: Boolean = false,
    block: context(ParadoxPsiSelectScope) T.() -> Iterable<R>,
): R? {
    val scope = ParadoxPsiSelectScope.from(ignoreCase)
    return with(scope) { block().firstOrNull() }
}
