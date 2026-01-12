package icu.windea.pls.lang.search.selector

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.core.thenPossible
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.model.ParadoxRootInfo
import java.util.function.Function

/**
 * 查询选择器。由多个选择器（[ParadoxSelector]）组合而成。
 *
 * 用于获取需要的查询上下文信息（例如游戏类型、查询作用域），
 * 以及对查询目标的最终的选择逻辑（例如是否上下文敏感、是否去重）。
 */
class ParadoxSearchSelector<T>(
    val project: Project,
    val context: Any? = null
) : ParadoxSelector<T> {
    val selectors = mutableListOf<ParadoxSelector<T>>()

    val file by lazy { selectFile(context) }
    val rootFile by lazy { selectRootFile(file) }

    val gameType by lazy {
        val selectorGameType = selectors.filterIsInstance<ParadoxWithGameTypeSelector<T>>().lastOrNull()?.gameType
        if (selectorGameType != null) return@lazy selectorGameType
        selectGameType(context)
    }
    val settings: ParadoxGameOrModSettingsState? by lazy {
        val rootInfo = file?.fileInfo?.rootInfo
        when (rootInfo) {
            is ParadoxRootInfo.Game -> PlsProfilesSettings.getInstance().state.gameSettings.get(rootInfo.rootFile.path)
            is ParadoxRootInfo.Mod -> PlsProfilesSettings.getInstance().state.modSettings.get(rootInfo.rootFile.path)
            else -> null
        }
    }
    val defaultScope: GlobalSearchScope by lazy {
        ParadoxSearchScope.fromFile(project, file) ?: ParadoxSearchScope.allScope(project, file)
    }
    val scope: GlobalSearchScope by lazy {
        // NOTE 这里需要保证适用 `ParadoxFileManager.canReference()`
        val selectorScopes = selectors.filterIsInstance<ParadoxSearchScopeAwareSelector<*>>().mapNotNull { it.getGlobalSearchScope() }
        val mergedScope = when {
            selectorScopes.isEmpty() -> defaultScope
            selectorScopes.size == 1 -> selectorScopes[0].intersectWith(ParadoxSearchScope.allScope(project, file))
            else -> selectorScopes.reduce { a, b -> a.intersectWith(b) }.intersectWith(ParadoxSearchScope.allScope(project, file))
        }
        mergedScope
    }

    private var defaultValue: T? = null
    private var defaultValuePriority = 0
    private val defaultValueLock = Any()

    fun getDefaultValue(): T? {
        return defaultValue
    }

    fun resetDefaultValue() {
        defaultValue = null
        defaultValuePriority = 0
    }

    fun matchesGameType(result: T): Boolean {
        // 某些情况下，可以直接认为游戏类型是匹配的
        val scope = scope
        if (scope is ParadoxSearchScope && scope.ensureMatchGameType()) return true

        return gameType == null || gameType == selectGameType(result)
    }

    override fun selectOne(target: T): Boolean {
        if (!matchesGameType(target)) return false
        if (selectors.isEmpty()) return true
        var finalSelectResult = true
        var finalSelectDefaultResult = true
        var finalDefaultValuePriority = 0
        selectors.forEach { selector ->
            val selectResult = selector.selectOne(target)
            finalSelectResult = finalSelectResult && selectResult
            if (selectResult) finalDefaultValuePriority++
            finalSelectDefaultResult = finalSelectDefaultResult && (selectResult || selector.select(target))
        }
        if (finalSelectDefaultResult) {
            if (defaultValuePriority == 0 || defaultValuePriority < finalDefaultValuePriority) {
                synchronized(defaultValueLock) {
                    if (defaultValuePriority == 0 || defaultValuePriority < finalDefaultValuePriority) {
                        defaultValue = target
                        defaultValuePriority = finalDefaultValuePriority
                    }
                }
            }
        }
        return finalSelectResult
    }

    override fun select(target: T): Boolean {
        if (!matchesGameType(target)) return false
        if (selectors.isEmpty()) return true
        selectors.forEach { selector ->
            if (!selector.select(target)) return false
        }
        return true
    }

    override fun keySelector(): Function<T, Any?>? {
        if (selectors.isEmpty()) return null
        // use merged key selector
        val selectors = selectors.mapNotNull { s -> s.keySelector() }
        return when (selectors.size) {
            0 -> null
            1 -> selectors.first()
            else -> Function { selectors.map { s -> s.apply(it) } }
        }
    }

    override fun comparator(): Comparator<T>? {
        if (selectors.isEmpty()) return null
        // use merged comparator
        var comparator: Comparator<T>? = null
        selectors.forEach { selector ->
            comparator = comparator thenPossible selector.comparator()
        }
        return comparator
    }
}
