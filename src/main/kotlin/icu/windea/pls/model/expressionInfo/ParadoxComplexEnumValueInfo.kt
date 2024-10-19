package icu.windea.pls.model.expressionInfo

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxComplexEnumValueInfo(
    val name: String,
    val enumName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
) : ParadoxExpressionInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
