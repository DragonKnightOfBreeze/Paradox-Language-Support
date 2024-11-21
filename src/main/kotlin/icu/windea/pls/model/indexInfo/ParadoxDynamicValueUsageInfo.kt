package icu.windea.pls.model.indexInfo

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.vfs.*

data class ParadoxDynamicValueUsageInfo(
    val name: String,
    val dynamicValueType: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val elementOffset: Int,
) : ParadoxIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
