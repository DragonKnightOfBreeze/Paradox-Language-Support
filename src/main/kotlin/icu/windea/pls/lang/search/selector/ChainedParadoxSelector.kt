package icu.windea.pls.lang.search.selector

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*

class ChainedParadoxSelector<T>(
    val project: Project,
    val context: Any? = null,
) : ParadoxSelector<T> {
    val file = selectFile(context)
    val rootFile = selectRootFile(file)
    
    val gameType by lazy {
        val selectorGameType = selectors.filterIsInstance<ParadoxWithGameTypeSelector<*>>().lastOrNull()?.gameType
        if(selectorGameType != null) return@lazy selectorGameType
        selectGameType(context)
    }
    
    val settings: ParadoxGameOrModSettingsState? by lazy {
        val rootInfo = file?.fileInfo?.rootInfo
        when {
            rootInfo is ParadoxGameRootInfo -> getProfilesSettings().gameSettings.get(rootInfo.rootFile.path)
            rootInfo is ParadoxModRootInfo -> getProfilesSettings().modSettings.get(rootInfo.rootFile.path)
            else -> null
        }
    }
    
    val defaultScope: GlobalSearchScope by lazy {
        ParadoxSearchScope.fromFile(project, file) ?: GlobalSearchScope.allScope(project)
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
    
    override fun select(target: T): Boolean {
        if(!matchesGameType(target)) return false
        if(selectors.isEmpty()) return super.select(target)
        var finalSelectResult = true
        var finalSelectDefaultResult = true
        var finalDefaultValuePriority = 0
        selectors.forEach { selector ->
            val selectResult = selector.select(target)
            finalSelectResult = finalSelectResult && selectResult
            if(selectResult) finalDefaultValuePriority++
            finalSelectDefaultResult = finalSelectDefaultResult && (selectResult || selector.selectAll(target))
        }
        if(finalSelectDefaultResult) {
            if(defaultValuePriority == 0 || defaultValuePriority < finalDefaultValuePriority) {
                synchronized(defaultValueLock) {
                    if(defaultValuePriority == 0 || defaultValuePriority < finalDefaultValuePriority) {
                        defaultValue = target
                        defaultValuePriority = finalDefaultValuePriority
                    }
                }
            }
        }
        return finalSelectResult
    }
    
    override fun selectAll(target: T): Boolean {
        if(!matchesGameType(target)) return false
        if(selectors.isEmpty()) return super.selectAll(target)
        selectors.forEach { selector ->
            if(!selector.selectAll(target)) return false
        }
        return true
    }
    
    /**
     * 注意：最终使用的排序器需要将比较结果为0的项按照原有顺序进行排序，除非它们值相等。
     */
    override fun comparator(): Comparator<T>? {
        if(selectors.isEmpty()) return super.comparator()
        var comparator: Comparator<T>? = null
        selectors.forEach { selector ->
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

