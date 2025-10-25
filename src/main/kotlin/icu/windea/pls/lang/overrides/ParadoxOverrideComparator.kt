package icu.windea.pls.lang.overrides

import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxSearchParameters
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxRootInfo

/**
 * 基于覆盖顺序的目标的排序器。用于对查询结果进行排序。
 *
 * 说明：
 * - 同文件中，后面的项总是会覆盖前面的项。
 * - 文件路径相同时，按照所在根目录在当前上下文中的顺序，由大到小排序。
 * - 文件路径不同时，基于覆盖方式进行排序（对于 `LIOS` 和 `DUPL`，后到者优先）。
 *
 * @see ParadoxOverrideStrategy
 * @see icu.windea.pls.lang.search.ParadoxQuery
 * @see icu.windea.pls.lang.search.ParadoxSearchParameters
 */
class ParadoxOverrideComparator<T>(
    private val searchParameters: ParadoxSearchParameters<T>,
    private val overrideStrategy: ParadoxOverrideStrategy,
    private val settings: ParadoxGameOrModSettingsState?,
) : Comparator<T> {
    override fun compare(o1: T?, o2: T?): Int {
        val file1 = selectFile(o1) ?: return 1
        val file2 = selectFile(o2) ?: return -1
        if (file1 == file2) {
            // 同文件中，后面的项总是会覆盖前面的项
            if (searchParameters is ParadoxFilePathSearch.SearchParameters) return 0
            return 1
        }
        val fileInfo1 = file1.fileInfo ?: return 1
        val fileInfo2 = file2.fileInfo ?: return -1
        val path1 = fileInfo1.path.path
        val path2 = fileInfo2.path.path
        val pathResult = path1.compareTo(path2)
        if (pathResult == 0) {
            if (settings == null) return 1 // 后到者优先
            val order1 = getOrderInContext(fileInfo1, settings)
            val order2 = getOrderInContext(fileInfo2, settings)
            val orderResult = order1.compareTo(order2)
            if (orderResult == 0) return 1 // 后到者优先
            // 文件路径相同时，按照在设置中的顺序，由大到小排序
            return -orderResult
        }
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
