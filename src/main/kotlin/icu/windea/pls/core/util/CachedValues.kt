package icu.windea.pls.core.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.core.runSmartReadAction

// @Suppress("NOTHING_TO_INLINE")
// inline fun <T> T.withDependencyItems(vararg dependencies: Any): CachedValueProvider.Result<T> {
//     return CachedValueProvider.Result.create(this, *dependencies)
// }
//
// @Suppress("NOTHING_TO_INLINE")
// inline fun <T> T.withDependencyItems(dependencies: List<Any>): CachedValueProvider.Result<T> {
//     return CachedValueProvider.Result.create(this, dependencies)
// }

fun <T> createCachedValue(
    project: Project,
    trackValue: Boolean = false,
    provider: CachedValueProvider<T>,
): CachedValue<T> {
    return CachedValuesManager.getManager(project).createCachedValue(provider, trackValue)
}

fun <T> getCachedValue(
    context: PsiElement,
    key: Key<CachedValue<T>>,
    provider: CachedValueProvider<T>,
): T {
    return CachedValuesManager.getCachedValue(context, key, provider)
}

fun <T> getCachedValueOnDemand(
    context: PsiElement,
    key: Key<CachedValue<T>>,
    onDemand: Boolean = true,
    provider: () -> CachedValueProvider.Result<T>,
): T {
    if (!onDemand) return runSmartReadAction { provider().value }
    return CachedValuesManager.getCachedValue(context, key) { runSmartReadAction { provider() } }
}
