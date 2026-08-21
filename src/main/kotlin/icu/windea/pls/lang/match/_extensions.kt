@file:Suppress("unused")

package icu.windea.pls.lang.match

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cache.NestedCache
import icu.windea.pls.core.util.KeyProviderWithFactory
import icu.windea.pls.core.util.KeyWithFactory

typealias ParadoxMatchResultNestedCache = NestedCache<VirtualFile, String, ParadoxMatchResult>
typealias ParadoxMatchResultNestedCacheKey = KeyWithFactory<CachedValue<ParadoxMatchResultNestedCache>, CwtConfigGroup>
typealias ParadoxMatchResultNestedCacheKeyProvider = KeyProviderWithFactory<CachedValue<ParadoxMatchResultNestedCache>, CwtConfigGroup>

/**
 * @see ParadoxPatternMatchService.matches
 */
fun String.matchesByPattern(
    key: String,
    element: PsiElement,
    configGroup: CwtConfigGroup,
    options: ParadoxMatchOptions? = null,
    fromIndex: Int = 0,
): Boolean {
    return ParadoxPatternMatchService.matches(this, key, element, configGroup, options, fromIndex)
}

/**
 * @see ParadoxPatternMatchService.find
 */
fun <V> Map<String, V>.findByPattern(
    key: String,
    element: PsiElement,
    configGroup: CwtConfigGroup,
    options: ParadoxMatchOptions? = null,
    fromIndex: Int = 0,
): V? {
    return ParadoxPatternMatchService.find(this, key, element, configGroup, options, fromIndex)
}

/**
 * @see ParadoxPatternMatchService.filter
 */
fun <V> Map<String, V>.filterByPattern(
    key: String,
    element: PsiElement,
    configGroup: CwtConfigGroup,
    options: ParadoxMatchOptions? = null,
    fromIndex: Int = 0,
): Collection<V> {
    return ParadoxPatternMatchService.filter(this, key, element, configGroup, options, fromIndex)
}
