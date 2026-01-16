@file:Suppress("unused")

package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * @see ParadoxPatternMatchService.matches
 */
fun String.matchesByPattern(
    key: String,
    contextElement: PsiElement,
    configGroup: CwtConfigGroup,
    options: ParadoxMatchOptions? = null,
    fromIndex: Int = 0,
): Boolean {
    return ParadoxPatternMatchService.matches(this, key, contextElement, configGroup, options, fromIndex)
}

/**
 * @see ParadoxPatternMatchService.find
 */
fun <V> Map<String, V>.findByPattern(
    key: String,
    contextElement: PsiElement,
    configGroup: CwtConfigGroup,
    options: ParadoxMatchOptions? = null,
    fromIndex: Int = 0,
): V? {
    return ParadoxPatternMatchService.find(this, key, contextElement, configGroup, options, fromIndex)
}

/**
 * @see ParadoxPatternMatchService.filter
 */
fun <V> Map<String, V>.filterByPattern(
    key: String,
    contextElement: PsiElement,
    configGroup: CwtConfigGroup,
    options: ParadoxMatchOptions? = null,
    fromIndex: Int = 0,
): List<V> {
    return ParadoxPatternMatchService.filter(this, key, contextElement, configGroup, options, fromIndex)
}
