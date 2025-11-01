package icu.windea.pls.model.index

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType

data class CwtConfigSymbolIndexInfo(
    val name: String,
    val type: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    val offset: Int,
    val elementOffset: Int,
    override val gameType: ParadoxGameType
) : CwtConfigIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
