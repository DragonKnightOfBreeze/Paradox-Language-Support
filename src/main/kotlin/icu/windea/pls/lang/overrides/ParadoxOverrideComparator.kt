package icu.windea.pls.lang.overrides

import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxSearchParameters
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxRootInfo

/**
 * 基于覆盖顺序的目标的排序器。
 *
 * 用于对查询结果进行排序。比较结果可能返回0。
 *
 * 说明：
 * - 文件路径相同时，按照所在根目录在当前上下文中的顺序，由大到小排序。
 * - 文件路径不同时，基于覆盖方式进行排序（对于 `LIOS` 和 `DUPL`，后到者优先）。
 *
 * @see ParadoxOverrideStrategy
 * @see ParadoxOverrideService
 * @see icu.windea.pls.lang.search.ParadoxQueryImpl
 */
class ParadoxOverrideComparator<T>(
    val searchParameters: ParadoxSearchParameters<T>
) : Comparator<T> {
    val overrideStrategy by lazy { ParadoxOverrideService.getOverrideStrategy(searchParameters) }
    val settings by lazy { computeSettings() }

    private fun computeSettings(): ParadoxGameOrModSettingsState? {
        val rootFile = searchParameters.selector.rootFile
        val rootInfo = rootFile?.fileInfo?.rootInfo
        return when (rootInfo) {
            is ParadoxRootInfo.Game -> PlsProfilesSettings.getInstance().state.gameSettings.get(rootFile.path)
            is ParadoxRootInfo.Mod -> PlsProfilesSettings.getInstance().state.modSettings.get(rootFile.path)
            else -> null
        }
    }

    override fun compare(o1: T?, o2: T?): Int {
        val file1 = selectFile(o1) ?: return 1
        val file2 = selectFile(o2) ?: return -1
        if (file1 == file2) return 0
        val fileInfo1 = file1.fileInfo ?: return 1
        val fileInfo2 = file2.fileInfo ?: return -1
        val path1 = fileInfo1.path.path
        val path2 = fileInfo2.path.path
        val pathResult = path1.compareTo(path2)
        if (pathResult == 0) {
            val settings = settings ?: return 0
            val order1 = getOrderInContext(fileInfo1, settings)
            val order2 = getOrderInContext(fileInfo2, settings)
            val orderResult = order1.compareTo(order2)
            if (orderResult == 0) return 0
            // 文件路径相同时，按照在设置中的顺序，由大到小排序
            return -orderResult
        }
        val overrideStrategy = overrideStrategy ?: return 0
        // 文件路径不同时，基于覆盖方式进行排序（对于 `LIOS` 和 `DUPL`，后到者优先）
        return when (overrideStrategy) {
            ParadoxOverrideStrategy.FIOS -> pathResult
            ParadoxOverrideStrategy.LIOS -> -pathResult
            ParadoxOverrideStrategy.DUPL -> -pathResult
            ParadoxOverrideStrategy.ORDERED -> pathResult
        }
    }

    private fun getOrderInContext(fileInfo: ParadoxFileInfo, settings: ParadoxGameOrModSettingsState): Int {
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return -1
        val rootPath = rootInfo.rootFile.path
        return ParadoxOverrideService.getOrderInContext(rootPath, settings)
    }
}
