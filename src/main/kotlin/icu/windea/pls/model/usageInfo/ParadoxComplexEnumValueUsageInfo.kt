package icu.windea.pls.model.usageInfo

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxComplexEnumValueUsageInfo(
    val name: String,
    val enumName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val elementOffset: Int,
) : ParadoxUsageInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
