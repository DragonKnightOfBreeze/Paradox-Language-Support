package icu.windea.pls.model.indexInfo

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType

data class ParadoxDefineIndexInfo(
    val namespace: String,
    val variable: String?,
    override val elementOffsets: Set<Int>,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo.Compact {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
