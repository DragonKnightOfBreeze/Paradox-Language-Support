package icu.windea.pls.model.usageInfo

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxParameterUsageInfo(
    val name: String,
    val contextKey: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxUsageInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
