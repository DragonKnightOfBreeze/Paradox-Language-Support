package icu.windea.pls.lang.search.util

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.base.settings.ChronicleProfilesSettings
import icu.windea.pls.base.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.core.thenPossible
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.search.scope.withFileExtensions
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import java.util.function.Function

/**
 * 查询选择器。由多个选择器（[ParadoxSelector]）组合而成。
 *
 * 用于获取需要的查询上下文信息（例如游戏类型、查询作用域），
 * 以及对查询目标的最终的选择逻辑（例如是否上下文敏感、是否去重）。
 */
abstract class ParadoxSearchSelector<T>(val project: Project, val context: Any?) : ParadoxSelector<T> {
    val selectors = mutableListOf<ParadoxSelector<T>>()

    val file = selectFile(context)
    val rootFile = selectRootFile(file)

    val gameType by lazy { computeGameType() }
    val settings: ParadoxGameOrModSettingsState? by lazy { computeSettings() }
    val defaultScope: GlobalSearchScope by lazy { computeDefaultScope() }
    val scope: GlobalSearchScope by lazy { computeScope() }

    private fun computeGameType(): ParadoxGameType? {
        val gameTypeFromSelectors = selectors.filterIsInstance<ParadoxWithGameTypeSelector<T>>().lastOrNull()?.gameType
        return gameTypeFromSelectors ?: selectGameType(context)
    }

    private fun computeSettings(): ParadoxGameOrModSettingsState? {
        val rootInfo = file?.fileInfo?.rootInfo
        val profilesSettings = ChronicleProfilesSettings.getInstance().state
        return when (rootInfo) {
            is ParadoxRootInfo.Game -> profilesSettings.gameSettings.get(rootInfo.rootFile.path)
            is ParadoxRootInfo.Mod -> profilesSettings.modSettings.get(rootInfo.rootFile.path)
            else -> null
        }
    }

    private fun computeDefaultScope(): GlobalSearchScope {
        return ParadoxSearchScope.fromFile(project, file) ?: ParadoxSearchScope.allScope(project, file)
    }

    private fun computeScope(): GlobalSearchScope {
        // NOTE 这里需要保证适用 `ParadoxFileManager.canReference()`
        val selectorScopes = selectors.filterIsInstance<ParadoxSearchScopeAwareSelector<*>>().mapNotNull { it.getGlobalSearchScope() }
        val mergedScope = when {
            selectorScopes.isEmpty() -> defaultScope
            selectorScopes.size == 1 -> selectorScopes[0].intersectWith(ParadoxSearchScope.allScope(project, file))
            else -> selectorScopes.reduce { a, b -> a.intersectWith(b) }.intersectWith(ParadoxSearchScope.allScope(project, file))
        }
        var resultScope = mergedScope
        val fileExtensions = selectors.filterIsInstance<ParadoxWithFileExtensionsSelector<T>>().flatMapTo(mutableSetOf()) { it.fileExtensions }
        if (fileExtensions.isNotEmpty()) resultScope = resultScope.withFileExtensions(fileExtensions)
        return resultScope
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
        for (selector in selectors) {
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
        for (selector in selectors) {
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
        for (selector in selectors) {
            comparator = comparator thenPossible selector.comparator()
        }
        return comparator
    }
}
