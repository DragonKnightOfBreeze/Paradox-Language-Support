package icu.windea.pls.model.index

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.lang.psi.CwtPsiFileManager
import icu.windea.pls.model.ParadoxGameType

data class CwtConfigSymbolIndexInfo(
    val name: String,
    val type: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    val offset: Int,
    val elementOffset: Int,
    override val gameType: ParadoxGameType
) : CwtConfigIndexInfo() {
    val element: CwtStringExpressionElement?
        get() = file?.let { file -> CwtPsiFileManager.findStringExpressionElementFromStartOfffset(file, elementOffset) }
}
