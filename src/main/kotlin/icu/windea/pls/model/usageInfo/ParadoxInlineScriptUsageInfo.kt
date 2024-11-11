package icu.windea.pls.model.usageInfo

import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxInlineScriptUsageInfo(
    val expression: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxUsageInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null

    data class Compact(
        val expression: String,
        override val elementOffsets: Collection<Int>,
        override val gameType: ParadoxGameType,
    ) : ParadoxUsageInfo.Compact {
        @Volatile
        override var virtualFile: VirtualFile? = null
    }
}
