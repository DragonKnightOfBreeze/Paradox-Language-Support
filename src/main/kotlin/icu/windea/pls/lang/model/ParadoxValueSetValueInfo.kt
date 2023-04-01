package icu.windea.pls.lang.model

import com.intellij.codeInsight.highlighting.*
import com.intellij.psi.*

data class ParadoxValueSetValueInfo(
    val name: String,
    val valueSetName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val gameType: ParadoxGameType
    //TODO 保存作用域信息
): ParadoxScriptExpressionInfo {
    override var file: PsiFile? = null
}
