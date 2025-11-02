package icu.windea.pls.model.index

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType

data class ParadoxFilePathIndexInfo(
    val directory: String,
    override val gameType: ParadoxGameType,
    val included: Boolean,
) : ParadoxIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
