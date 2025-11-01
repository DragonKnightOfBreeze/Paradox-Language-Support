package icu.windea.pls.model.index

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType

data class ParadoxDefineIndexInfo(
    val namespace: String,
    val variable: String?,
    val elementOffsets: Set<Int>,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
