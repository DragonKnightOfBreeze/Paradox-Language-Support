package icu.windea.pls.model.indexInfo

import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxInlineScriptUsageIndexInfo(
    val expression: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null

    data class Compact(
        val expression: String,
        override val elementOffsets: Collection<Int>,
        override val gameType: ParadoxGameType,
    ) : ParadoxIndexInfo.Compact {
        @Volatile
        override var virtualFile: VirtualFile? = null
    }
}

