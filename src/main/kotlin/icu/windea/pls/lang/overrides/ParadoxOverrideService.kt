package icu.windea.pls.lang.overrides

import icu.windea.pls.PlsFacade
import icu.windea.pls.ep.overrides.ParadoxOverrideStrategyProvider
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxSearchParameters
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.settings.ParadoxModSettingsState
import icu.windea.pls.model.ParadoxRootInfo

object ParadoxOverrideService {
    /**
     * 从目标（文件、封装变量、定义、本地化、复杂枚举等）得到覆盖方式。
     * 默认使用 [ParadoxOverrideStrategy.LIOS]。
     */
    fun getOverrideStrategy(target: Any): ParadoxOverrideStrategy {
        return ParadoxOverrideStrategyProvider.get(target) ?: ParadoxOverrideStrategy.LIOS
    }

    /**
     * 从目标（文件、封装变量、定义、本地化、复杂枚举等）的查询参数得到覆盖方式。
     * 默认使用 [ParadoxOverrideStrategy.LIOS]。
     */
    fun getOverrideStrategy(searchParameters: ParadoxSearchParameters<*>): ParadoxOverrideStrategy {
        return ParadoxOverrideStrategyProvider.get(searchParameters) ?: ParadoxOverrideStrategy.LIOS
    }

    /**
     * 得到基于覆盖顺序的目标的排序器。
     */
    fun <T> getOverrideComparator(searchParameters: ParadoxSearchParameters<T>): ParadoxOverrideComparator<T> {
        val overrideStrategy = getOverrideStrategy(searchParameters)
        val rootFile = searchParameters.selector.rootFile
        val rootInfo = rootFile?.fileInfo?.rootInfo
        val settings = when (rootInfo) {
            is ParadoxRootInfo.Game -> PlsFacade.getProfilesSettings().gameSettings.get(rootFile.path)
            is ParadoxRootInfo.Mod -> PlsFacade.getProfilesSettings().modSettings.get(rootFile.path)
            else -> null
        }
        return ParadoxOverrideComparator(searchParameters, overrideStrategy, settings)
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
