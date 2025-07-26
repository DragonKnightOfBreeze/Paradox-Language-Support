package icu.windea.pls.model.indexInfo

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class CwtConfigSymbolIndexInfo(
    val name: String,
    val type: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    val offset: Int,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
) : CwtConfigIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
