@file:Suppress("unused")

package icu.windea.pls.core.codeInsight.navigation

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import javax.swing.Icon

object NavigationGutterIconBuilderFacade {
    private val defaultConverter: (PsiElement) -> Collection<PsiElement> = { listOf(it) }

    @JvmStatic
    fun createForPsi(
        icon: Icon,
        gotoRelatedItemProvider: ((PsiElement) -> Collection<GotoRelatedItem>)? = null
    ): NavigationGutterIconBuilder<PsiElement> {
        return NavigationGutterIconBuilder.create(icon, defaultConverter, gotoRelatedItemProvider)
    }

    @JvmStatic
    fun <T> create(
        icon: Icon,
        converter: (T) -> Collection<PsiElement>
    ): NavigationGutterIconBuilder<T> {
        return NavigationGutterIconBuilder.create(icon, converter, null)
    }

    @JvmStatic
    fun <T> create(
        icon: Icon,
        converter: (T) -> Collection<PsiElement>,
        gotoRelatedItemProvider: (T) -> Collection<GotoRelatedItem>
    ): NavigationGutterIconBuilder<T> {
        return NavigationGutterIconBuilder.create(icon, converter, gotoRelatedItemProvider)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> NavigationGutterIconBuilder<T>.setTargets(noinline targetsProvider: () -> Collection<T>): NavigationGutterIconBuilder<T> {
    return setTargets(NotNullLazyValue.lazy(targetsProvider))
}
