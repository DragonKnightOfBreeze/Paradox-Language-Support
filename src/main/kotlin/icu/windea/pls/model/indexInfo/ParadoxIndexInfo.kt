package icu.windea.pls.model.indexInfo

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.QueryExecutor
import icu.windea.pls.model.ParadoxGameType

/**
 * 索引信息。记录的信息包括PSI元素偏移与游戏类型。
 *
 * @property elementOffset 对应的PSI元素在文件中的起始位置。
 * @property gameType 对应的游戏类型。
 * @property virtualFile 对应的虚拟文件。仅使用[QueryExecutor]进行查询时才能获取。
 */
interface ParadoxIndexInfo {
    val elementOffset: Int
    val gameType: ParadoxGameType
    var virtualFile: VirtualFile?

    /**
     * 整合的索引信息。记录的信息包括所有的PSI元素偏移与游戏类型。
     *
     * @property elementOffsets 对应的PSI元素在文件中的所有起始位置。
     * @property gameType 对应的游戏类型。
     * @property virtualFile 对应的虚拟文件。仅使用[QueryExecutor]进行查询时才能获取。
     */
    interface Compact {
        val elementOffsets: Set<Int>
        val gameType: ParadoxGameType
        var virtualFile: VirtualFile?
    }
}
