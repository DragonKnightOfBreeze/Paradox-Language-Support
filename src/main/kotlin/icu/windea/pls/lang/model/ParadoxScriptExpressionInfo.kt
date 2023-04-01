package icu.windea.pls.lang.model

import com.intellij.psi.*

interface ParadoxScriptExpressionInfo {
    val gameType: ParadoxGameType
    var file: PsiFile?
}

inline fun <T> ParadoxScriptExpressionInfo.withFile(file: PsiFile, action: () -> T): T {
    this.file = file
    val r = action()
    this.file = null
    return r
}