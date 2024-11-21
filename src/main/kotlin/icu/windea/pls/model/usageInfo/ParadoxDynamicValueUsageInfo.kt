package icu.windea.pls.model.usageInfo

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxDynamicValueUsageInfo(
    val name: String,
    val dynamicValueType: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val elementOffset: Int,
) : ParadoxUsageInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
