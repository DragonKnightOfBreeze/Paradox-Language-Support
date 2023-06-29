package icu.windea.pls.core.search.selector.chained

import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.model.*

fun <S : ChainedParadoxSelector<T>, T> S.withGameType(gameType: ParadoxGameType?): S {
    if(gameType != null) selectors += ParadoxWithGameTypeSelector(gameType)
    return this
}

fun <S : ChainedParadoxSelector<T>, T> S.withSearchScope(searchScope: GlobalSearchScope?): S {
    if(searchScope != null) selectors += ParadoxWithSearchScopeSelector(searchScope)
    return this
}

fun <S : ChainedParadoxSelector<T>, T> S.withSearchScopeType(searchScopeType: String?): S {
    if(searchScopeType != null) selectors += ParadoxWithSearchScopeTypeSelector(searchScopeType, project, context)
    return this
}

/**
 * 首先尝试选用同一根目录下的，然后尝试选用同一文件下的。
 */
fun <S : ChainedParadoxSelector<T>, T> S.contextSensitive(condition: Boolean = true): S {
    if(condition) {
        if(rootFile != null) selectors += ParadoxPreferRootFileSelector(rootFile)
        if(file != null) selectors += ParadoxPreferFileSelector(file)
    }
    return this
}

fun <S : ChainedParadoxSelector<T>, T, K> S.distinctBy(keySelector: (T) -> K): S {
    selectors += ParadoxDistinctSelector(keySelector)
    return this
}

fun <S : ChainedParadoxSelector<T>, T> S.filterBy(predicate: (T) -> Boolean): S {
    selectors += ParadoxFilterSelector(predicate)
    return this
}

fun <S : ChainedParadoxSelector<T>, T : PsiElement> S.notSamePosition(element: PsiElement?): S {
    filterBy { element == null || !element.isSamePosition(it) }
    return this
}

//fun <S : ChainedParadoxSelector<T>, T : PsiElement> S.useIndexKey(indexKey: StubIndexKey<String, T>): S {
//    selectors += ParadoxWithIndexKeySelector(indexKey)
//    return this
//}
//
//fun <S : ChainedParadoxSelector<T>, T : PsiElement> S.getIndexKey(): StubIndexKey<String, T>? {
//    return selectors.findIsInstance<ParadoxWithIndexKeySelector<T>>()?.indexKey
//}