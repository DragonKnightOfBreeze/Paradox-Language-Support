package icu.windea.pls.model.indexInfo

import com.intellij.openapi.vfs.*
import com.intellij.util.*
import icu.windea.pls.model.*

/**
 * @property elementOffset 对应的表达式PSI元素在文件中的起始位置。
 * @property gameType 对应的游戏类型。
 * @property virtualFile 对应的虚拟文件。仅使用[QueryExecutor]进行查询时才能获取。
 */
interface ParadoxIndexInfo {
    val elementOffset: Int
    val gameType: ParadoxGameType
    var virtualFile: VirtualFile?

    /**
     * @property elementOffsets 对应的表达式PSI元素在文件中的所有起始位置。
     * @property gameType 对应的游戏类型。
     * @property virtualFile 对应的虚拟文件。仅使用[QueryExecutor]进行查询时才能获取。
     */
    interface Compact {
        val elementOffsets: Collection<Int>
        val gameType: ParadoxGameType
        var virtualFile: VirtualFile?
    }
}

