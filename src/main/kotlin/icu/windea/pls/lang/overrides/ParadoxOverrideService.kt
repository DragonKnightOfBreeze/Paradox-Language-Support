package icu.windea.pls.lang.overrides

import icu.windea.pls.ep.overrides.ParadoxOverrideStrategyProvider
import icu.windea.pls.lang.search.ParadoxSearchParameters
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.settings.ParadoxModSettingsState

object ParadoxOverrideService {
    /**
     * 得到目标（文件、封装变量、定义、本地化等）使用的覆盖方式。
     * 如果返回值为 `null`，则表示不适用覆盖策略。
     */
    fun getOverrideStrategy(target: Any): ParadoxOverrideStrategy? {
        return ParadoxOverrideStrategyProvider.get(target)
    }

    /**
     * 从查询参数得到目标（文件、封装变量、定义、本地化等）使用的覆盖方式。
     * 如果返回值为 `null`，则表示不适用覆盖策略。
     */
    fun getOverrideStrategy(searchParameters: ParadoxSearchParameters<*>): ParadoxOverrideStrategy? {
        return ParadoxOverrideStrategyProvider.get(searchParameters)
    }

    /**
     * 得到基于覆盖顺序的目标的排序器。
     */
    fun <T> getOverrideComparator(searchParameters: ParadoxSearchParameters<T>): ParadoxOverrideComparator<T> {
        return ParadoxOverrideComparator(searchParameters)
    }

    /**
     * 得到根目录路径 [rootPath] 在当前上下文中的顺序。
     */
    fun getOrderInContext(rootPath: String, settings: ParadoxGameOrModSettingsState): Int {
        if (rootPath == settings.gameDirectory) return 0
        val i = settings.modDependencies.indexOfFirst { it.modDirectory == rootPath }
        if (i != -1) return i + 1
        if (settings is ParadoxModSettingsState && rootPath == settings.modDirectory) return Int.MAX_VALUE
        return -1
    }
}
