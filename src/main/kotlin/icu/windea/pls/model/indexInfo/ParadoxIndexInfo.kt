package icu.windea.pls.model.indexInfo

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType

/**
 * 索引信息。记录的信息包括 PSI 的偏移和游戏类型。
 *
 * @property elementOffset 对应的 PSI 元素在文件中的起始位置。
 * @property gameType 对应的游戏类型。
 * @property virtualFile 对应的虚拟文件。仅使用 [com.intellij.util.QueryExecutor] 进行查询时才能获取。
 */
interface ParadoxIndexInfo {
    val elementOffset: Int
    val gameType: ParadoxGameType
    var virtualFile: VirtualFile?

    /**
     * 整合的索引信息。记录的信息包括所有 PSI 元素的偏移和游戏类型。
     *
     * @property elementOffsets 对应的 PSI 元素在文件中的所有起始位置。
     */
    interface Compact : ParadoxIndexInfo {
        val elementOffsets: Set<Int>

        override val elementOffset: Int get() = elementOffsets.firstOrNull() ?: -1
    }
}
