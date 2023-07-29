package icu.windea.pls.lang.model

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.script.psi.*

/**
 * 脚本表达式信息。
 * @property elementOffset 对应的[ParadoxScriptExpressionElement]在文件中的起始位置。
 * @property gameType 对应的游戏类型。
 * @property virtualFile 对应的虚拟文件。使用[QueryExecutor]进行查询时才能获取。
 * @see ParadoxScriptExpressionElement
 */
interface ParadoxExpressionInfo {
    val elementOffset: Int
    val gameType: ParadoxGameType
    var virtualFile: VirtualFile?
}

inline fun <T> ParadoxExpressionInfo.withVirtualFile(virtualFile: VirtualFile, action: () -> T): T {
    this.virtualFile = virtualFile
    val r = action()
    this.virtualFile = null
    return r
}
