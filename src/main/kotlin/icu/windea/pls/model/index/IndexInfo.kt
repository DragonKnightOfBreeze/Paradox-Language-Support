package icu.windea.pls.model.index

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.QueryExecutor

/**
 * 索引信息。
 *
 * @property virtualFile 对应的虚拟文件。仅使用 [QueryExecutor] 进行查询时才能获取。
 */
interface IndexInfo {
    var virtualFile: VirtualFile?
}
