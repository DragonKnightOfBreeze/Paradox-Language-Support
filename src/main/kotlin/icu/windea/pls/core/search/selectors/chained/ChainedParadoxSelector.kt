package icu.windea.pls.core.search.selectors.chained

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.scopes.*
import icu.windea.pls.core.search.selectors.*
import icu.windea.pls.lang.model.*

open class ChainedParadoxSelector<T>(
    val project: Project,
    val context: Any? = null
) : ParadoxSelector<T> {
    val file = selectFile(context)
    val fileInfo = file?.fileInfo
    val rootInfo = fileInfo?.rootInfo
    val rootFile = rootInfo?.rootFile
    val gameType = rootInfo?.gameType
    
    val settings = when {
        rootInfo is ParadoxGameRootInfo -> getProfilesSettings().gameSettings.get(rootInfo.rootFile.path)
        rootInfo is ParadoxModRootInfo -> getProfilesSettings().modSettings.get(rootInfo.rootFile.path)
        else -> null
    }
    
    val defaultScope: GlobalSearchScope by lazy {
        ParadoxGlobalSearchScope.fromFile(project, file, fileInfo)
    }
    val scope: GlobalSearchScope by lazy {
        selectors.findIsInstance<ParadoxSearchScopeAwareSelector<*>>()?.getGlobalSearchScope()
            ?: defaultScope
    }
    
    val selectors = mutableListOf<ParadoxSelector<T>>()
    
    var defaultValue: T? = null
    var defaultValuePriority = 0
    
    override fun select(result: T): Boolean {
        if(!matchesGameType(result)) return false
        if(selectors.isEmpty()) return super.select(result)
        var finalSelectResult = true
        var finalSelectDefaultResult = true
        var finalDefaultValuePriority = 0
        for(selector in selectors) {
            val selectResult = selector.select(result)
            finalSelectResult = finalSelectResult && selectResult
            if(selectResult) finalDefaultValuePriority++
            finalSelectDefaultResult = finalSelectDefaultResult && (selectResult || selector.selectAll(result))
        }
        if(finalSelectDefaultResult && defaultValuePriority < finalDefaultValuePriority) {
            defaultValue = result
            defaultValuePriority = finalDefaultValuePriority
        }
        return finalSelectResult
    }
    
    override fun selectAll(result: T): Boolean {
        if(!matchesGameType(result)) return false
        if(selectors.isEmpty()) return super.selectAll(result)
        var finalSelectAllResult = true
        for(selector in selectors) {
            val selectAllResult = selector.selectAll(result)
            finalSelectAllResult = finalSelectAllResult && selectAllResult
        }
        return finalSelectAllResult
    }
    
    private fun matchesGameType(result: T): Boolean {
        //某些情况下，可以直接认为游戏类型是匹配的
        if(scope === defaultScope) return true
        
        return gameType == null || gameType == selectGameType(result)
    }
    
    override fun comparator(): Comparator<T>? {
        if(selectors.isEmpty()) return super.comparator()
        var comparator: Comparator<T>? = null
        for(paradoxSelector in selectors) {
            val nextComparator = paradoxSelector.comparator() ?: continue
            if(comparator == null) {
                comparator = nextComparator
            } else {
                comparator = comparator.thenComparing(nextComparator)
            }
        }
        //最终使用的排序器需要将比较结果为0的项按照原有顺序进行排序，除非它们值相等
        return comparator?.thenComparing { a, b ->
            if(a == b) 0 else 1
        }
    }
}

fun <S : ChainedParadoxSelector<T>, T> S.withSearchScope(scope: GlobalSearchScope): S {
    selectors += ParadoxWithSearchScopeSelector(scope)
    return this
}

fun <S : ChainedParadoxSelector<T>, T : PsiElement> S.withSearchScopeType(searchScopeType: String?, context: PsiElement): S {
    if(searchScopeType != null) selectors += ParadoxWithSearchScopeTypeSelector(searchScopeType, context)
    return this
}

/**
 * 首先尝试选用同一根目录下的，然后尝试选用同一文件下的。
 */
@JvmOverloads
fun <S : ChainedParadoxSelector<T>, T> S.contextSensitive(condition: Boolean = true): S {
    if(condition) {
        if(rootFile != null) selectors += ParadoxPreferRootFileSelector(rootFile)
        //if(file != null) selectors += ParadoxPreferFileSelector(file)
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
