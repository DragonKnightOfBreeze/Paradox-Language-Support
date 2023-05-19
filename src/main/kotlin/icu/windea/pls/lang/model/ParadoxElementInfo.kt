package icu.windea.pls.lang.model

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.script.psi.*

/**
 *
 * @property elementOffset 对应的[ParadoxScriptExpressionElement]在文件中的起始位置。
 * @property gameType 对应的游戏类型。
 * @property file 对应的文件。使用[QueryExecutor]进行查询时才能获取。
 */
interface ParadoxElementInfo {
    val elementOffset: Int
    val gameType: ParadoxGameType
    var file: PsiFile?
}

inline fun <T> ParadoxElementInfo.withFile(file: PsiFile, action: () -> T): T {
    this.file = file
    val r = action()
    this.file = null
    return r
}