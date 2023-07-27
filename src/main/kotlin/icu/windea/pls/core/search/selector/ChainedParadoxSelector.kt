package icu.windea.pls.core.search.selector

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.model.*

open class ChainedParadoxSelector<T>(
    val project: Project,
    val context: Any? = null,
) : ParadoxSelector<T> {
    val file = selectFile(context)
    val rootFile = selectRootFile(file)
    
    val gameType by lazy {
        val selectorGameType = selectors.filterIsInstance<ParadoxWithGameTypeSelector<*>>().lastOrNull()?.gameType
        when {
            selectorGameType == null -> selectGameType(rootFile)
            else -> selectorGameType
        }
    }
    
    val settings: ParadoxGameOrModSettingsState? by lazy {
        val rootInfo = rootFile?.fileInfo?.rootInfo
        when {
            rootInfo is ParadoxGameRootInfo -> getProfilesSettings().gameSettings.get(rootInfo.rootFile.path)
            rootInfo is ParadoxModRootInfo -> getProfilesSettings().modSettings.get(rootInfo.rootFile.path)
            else -> null
        }
    }
    
    val defaultScope: GlobalSearchScope by lazy {
        ParadoxSearchScope.fromFile(project, file)
    }
    
    val scope: GlobalSearchScope by lazy {
        val selectorScopes = selectors.filterIsInstance<ParadoxSearchScopeAwareSelector<*>>().mapNotNull { it.getGlobalSearchScope() }
        when {
            selectorScopes.isEmpty() -> defaultScope
            selectorScopes.size == 1 -> selectorScopes[0]
            else -> selectorScopes.reduce { a, b -> a.intersectWith(b) }
        }
    }
    
    val selectors = mutableListOf<ParadoxSelector<T>>()
    
    private var defaultValue: T? = null
    private var defaultValuePriority = 0
    private var defaultValueLock = Any()
    
    override fun select(result: T): Boolean {
        if(!matchesGameType(result)) return false
        if(selectors.isEmpty()) return super.select(result)
        var finalSelectResult = true
        var finalSelectDefaultResult = true
        var finalDefaultValuePriority = 0
        selectors.forEachFast { selector ->
            val selectResult = selector.select(result)
            finalSelectResult = finalSelectResult && selectResult
            if(selectResult) finalDefaultValuePriority++
            finalSelectDefaultResult = finalSelectDefaultResult && (selectResult || selector.selectAll(result))
        }
        if(finalSelectDefaultResult) {
            if(defaultValuePriority == 0 || defaultValuePriority < finalDefaultValuePriority) {
                synchronized(defaultValueLock) {
                    if(defaultValuePriority == 0 || defaultValuePriority < finalDefaultValuePriority) {
                        defaultValue = result
                        defaultValuePriority = finalDefaultValuePriority
                    }
                }
            }
        }
        return finalSelectResult
    }
    
    override fun selectAll(result: T): Boolean {
        if(!matchesGameType(result)) return false
        if(selectors.isEmpty()) return super.selectAll(result)
        selectors.forEachFast { selector ->
            if(!selector.selectAll(result)) return false
        }
        return true
    }
    
    /**
     * 注意：最终使用的排序器需要将比较结果为0的项按照原有顺序进行排序，除非它们值相等。
     */
    override fun comparator(): Comparator<T>? {
        if(selectors.isEmpty()) return super.comparator()
        var comparator: Comparator<T>? = null
        selectors.forEachFast { selector ->
            comparator = comparator thenPossible selector.comparator()
        }
        return comparator
    }
    
    fun defaultValue(): T? {
        return defaultValue
    }
    
    fun resetDefaultValue() {
        defaultValue = null
        defaultValuePriority = 0
    }
    
    fun matchesGameType(result: T): Boolean {
        //某些情况下，可以直接认为游戏类型是匹配的
        if(scope is ParadoxSearchScope) return true
        
        return gameType == null || gameType == selectGameType(result)
    }
}

