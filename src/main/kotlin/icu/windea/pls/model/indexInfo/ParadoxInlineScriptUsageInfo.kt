package icu.windea.pls.model.indexInfo

import com.intellij.openapi.vfs.*

data class ParadoxInlineScriptUsageInfo(
    val expression: String,
    override val elementOffset: Int,
) : ParadoxIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null

    data class Compact(
        val expression: String,
        override val elementOffsets: Collection<Int>,
    ) : ParadoxIndexInfo.Compact {
        @Volatile
        override var virtualFile: VirtualFile? = null
    }
}
