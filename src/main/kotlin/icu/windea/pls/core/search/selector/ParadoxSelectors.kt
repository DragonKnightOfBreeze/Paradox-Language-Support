package icu.windea.pls.core.search.selector

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.scope.type.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

class ParadoxWithGameTypeSelector<T>(
    val gameType: ParadoxGameType
) : ParadoxSelector<T> {
    override fun select(result: T): Boolean {
        return selectGameType(result) == gameType
    }
    
    override fun selectAll(result: T): Boolean {
        return selectGameType(result) == gameType
    }
}

interface ParadoxSearchScopeAwareSelector<T> : ParadoxSelector<T> {
    fun getGlobalSearchScope(): GlobalSearchScope?
}

class ParadoxWithSearchScopeSelector<T>(
    val searchScope: GlobalSearchScope
) : ParadoxSearchScopeAwareSelector<T> {
    override fun getGlobalSearchScope(): GlobalSearchScope {
        return searchScope
    }
}

class ParadoxWithSearchScopeTypeSelector<T>(
    searchScopeType: String,
    val project: Project,
    val context: Any?,
) : ParadoxSearchScopeAwareSelector<T> {
    val searchScopeType = ParadoxSearchScopeTypes.get(searchScopeType)
    
    private val root by lazy { this.searchScopeType.findRoot(project, context) }
    
    override fun select(result: T): Boolean {
        val root1 = root
        val root2 = findRoot(result)
        return root1 isSamePosition root2
    }
    
    override fun selectAll(result: T): Boolean {
        return select(result)
    }
    
    fun findRoot(context: Any?): PsiElement? {
        return searchScopeType.findRoot(project, context)
    }
    
    override fun getGlobalSearchScope(): GlobalSearchScope? {
        return searchScopeType.getGlobalSearchScope(project, context)
    }
}

class ParadoxPreferFileSelector<T>(
    val file: VirtualFile
) : ParadoxSelector<T> {
    override fun select(result: T): Boolean {
        return file == selectFile(result)
    }
    
    override fun selectAll(result: T): Boolean {
        return true
    }
    
    override fun comparator(): Comparator<T> {
        return complexCompareBy({ it }, { null }, { file == selectFile(it) })
    }
}

class ParadoxPreferRootFileSelector<T>(
    val rootFile: VirtualFile
) : ParadoxSelector<T> {
    override fun select(result: T): Boolean {
        return rootFile == selectRootFile(result)
    }
    
    override fun selectAll(result: T): Boolean {
        return true
    }
    
    override fun comparator(): Comparator<T> {
        return complexCompareBy({ it }, { null }, { rootFile == selectRootFile(it) })
    }
}

class ParadoxDistinctSelector<T, K>(
    val keySelector: (T) -> K
) : ParadoxSelector<T> {
    val keys = mutableSetOf<K>().synced()
    
    override fun selectAll(result: T): Boolean {
        val key = keySelector(result) ?: return false
        return keys.add(key)
    }
}

class ParadoxFilterSelector<T>(
    val predicate: (T) -> Boolean
) : ParadoxSelector<T> {
    override fun select(result: T): Boolean {
        return predicate(result)
    }
    
    override fun selectAll(result: T): Boolean {
        return predicate(result)
    }
}

class ParadoxLocaleSelector(
    val locale: CwtLocalisationLocaleConfig
) : ParadoxSelector<ParadoxLocalisationProperty> {
    override fun select(result: ParadoxLocalisationProperty): Boolean {
        return locale == result.localeConfig
    }
    
    override fun selectAll(result: ParadoxLocalisationProperty): Boolean {
        return select(result)
    }
}

class ParadoxPreferLocaleSelector(
    val locale: CwtLocalisationLocaleConfig
) : ParadoxSelector<ParadoxLocalisationProperty> {
    override fun select(result: ParadoxLocalisationProperty): Boolean {
        return locale == result.localeConfig
    }
    
    override fun selectAll(result: ParadoxLocalisationProperty): Boolean {
        return true
    }
    
    override fun comparator(): Comparator<ParadoxLocalisationProperty> {
        return complexCompareBy({ it.localeConfig }, { it.id }, { locale == it }) //同时也按照localeId来进行排序
    }
}

//class ParadoxWithIndexKeySelector<T: PsiElement> (
//    val indexKey: StubIndexKey<String, T>
//): ParadoxSelector<T>