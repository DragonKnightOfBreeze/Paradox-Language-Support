package icu.windea.pls.model.index

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType

/**
 * 索引信息。
 *
 * @property gameType 对应的游戏类型。
 * @property virtualFile 对应的虚拟文件。仅使用 [com.intellij.util.QueryExecutor] 进行查询时才能获取。
 */
interface ParadoxIndexInfo {
    val gameType: ParadoxGameType
    var virtualFile: VirtualFile?
}
