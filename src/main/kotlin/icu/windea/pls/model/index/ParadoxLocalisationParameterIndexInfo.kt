package icu.windea.pls.model.index

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType

data class ParadoxLocalisationParameterIndexInfo(
    val name: String,
    val localisationName: String,
    val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
