package icu.windea.pls.lang.search.selector

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.isSamePosition
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxIndexConstraint

fun <S : ParadoxSearchSelector<T>, T> S.withGameType(gameType: ParadoxGameType?): S {
    if (gameType != null) selectors += ParadoxWithGameTypeSelector(gameType)
    return this
}

fun <S : ParadoxSearchSelector<T>, T> S.withSearchScope(searchScope: GlobalSearchScope?): S {
    if (searchScope != null) selectors += ParadoxWithSearchScopeSelector(searchScope)
    return this
}

fun <S : ParadoxSearchSelector<T>, T> S.withSearchScopeType(searchScopeType: String?): S {
    if (searchScopeType != null) selectors += ParadoxWithSearchScopeTypeSelector(searchScopeType, project, context)
    return this
}

/**
 * 首先尝试选用同一根目录下的，然后尝试选用同一文件下的。
 */
fun <S : ParadoxSearchSelector<T>, T> S.contextSensitive(condition: Boolean = true): S {
    if (condition) {
        if (rootFile != null) selectors += ParadoxPreferRootFileSelector(rootFile)
        if (file != null) selectors += ParadoxPreferFileSelector(file)
    }
    return this
}

fun <S : ParadoxSearchSelector<T>, T> S.filterBy(predicate: (T) -> Boolean): S {
    selectors += ParadoxFilterSelector(predicate)
    return this
}

fun <S : ParadoxSearchSelector<T>, T, K> S.distinctBy(keySelector: (T) -> K): S {
    selectors += ParadoxDistinctSelector(keySelector)
    return this
}

fun <S : ParadoxSearchSelector<T>, T : PsiElement> S.notSamePosition(element: PsiElement?): S {
    return filterBy { element == null || !element.isSamePosition(it) }
}

fun <S : ParadoxSearchSelector<T>, T : PsiElement> S.withConstraint(constraint: ParadoxIndexConstraint<T>?): S {
    if (constraint != null) selectors += ParadoxWithConstraintSelector(constraint)
    return this
}

fun <S : ParadoxSearchSelector<T>, T : PsiElement> S.getConstraint(): ParadoxIndexConstraint<T>? {
    return selectors.findIsInstance<ParadoxWithConstraintSelector<T>>()?.constraint
}

fun <S : ParadoxSearchSelector<ParadoxLocalisationProperty>> S.locale(locale: CwtLocaleConfig?): S {
    if (locale != null) selectors += ParadoxLocaleSelector(locale)
    return this
}

fun <S : ParadoxSearchSelector<ParadoxLocalisationProperty>> S.preferLocale(locale: CwtLocaleConfig?, condition: Boolean = true): S {
    if (locale != null && condition) selectors += ParadoxPreferLocaleSelector(locale)
    return this
}

fun <S : ParadoxSearchSelector<VirtualFile>> S.withFileExtensions(fileExtensions: Set<String>): S {
    if (fileExtensions.isEmpty()) return this
    return filterBy { it.extension?.let { e -> ".$e" }.orEmpty() in fileExtensions }
}
