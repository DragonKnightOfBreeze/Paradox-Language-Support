package icu.windea.pls.model.index

import icu.windea.pls.core.ReadWriteAccess
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.lang.psi.CwtPsiFileService
import icu.windea.pls.model.ParadoxGameType

/**
 * @see icu.windea.pls.lang.psi.light.CwtConfigSymbolLightElement
 * @see icu.windea.pls.lang.index.CwtConfigIndexInfoAwareFileBasedIndex
 */
data class CwtConfigSymbolIndexInfo(
    val name: String,
    val type: String,
    val readWriteAccess: ReadWriteAccess,
    val offset: Int,
    val elementOffset: Int,
    override val gameType: ParadoxGameType
) : CwtConfigIndexInfo() {
    val element: CwtStringExpressionElement?
        get() = file?.let { file -> CwtPsiFileService.findStringExpressionElementFromStartOffset(file, elementOffset) }
}
