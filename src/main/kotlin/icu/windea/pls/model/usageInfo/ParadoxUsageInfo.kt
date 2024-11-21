package icu.windea.pls.model.usageInfo

import com.intellij.openapi.vfs.*
import com.intellij.util.*
import icu.windea.pls.model.*

/**
 * @property elementOffset 对应的表达式PSI元素在文件中的起始位置。
 * @property virtualFile 对应的虚拟文件。仅使用[QueryExecutor]进行查询时才能获取。
 */
interface ParadoxUsageInfo {
    val elementOffset: Int
    var virtualFile: VirtualFile?

    /**
     * @property elementOffsets 对应的表达式PSI元素在文件中的所有起始位置。
     * @property virtualFile 对应的虚拟文件。仅使用[QueryExecutor]进行查询时才能获取。
     */
    interface Compact {
        val elementOffsets: Collection<Int>
        var virtualFile: VirtualFile?
    }
}
