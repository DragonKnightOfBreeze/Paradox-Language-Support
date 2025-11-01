package icu.windea.pls.model.index

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType

data class ParadoxComplexEnumValueIndexInfo(
    val name: String,
    val enumName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
