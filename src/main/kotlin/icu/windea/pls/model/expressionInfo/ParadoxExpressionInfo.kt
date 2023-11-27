package icu.windea.pls.model.expressionInfo

import com.intellij.openapi.vfs.*
import com.intellij.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * @property elementOffset 对应的表达式PSI元素在文件中的起始位置。
 * @property gameType 对应的游戏类型。
 * @property virtualFile 对应的虚拟文件。使用[QueryExecutor]进行查询时才能获取。
 * @see ParadoxScriptExpressionElement
 */
interface ParadoxExpressionInfo {
    val elementOffset: Int
    val gameType: ParadoxGameType
    var virtualFile: VirtualFile?
}

