package icu.windea.pls.lang.search.selector

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.scope.type.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

class ParadoxWithGameTypeSelector<T>(
    val gameType: ParadoxGameType
) : ParadoxSelector<T> {
    override fun selectOne(target: T): Boolean {
        return selectGameType(target) == gameType
    }

    override fun select(target: T): Boolean {
        return selectGameType(target) == gameType
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

    override fun selectOne(target: T): Boolean {
        val root1 = root
        val root2 = findRoot(target)
        return root1 isSamePosition root2
    }

    override fun select(target: T): Boolean {
        return selectOne(target)
    }

    private fun findRoot(context: Any?): PsiElement? {
        return searchScopeType.findRoot(project, context)
    }

    override fun getGlobalSearchScope(): GlobalSearchScope? {
        return searchScopeType.getGlobalSearchScope(project, context)
    }
}

class ParadoxPreferFileSelector<T>(
    val file: VirtualFile
) : ParadoxSelector<T> {
    override fun selectOne(target: T): Boolean {
        return file == selectFile(target)
    }

    override fun select(target: T): Boolean {
        return true
    }

    override fun comparator(): Comparator<T> {
        return complexCompareBy({ it }, { null }, { file == selectFile(it) })
    }
}

class ParadoxPreferRootFileSelector<T>(
    val rootFile: VirtualFile
) : ParadoxSelector<T> {
    override fun selectOne(target: T): Boolean {
        return rootFile == selectRootFile(target)
    }

    override fun select(target: T): Boolean {
        return true
    }

    override fun comparator(): Comparator<T> {
        return complexCompareBy({ it }, { null }, { rootFile == selectRootFile(it) })
    }
}

class ParadoxFilterSelector<T>(
    val predicate: (T) -> Boolean
) : ParadoxSelector<T> {
    override fun selectOne(target: T): Boolean {
        return predicate(target)
    }

    override fun select(target: T): Boolean {
        return predicate(target)
    }
}

class ParadoxDistinctSelector<T, K>(
    val keySelector: (T) -> K
) : ParadoxSelector<T> {
    val keysToDistinct = mutableSetOf<K>().synced()

    override fun selectOne(target: T): Boolean {
        return keysToDistinct.add(keySelector(target))
    }
    
    override fun select(target: T): Boolean {
        return keysToDistinct.add(keySelector(target))
    }
}

class ParadoxLocaleSelector(
    val locale: CwtLocalisationLocaleConfig
) : ParadoxSelector<ParadoxLocalisationProperty> {
    override fun selectOne(target: ParadoxLocalisationProperty): Boolean {
        return locale == selectLocale(target)
    }

    override fun select(target: ParadoxLocalisationProperty): Boolean {
        return selectOne(target)
    }
}

class ParadoxPreferLocaleSelector(
    val locale: CwtLocalisationLocaleConfig
) : ParadoxSelector<ParadoxLocalisationProperty> {
    override fun selectOne(target: ParadoxLocalisationProperty): Boolean {
        return locale == selectLocale(target)
    }

    override fun select(target: ParadoxLocalisationProperty): Boolean {
        return true
    }

    override fun comparator(): Comparator<ParadoxLocalisationProperty> {
        return complexCompareBy({ selectLocale(it) }, { it.id }, { locale == it }) //同时也按照localeId来进行排序
    }
}

//class ParadoxWithIndexKeySelector<T: PsiElement> (
//    val indexKey: StubIndexKey<String, T>
//): ParadoxSelector<T>
